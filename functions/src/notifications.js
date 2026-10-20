import { randomUUID } from "node:crypto";
import { digest } from "./service.js";

const invalidTokenCodes = new Set([
  "messaging/registration-token-not-registered", "messaging/invalid-registration-token"
]);
export function messageText(job) {
  switch (job.kind) {
    case "ARRIVED": return { title: "A customer has arrived", body: "Open RUKO to view your live queue." };
    case "STARTED": return { title: "Your service has started", body: "Your turn is now in progress." };
    case "COMPLETED": return { title: "Your visit is complete", body: "Thank you for using RUKO." };
    case "JOINED":
      return job.position === 1
        ? { title: "You're next", body: "Please arrive and check in. Open RUKO to view your queue." }
        : { title: "You're in line", body: "Open RUKO to follow your position and check in." };
    case "PROGRESS":
      if (job.position <= 3) return job.arrived
        ? { title: "Your turn is approaching", body: "You're checked in. Keep an eye on your live queue." }
        : { title: "Your turn is approaching", body: "Please arrive and check in. Open RUKO for your live position." };
      return { title: "Your queue has moved", body: "Open RUKO for your latest position and waiting time." };
    default: return { title: "Your queue has moved", body: "Open RUKO for your latest position and waiting time." };
  }
}
// Durable outbox with leases and a scheduled recovery pass. FCM itself does not offer
// exactly-once delivery; stable collapse IDs and client event IDs suppress repeats.
export function notificationWorker(db, messaging) {
  return async function deliver(jobId) {
    const ref = db.doc("notificationJobs/" + jobId);
    const now = Date.now();
    const leaseId = randomUUID();
    const job = await db.runTransaction(async tx => {
      const snap = await tx.get(ref);
      if (!snap.exists) return null;
      const data = snap.data();
      if (!["PENDING", "PROCESSING"].includes(data.status) || data.nextAttemptAt > now) return null;
      if (data.attempts >= 10) {
        tx.update(ref, { status: "DEAD", lastError: "Retry limit reached" });
        return null;
      }
      tx.update(ref, { status: "PROCESSING", leaseId,
        attempts: data.attempts + 1, nextAttemptAt: now + 120000 });
      return data;
    });
    if (!job) return;
    async function finish(update) {
      await db.runTransaction(async tx => {
        const current = await tx.get(ref);
        if (current.data()?.leaseId === leaseId) tx.update(ref, update);
      });
    }
    try {
      // Don't send old waiting alerts to a customer who already left or completed.
      const membership = await db.doc("activeMemberships/" + job.uid).get();
      if (!["ARRIVED", "COMPLETED"].includes(job.kind) &&
          (!membership.data()?.active || membership.data().ticketId !== job.ticketId)) {
        await finish({ status: "SUPERSEDED" });
        return;
      }
      if (job.kind === "ARRIVED") {
        const queue = await db.doc("queues/" + job.queueId).get();
        if (queue.data()?.storeId !== job.uid) {
          await finish({ status: "SUPERSEDED" });
          return;
        }
      }
      const devices = await db.collection("pushDevices").where("uid", "==", job.uid)
        .where("updatedAt", ">=", now - 60 * 86400000).limit(20).get();
      if (devices.empty) { await finish({ status: "NO_DEVICES" }); return; }
      const text = messageText(job);
      const collapse = digest(job.queueId + ":" + job.ticketId).slice(0, 32);
      const tokens = devices.docs.map(doc => doc.data().token);
      const response = await messaging.sendEachForMulticast({
        tokens, notification: text,
        data: { eventId: jobId, queueId: job.queueId, ticketId: job.ticketId,
          kind: job.kind, revision: String(job.revision) },
        android: { priority: "high", collapseKey: collapse,
          notification: { channelId: "ruko_queue", tag: collapse, visibility: "private",
            clickAction: "RUKO_QUEUE_NOTIFICATION" } },
        apns: { headers: { "apns-collapse-id": collapse, "apns-priority": "10" },
          payload: { aps: { sound: "default", "thread-id": job.queueId } } }
      });
      let retry = false;
      for (let i = 0; i < response.responses.length; i++) {
        const item = response.responses[i];
        if (item.success) continue;
        if (invalidTokenCodes.has(item.error?.code)) {
          // Conditional delete: don't erase a device that was rebound meanwhile.
          await db.runTransaction(async tx => {
            const doc = devices.docs[i];
            const current = await tx.get(doc.ref);
            if (current.data()?.uid === job.uid && current.data()?.token === tokens[i]) tx.delete(doc.ref);
          });
        } else retry = true;
      }
      await finish(retry ? { status: "PENDING", nextAttemptAt: now + Math.min(3600000, 30000 * 2 ** job.attempts),
        lastError: "Transient FCM delivery failure" } : { status: "SENT", sentAt: Date.now() });
    } catch (error) {
      await finish({ status: "PENDING", nextAttemptAt: now + Math.min(3600000, 30000 * 2 ** job.attempts),
        lastError: String(error.code ?? "delivery-failed").slice(0, 100) });
    }
  };
}
