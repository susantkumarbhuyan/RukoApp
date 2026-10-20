import { createHash, randomUUID } from "node:crypto";
import { Timestamp } from "firebase-admin/firestore";
import { applyCommand, initialRuntime, QueueError, requireThat, validateConfig } from "./domain.js";

export const digest = value => createHash("sha256").update(value).digest("hex");
export function safeId(value, label = "identifier") {
  requireThat(typeof value === "string" && /^[A-Za-z0-9_-]{1,100}$/.test(value),
    "invalid-argument", "Invalid " + label);
  return value;
}
function requestKey(value) {
  requireThat(typeof value === "string" && /^[A-Za-z0-9_-]{16,80}$/.test(value),
    "invalid-argument", "A stable request ID is required");
  return value;
}
async function readThrottle(tx, db, uid, now) {
  const ref = db.doc("rateLimits/" + uid);
  const snapshot = await tx.get(ref);
  const previous = snapshot.data();
  const within = previous && now - previous.window < 60000;
  const count = within ? previous.count + 1 : 1;
  requireThat(count <= 60, "resource-exhausted", "Too many requests; please wait a minute");
  return { ref, data: { window: within ? previous.window : now, count } };
}
export function createService(db) {
  return {
    async command(uid, input) {
      const queueId = safeId(input.queueId, "queue");
      const requestId = requestKey(input.requestId);
      const action = input.action;
      requireThat(["JOIN", "ARRIVE", "LEAVE", "START", "COMPLETE"].includes(action),
        "invalid-argument", "Invalid action");
      const ticketId = action === "JOIN" ? null : safeId(input.ticketId, "ticket");
      const fingerprint = digest(JSON.stringify({ action, queueId, ticketId }));
      const receiptRef = db.doc("queueCommands/" + uid + "/receipts/" + requestId);
      const queueRef = db.doc("queues/" + queueId);
      const runtimeRef = db.doc("queueRuntime/" + queueId);
      const now = Date.now();
      const newTicketId = randomUUID();
      return db.runTransaction(async tx => {
        const receipt = await tx.get(receiptRef);
        if (receipt.exists) {
          requireThat(receipt.data().fingerprint === fingerprint, "invalid-argument",
            "Request ID was reused with different arguments");
          return receipt.data().response;
        }
        const [q, rt, own] = await tx.getAll(queueRef, runtimeRef, db.doc("activeMemberships/" + uid));
        requireThat(q.exists && q.data().status !== "DELETED", "not-found", "Queue not found");
        requireThat(q.data().schemaVersion === 2 && rt.exists, "failed-precondition",
          "This queue must be migrated by its owner before use");
        const queue = { ...q.data(), id: queueId };
        const runtime = rt.data();
        requireThat(Array.isArray(runtime.order) && runtime.order.length <= 200,
          "internal", "Queue requires administrator repair");
        const ids = [...new Set([...runtime.order, ...(ticketId ? [ticketId] : [])])];
        ids.forEach(id => safeId(id, "stored ticket"));
        const snapshots = ids.length ? await tx.getAll(...ids.map(id => queueRef.collection("tickets").doc(id))) : [];
        const tickets = Object.fromEntries(snapshots.filter(s => s.exists).map(s => [s.id, s.data()]));
        requireThat(runtime.order.every(id => tickets[id]?.status === "JOINED"),
          "internal", "Queue requires administrator repair");
        let membership = own.data() ?? null;
        if (action === "COMPLETE" && tickets[ticketId]) {
          membership = (await tx.get(db.doc("activeMemberships/" + tickets[ticketId].userId))).data() ?? null;
        }
        const throttle = await readThrottle(tx, db, uid, now);
        const planned = applyCommand({ queue, runtime, tickets, membership }, { action, ticketId },
          { uid, now, newTicketId });
        // All reads finish before the first write. Runtime + membership documents serialize
        // queue admission and cross-queue joins on every device.
        tx.set(throttle.ref, throttle.data);
        if (planned.changed) {
          tx.set(runtimeRef, planned.state.runtime);
          for (const entry of planned.ticketUpdates) {
            tx.set(queueRef.collection("tickets").doc(entry.id), entry);
          }
          if (planned.membershipUpdate) {
            const membership = planned.membershipUpdate;
            tx.set(db.doc("activeMemberships/" + membership.userId),
              { ...membership, updatedAt: now });
          }
          if (planned.history) {
            tx.set(db.doc("queueHistory/" + queueId + "/visits/" + planned.history.id), planned.history);
          }
          for (const notification of planned.notifications) {
            const id = digest([queueId, notification.revision, notification.uid,
              notification.ticketId, notification.kind].join(":"));
            tx.create(db.doc("notificationJobs/" + id), {
              ...notification, status: "PENDING", attempts: 0, nextAttemptAt: now,
              expiresAt: Timestamp.fromMillis(now + 30 * 86400000)
            });
          }
        }
        tx.create(receiptRef, { fingerprint, response: planned.response,
          expiresAt: Timestamp.fromMillis(now + 30 * 86400000) });
        return planned.response;
      });
    },

    async saveQueue(uid, input) {
      const id = safeId(input.queue?.id, "queue");
      const now = Date.now();
      return db.runTransaction(async tx => {
        const queueRef = db.doc("queues/" + id);
        const runtimeRef = db.doc("queueRuntime/" + id);
        const [q, rt, owner] = await tx.getAll(queueRef, runtimeRef, db.doc("stores/" + uid));
        requireThat(owner.exists, "permission-denied", "A business account is required");
        requireThat(!q.exists || q.data().storeId === uid || q.data().businessId === uid,
          "permission-denied", "Only the owner may change this queue");
        requireThat(!q.exists || q.data().schemaVersion === 2, "failed-precondition",
          "Legacy queues must be drained and recreated before upgrading");
        requireThat(!q.exists || q.data().status !== "DELETED", "failed-precondition", "Queue was deleted");
        const throttle = await readThrottle(tx, db, uid, now);
        const queue = validateConfig(input.queue, uid, q.exists ? { ...q.data(), id } : null, rt.data()?.order.length ?? 0);
        queue.createdAt = q.data()?.createdAt ?? now;
        queue.updatedAt = now;
        const runtime = rt.data() ?? initialRuntime(queue);
        // A manually changed default affects estimates until measured service samples exist.
        if (runtime.sampleCount === 0) runtime.averageMillis = queue.avgServiceTime;
        tx.set(throttle.ref, throttle.data);
        tx.set(queueRef, queue);
        tx.set(runtimeRef, runtime);
        return { ok: true, queueId: id };
      });
    },

    async deleteQueue(uid, input) {
      const id = safeId(input.queueId, "queue");
      return db.runTransaction(async tx => {
        const queueRef = db.doc("queues/" + id);
        const runtimeRef = db.doc("queueRuntime/" + id);
        const [q, rt] = await tx.getAll(queueRef, runtimeRef);
        requireThat(q.exists && q.data().storeId === uid, "permission-denied", "Only the owner may delete this queue");
        requireThat(rt.exists && rt.data().order.length === 0, "failed-precondition", "Finish the queue first");
        const throttle = await readThrottle(tx, db, uid, Date.now());
        tx.set(throttle.ref, throttle.data);
        tx.update(queueRef, { status: "DELETED", isActive: false, updatedAt: Date.now() });
        return { ok: true };
      });
    },

    async registerDevice(uid, input) {
      const token = input.token;
      requireThat(typeof token === "string" && token.length >= 20 && token.length <= 4096,
        "invalid-argument", "Invalid device token");
      const ref = db.doc("pushDevices/" + digest(token));
      const now = Date.now();
      return db.runTransaction(async tx => {
        const throttle = await readThrottle(tx, db, uid, now);
        // One token has one current owner: account switching cannot leave duplicate bindings.
        tx.set(throttle.ref, throttle.data);
        tx.set(ref, { uid, token, updatedAt: now });
        return { ok: true };
      });
    },

    async unregisterDevice(uid, input) {
      const token = input.token;
      requireThat(typeof token === "string" && token.length <= 4096, "invalid-argument", "Invalid token");
      const ref = db.doc("pushDevices/" + digest(token));
      return db.runTransaction(async tx => {
        const device = await tx.get(ref);
        if (device.data()?.uid === uid) tx.delete(ref);
        return { ok: true };
      });
    }
  };
}
