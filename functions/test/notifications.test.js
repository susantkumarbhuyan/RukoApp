import test from "node:test";
import assert from "node:assert/strict";
import { messageText } from "../src/notifications.js";

test("near-front customers receive an arrival reminder", () => {
  const text = messageText({ kind: "PROGRESS", position: 2, arrived: false });
  assert.match(text.body, /arrive and check in/);
});
test("checked-in customers do not receive a redundant arrival request", () => {
  const text = messageText({ kind: "PROGRESS", position: 1, arrived: true });
  assert.match(text.body, /checked in/);
});
test("first customer is told to arrive immediately after joining", () => {
  assert.equal(messageText({ kind: "JOINED", position: 1 }).title, "You're next");
});
