// Pure queue policy. No client timestamp, UID, position, or duration is trusted.
export class QueueError extends Error {
  constructor(code, message) { super(message); this.code = code; }
}
export function requireThat(value, code, message) {
  if (!value) throw new QueueError(code, message);
}
export function queueDay(now, timezone = "Asia/Kolkata") {
  const parts = new Intl.DateTimeFormat("en", {
    timeZone: timezone, year: "numeric", month: "2-digit", day: "2-digit"
  }).formatToParts(new Date(now));
  const part = (type) => parts.find(p => p.type === type).value;
  return part("year") + "-" + part("month") + "-" + part("day");
}
export function validateConfig(input, uid, existing, activeCount = 0) {
  requireThat(input && typeof input === "object", "invalid-argument", "Queue details are required");
  const name = String(input.name ?? "").trim();
  const type = String(input.type ?? "").trim();
  const description = String(input.description ?? "").trim();
  const capacity = input.maxQueueSize ?? input.size ?? 100;
  const average = input.avgServiceTime ?? 600000;
  const dailyLimit = input.dailyLimit ?? 100;
  requireThat(name.length > 0 && name.length <= 100 && type.length <= 100 && description.length <= 1000,
    "invalid-argument", "Enter a valid queue name and description");
  requireThat(Number.isInteger(capacity) && capacity >= 1 && capacity <= 200 && capacity >= activeCount,
    "invalid-argument", "Capacity must be 1–200 and cannot be less than the people already in line");
  requireThat(Number.isInteger(average) && average >= 60000 && average <= 86400000,
    "invalid-argument", "Service time must be between one minute and one day");
  requireThat(Number.isInteger(dailyLimit) && dailyLimit >= 1 && dailyLimit <= 10000,
    "invalid-argument", "Daily limit must be 1–10000");
  const timezone = input.timezone ?? "Asia/Kolkata";
  requireThat(typeof timezone === "string" && timezone.length < 80, "invalid-argument", "Invalid timezone");
  try { queueDay(Date.now(), timezone); } catch { throw new QueueError("invalid-argument", "Invalid timezone"); }
  const status = input.status ?? "ACTIVE";
  requireThat(["ACTIVE", "IN_ACTIVE"].includes(status), "invalid-argument", "Invalid queue status");
  requireThat(input.closingHour == null ||
    (Number.isSafeInteger(input.closingHour) && input.closingHour > 0),
    "invalid-argument", "Invalid closing time");
  return {
    id: existing?.id ?? input.id, businessId: uid, storeId: uid,
    name, type, description, size: capacity, maxQueueSize: capacity,
    avgServiceTime: average, dailyLimit, timezone, status,
    isActive: status === "ACTIVE", closingHour: input.closingHour ?? null,
    createdAt: existing?.createdAt ?? Date.now(), schemaVersion: 2
  };
}
export function initialRuntime(queue) {
  return { order: [], nextSequence: 1, revision: 0, averageMillis: queue.avgServiceTime,
    totalServiceMillis: 0, sampleCount: 0, day: "", joinedToday: 0 };
}
export function waitingMillis(order, tickets, ticketId, average, now) {
  const position = order.indexOf(ticketId);
  if (position <= 0) return 0;
  return order.slice(0, position).reduce((sum, id) => {
    const start = tickets[id]?.startedAt;
    return sum + (start == null ? average : Math.max(0, average - Math.max(0, now - start)));
  }, 0);
}
export function applyCommand(source, command, context) {
  const state = structuredClone(source);
  const { uid, now, newTicketId } = context;
  const { queue, runtime, tickets } = state;
  const { action, ticketId } = command;
  const result = { state, changed: false, ticketUpdates: [], membershipUpdate: null,
    history: null, notifications: [], response: { ok: true } };
  const owner = queue.storeId === uid;
  const active = () => {
    const entry = tickets[ticketId];
    requireThat(entry && entry.userId === uid, "permission-denied", "This is not your ticket");
    requireThat(state.membership?.active && state.membership.ticketId === ticketId &&
      state.membership.queueId === queue.id, "failed-precondition", "Your queue membership has ended");
    requireThat(runtime.order.includes(ticketId) && entry.status === "JOINED",
      "failed-precondition", "This ticket is no longer active");
    return entry;
  };
  let touched;
  if (action === "JOIN") {
    if (state.membership?.active) {
      requireThat(state.membership.queueId === queue.id, "already-exists", "You are already in another queue");
      requireThat(tickets[state.membership.ticketId]?.status === "JOINED",
        "failed-precondition", "Queue membership needs administrator repair");
      result.response.ticketId = state.membership.ticketId;
      return result;
    }
    requireThat(queue.isActive && queue.status === "ACTIVE", "failed-precondition", "This queue is closed");
    requireThat(queue.closingHour == null || now < queue.closingHour,
      "failed-precondition", "This queue has closed for today");
    const cap = queue.maxQueueSize ?? queue.size;
    requireThat(runtime.order.length < cap, "resource-exhausted", "This queue is full");
    const day = queueDay(now, queue.timezone);
    if (runtime.day !== day) { runtime.day = day; runtime.joinedToday = 0; }
    requireThat(runtime.joinedToday < queue.dailyLimit, "resource-exhausted", "Today's token limit has been reached");
    touched = { id: newTicketId, queueId: queue.id, userId: uid, status: "JOINED",
      joinedAt: now, sequence: runtime.nextSequence++, startedAt: null, arrivedAt: null, completedAt: null };
    tickets[touched.id] = touched;
    runtime.order.push(touched.id);
    runtime.joinedToday++;
    result.membershipUpdate = { userId: uid, queueId: queue.id, ticketId: touched.id, active: true };
    result.response.ticketId = touched.id;
  } else if (action === "ARRIVE") {
    touched = active();
    if (touched.arrivedAt != null) return result;
    touched.arrivedAt = now;
  } else if (action === "LEAVE") {
    const previous = tickets[ticketId];
    if (previous?.userId === uid && previous.status === "LEFT") return result;
    touched = active();
    requireThat(touched.startedAt == null, "failed-precondition", "Service is already in progress");
    touched.status = "LEFT";
    touched.completedAt = now;
    runtime.order = runtime.order.filter(id => id !== ticketId);
    result.membershipUpdate = { userId: uid, queueId: queue.id, ticketId, active: false };
  } else if (action === "START" || action === "COMPLETE") {
    requireThat(owner, "permission-denied", "Only the business owner may manage service");
    touched = tickets[ticketId];
    requireThat(touched, "not-found", "Ticket not found");
    if (action === "COMPLETE" && touched.status === "COMPLETED") return result;
    requireThat(runtime.order[0] === ticketId && touched.status === "JOINED",
      "failed-precondition", "Serve the first customer in line");
    if (action === "START") {
      requireThat(touched.arrivedAt != null, "failed-precondition", "Customer must check in first");
      if (touched.startedAt != null) return result;
      touched.startedAt = now;
    } else {
      requireThat(touched.startedAt != null, "failed-precondition", "Start service first");
      requireThat(state.membership?.active && state.membership.ticketId === ticketId &&
        state.membership.queueId === queue.id, "failed-precondition", "Queue membership needs repair");
      touched.status = "COMPLETED";
      touched.completedAt = now;
      const serviceTime = Math.max(1, now - touched.startedAt);
      result.history = { id: ticketId, queueId: queue.id, userId: touched.userId,
        joinedAt: touched.joinedAt, startedAt: touched.startedAt, completedAt: now, serviceTime };
      runtime.totalServiceMillis += serviceTime;
      runtime.sampleCount++;
      runtime.averageMillis = Math.round(runtime.totalServiceMillis / runtime.sampleCount);
      runtime.order.shift();
      result.membershipUpdate = { userId: touched.userId, queueId: queue.id, ticketId, active: false };
    }
  } else {
    throw new QueueError("invalid-argument", "Unknown queue action");
  }
  result.changed = true;
  runtime.revision++;
  result.ticketUpdates.push(touched);
  const notify = (userId, id, kind, position = 0) => result.notifications.push({
    uid: userId, queueId: queue.id, ticketId: id, kind, position,
    estimatedMillis: waitingMillis(runtime.order, tickets, id, runtime.averageMillis, now),
    arrived: tickets[id]?.arrivedAt != null,
    revision: runtime.revision, createdAt: now
  });
  if (action === "JOIN") notify(uid, touched.id, "JOINED", runtime.order.indexOf(touched.id) + 1);
  if (action === "ARRIVE") notify(queue.storeId, touched.id, "ARRIVED");
  if (action === "START") notify(touched.userId, touched.id, "STARTED", 1);
  if (action === "COMPLETE") notify(touched.userId, touched.id, "COMPLETED");
  if (action === "COMPLETE" || action === "LEAVE") {
    runtime.order.forEach((id, i) => notify(tickets[id].userId, id, "PROGRESS", i + 1));
  }
  return result;
}
