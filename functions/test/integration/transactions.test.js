import test from "node:test";
import assert from "node:assert/strict";
import { randomUUID } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { createService } from "../../src/service.js";

if (!process.env.FIRESTORE_EMULATOR_HOST) throw new Error("Emulator required; production tests are forbidden");
const app = initializeApp({ projectId: "demo-ruko" }, "transaction-tests");
const db = getFirestore(app);
const service = createService(db);
const id = prefix => prefix + randomUUID().replaceAll("-", "");
async function fixture(capacity = 2) {
  const owner = id("owner"), queueId = id("queue");
  await db.doc("stores/" + owner).set({ id: owner, businessName: "Test Salon" });
  await service.saveQueue(owner, { queue: { id: queueId, name: "Test queue", size: capacity, avgServiceTime: 600000 } });
  return { owner, queueId };
}
const call = (uid, queueId, action, ticketId, requestId = id("request")) =>
  service.command(uid, { queueId, action, ticketId, requestId });

test("simultaneous customers cannot exceed capacity", async () => {
  const { queueId } = await fixture(2);
  const joins = await Promise.allSettled(
    Array.from({ length: 8 }, () => call(id("user"), queueId, "JOIN"))
  );
  assert.equal(joins.filter(x => x.status === "fulfilled").length, 2);
  const runtime = (await db.doc("queueRuntime/" + queueId).get()).data();
  assert.equal(runtime.order.length, 2);
  const tickets = await db.collection("queues/" + queueId + "/tickets").get();
  assert.equal(tickets.size, 2);
});

test("simultaneous joins across businesses retain one active membership", async () => {
  const a = await fixture(), b = await fixture(), uid = id("user");
  const results = await Promise.allSettled([call(uid, a.queueId, "JOIN"), call(uid, b.queueId, "JOIN")]);
  assert.equal(results.filter(x => x.status === "fulfilled").length, 1);
  const member = (await db.doc("activeMemberships/" + uid).get()).data();
  assert.equal(member.active, true);
  const total = (await db.doc("queueRuntime/" + a.queueId).get()).data().order.length +
    (await db.doc("queueRuntime/" + b.queueId).get()).data().order.length;
  assert.equal(total, 1);
});

test("concurrent duplicate completion creates one history row and advances once", async () => {
  const { owner, queueId } = await fixture(), uid = id("user");
  const { ticketId } = await call(uid, queueId, "JOIN");
  await call(uid, queueId, "ARRIVE", ticketId);
  await call(owner, queueId, "START", ticketId);
  const later = id("later");
  await call(later, queueId, "JOIN");
  await Promise.all([call(owner, queueId, "COMPLETE", ticketId), call(owner, queueId, "COMPLETE", ticketId)]);
  assert.equal((await db.collection("queueHistory/" + queueId + "/visits").get()).size, 1);
  const rt = (await db.doc("queueRuntime/" + queueId).get()).data();
  assert.equal(rt.order.length, 1);
  assert.equal(rt.sampleCount, 1);
  assert.equal((await db.doc("activeMemberships/" + uid).get()).data().active, false);
});

test("receipt replay returns identical ticket and cannot be reused for another action", async () => {
  const { queueId } = await fixture(), uid = id("user"), request = id("request");
  const first = await call(uid, queueId, "JOIN", null, request);
  const retry = await call(uid, queueId, "JOIN", null, request);
  assert.equal(first.ticketId, retry.ticketId);
  await assert.rejects(call(uid, queueId, "ARRIVE", first.ticketId, request), { code: "invalid-argument" });
});

test("non-owner service commands cannot modify another business queue", async () => {
  const { queueId } = await fixture(), uid = id("user");
  const { ticketId } = await call(uid, queueId, "JOIN");
  await call(uid, queueId, "ARRIVE", ticketId);
  await assert.rejects(call(id("intruder"), queueId, "START", ticketId), { code: "permission-denied" });
  assert.equal((await db.doc("queues/" + queueId + "/tickets/" + ticketId).get()).data().startedAt, null);
});
