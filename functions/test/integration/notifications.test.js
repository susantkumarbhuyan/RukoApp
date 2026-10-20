import test from "node:test";
import assert from "node:assert/strict";
import { randomUUID } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { createService, digest } from "../../src/service.js";
import { notificationWorker } from "../../src/notifications.js";

if (!process.env.FIRESTORE_EMULATOR_HOST) throw new Error("Emulator required");
const db = getFirestore(initializeApp({ projectId: "demo-ruko" }, "notification-tests"));
const service = createService(db);
const id = () => randomUUID().replaceAll("-", "");
async function fixture() {
  const uid = id(), ticketId = id(), jobId = id(), queueId = id();
  await db.doc("activeMemberships/" + uid).set({ uid, ticketId, queueId, active: true });
  await db.doc("notificationJobs/" + jobId).set({ uid, ticketId, queueId, kind: "PROGRESS",
    revision: 1, status: "PENDING", attempts: 0, nextAttemptAt: Date.now(), createdAt: Date.now() });
  return { uid, ticketId, jobId };
}

test("concurrent trigger retries claim one delivery lease", async () => {
  const f = await fixture(), token = "test-token-" + id();
  await service.registerDevice(f.uid, { token });
  let sends = 0;
  const worker = notificationWorker(db, { sendEachForMulticast: async request => {
    sends++;
    return { responses: request.tokens.map(() => ({ success: true })) };
  } });
  await Promise.all([worker(f.jobId), worker(f.jobId)]);
  await worker(f.jobId);
  assert.equal(sends, 1);
  assert.equal((await db.doc("notificationJobs/" + f.jobId).get()).data().status, "SENT");
});

test("FCM invalid tokens are removed, transient failures remain retryable", async () => {
  const f = await fixture(), token = "test-token-" + id();
  await service.registerDevice(f.uid, { token });
  const worker = notificationWorker(db, { sendEachForMulticast: async () => ({
    responses: [{ success: false, error: { code: "messaging/registration-token-not-registered" } }]
  }) });
  await worker(f.jobId);
  assert.equal((await db.doc("pushDevices/" + digest(token)).get()).exists, false);
  const g = await fixture(), token2 = "test-token-" + id();
  await service.registerDevice(g.uid, { token: token2 });
  await notificationWorker(db, { sendEachForMulticast: async () => {
    throw Object.assign(new Error("Transient"), { code: "messaging/server-unavailable" });
  } })(g.jobId);
  const job = (await db.doc("notificationJobs/" + g.jobId).get()).data();
  assert.equal(job.status, "PENDING");
  assert.ok(job.nextAttemptAt > Date.now());
});

test("account switching rebinds device and old logout cannot remove new binding", async () => {
  const token = "test-token-" + id(), a = id(), b = id();
  await service.registerDevice(a, { token });
  await service.registerDevice(b, { token });
  await service.unregisterDevice(a, { token });
  assert.equal((await db.doc("pushDevices/" + digest(token)).get()).data().uid, b);
});

test("waiting alerts are suppressed after a customer leaves", async () => {
  const f = await fixture();
  await db.doc("activeMemberships/" + f.uid).update({ active: false });
  let sends = 0;
  const worker = notificationWorker(db, { sendEachForMulticast: async () => { sends++; } });
  await worker(f.jobId);
  assert.equal(sends, 0);
  assert.equal((await db.doc("notificationJobs/" + f.jobId).get()).data().status, "SUPERSEDED");
});
