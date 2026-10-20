import test from "node:test";
import assert from "node:assert/strict";
import { applyCommand, initialRuntime, validateConfig, waitingMillis, queueDay } from "../src/domain.js";

function fixture() {
  const queue = validateConfig({ id: "queue1", name: "Salon", size: 2, dailyLimit: 10, avgServiceTime: 600000 }, "owner");
  return { queue, runtime: initialRuntime(queue), tickets: {}, membership: null };
}
function run(s, uid, action, ticketId = null, now = 1000000, newTicketId = "ticket1") {
  return applyCommand(s, { action, ticketId }, { uid, now, newTicketId });
}

test("join uses server identity and creates ordered membership", () => {
  const r = run(fixture(), "customer", "JOIN");
  assert.deepEqual(r.state.runtime.order, ["ticket1"]);
  assert.equal(r.ticketUpdates[0].userId, "customer");
  assert.equal(r.membershipUpdate.active, true);
  assert.equal(r.state.runtime.joinedToday, 1);
});

test("duplicate join returns existing ticket without consuming capacity", () => {
  const r = run(fixture(), "customer", "JOIN");
  r.state.membership = r.membershipUpdate;
  const again = run(r.state, "customer", "JOIN", null, 2000000, "duplicate");
  assert.equal(again.changed, false);
  assert.equal(again.response.ticketId, "ticket1");
  assert.equal(again.state.runtime.order.length, 1);
});

test("joining another queue while active is rejected", () => {
  const s = fixture();
  s.membership = { active: true, queueId: "other", ticketId: "other-ticket" };
  assert.throws(() => run(s, "customer", "JOIN"), { code: "already-exists" });
});

test("capacity and daily limits are enforced", () => {
  let r = run(fixture(), "a", "JOIN");
  r.state.membership = null;
  r = run(r.state, "b", "JOIN", null, 1000001, "ticket2");
  r.state.membership = null;
  assert.throws(() => run(r.state, "c", "JOIN"), { code: "resource-exhausted" });
  const s = fixture(); s.queue.dailyLimit = 1;
  const first = run(s, "a", "JOIN");
  first.state.membership = null;
  assert.throws(() => run(first.state, "b", "JOIN"), { code: "resource-exhausted" });
});

test("cannot start without arrival, cannot start out of order, only owner can serve", () => {
  let r = run(fixture(), "a", "JOIN");
  r.state.membership = r.membershipUpdate;
  assert.throws(() => run(r.state, "owner", "START", "ticket1"), { code: "failed-precondition" });
  r = run(r.state, "a", "ARRIVE", "ticket1");
  assert.throws(() => run(r.state, "intruder", "START", "ticket1"), { code: "permission-denied" });
  assert.throws(() => run(r.state, "owner", "START", "other"), { code: "not-found" });
  const started = run(r.state, "owner", "START", "ticket1", 1100000);
  assert.equal(started.ticketUpdates[0].startedAt, 1100000);
  assert.throws(() => run(started.state, "a", "LEAVE", "ticket1"), { code: "failed-precondition" });
});

test("completion records actual service and releases membership exactly once", () => {
  let r = run(fixture(), "a", "JOIN");
  r.state.membership = r.membershipUpdate;
  r = run(r.state, "a", "ARRIVE", "ticket1", 1100000);
  r = run(r.state, "owner", "START", "ticket1", 1200000);
  r = run(r.state, "owner", "COMPLETE", "ticket1", 1500000);
  assert.equal(r.history.serviceTime, 300000);
  assert.equal(r.membershipUpdate.active, false);
  assert.deepEqual(r.state.runtime.order, []);
  assert.equal(r.state.runtime.sampleCount, 1);
  const duplicate = run(r.state, "owner", "COMPLETE", "ticket1", 1600000);
  assert.equal(duplicate.changed, false);
  assert.equal(duplicate.history, null);
});

test("one customer cannot arrive or leave for another", () => {
  const r = run(fixture(), "a", "JOIN");
  r.state.membership = r.membershipUpdate;
  assert.throws(() => run(r.state, "b", "ARRIVE", "ticket1"), { code: "permission-denied" });
  assert.throws(() => run(r.state, "b", "LEAVE", "ticket1"), { code: "permission-denied" });
});

test("waiting time excludes own service and never becomes negative", () => {
  const tickets = { a: { startedAt: 1000 }, b: {}, c: {} };
  assert.equal(waitingMillis(["a", "b", "c"], tickets, "c", 600000, 301000), 900000);
  assert.equal(waitingMillis(["a", "b", "c"], tickets, "c", 600000, 901000), 600000);
});

test("daily boundary uses business timezone", () => {
  assert.equal(queueDay(Date.UTC(2026, 9, 20, 20), "Asia/Kolkata"), "2026-10-21");
  assert.equal(queueDay(Date.UTC(2026, 9, 20, 17), "Asia/Kolkata"), "2026-10-20");
});

test("closed queues and reducing capacity under active count are rejected", () => {
  const s = fixture(); s.queue.isActive = false;
  assert.throws(() => run(s, "a", "JOIN"), { code: "failed-precondition" });
  assert.throws(() => validateConfig({ id: "q", name: "Salon", size: 1 }, "owner", null, 2),
    { code: "invalid-argument" });
});
