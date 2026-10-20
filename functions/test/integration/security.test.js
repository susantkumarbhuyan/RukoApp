import test, { before, after } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { initializeTestEnvironment, assertFails, assertSucceeds } from "@firebase/rules-unit-testing";
import { doc, getDoc, setDoc, collection, getDocs, query, where, orderBy } from "firebase/firestore";

if (!process.env.FIRESTORE_EMULATOR_HOST) throw new Error("Emulator required");
let env;
before(async () => {
  env = await initializeTestEnvironment({ projectId: "demo-ruko",
    firestore: { rules: readFileSync(new URL("../../../firestore.rules", import.meta.url), "utf8") } });
  await env.withSecurityRulesDisabled(async ctx => {
    const db = ctx.firestore();
    await setDoc(doc(db, "queues/securityQ"), { storeId: "business", name: "Queue", schemaVersion: 2 });
    await setDoc(doc(db, "activeMemberships/alice"), { queueId: "securityQ", ticketId: "ticketA", active: true });
    await setDoc(doc(db, "customers/alice"), { id: "alice", name: "Alice", email: "alice@example.com" });
    await setDoc(doc(db, "queues/securityQ/tickets/ticketA"), { userId: "alice", status: "JOINED", sequence: 1 });
    await setDoc(doc(db, "pushDevices/private"), { uid: "alice", token: "sensitive" });
  });
});
after(async () => { await env.cleanup(); });
const user = uid => env.authenticatedContext(uid, { email: uid + "@example.com" }).firestore();

test("queue writes and membership writes are server-only, including for owners", async () => {
  await assertFails(setDoc(doc(user("business"), "queues/securityQ"), { storeId: "business" }));
  await assertFails(setDoc(doc(user("alice"), "activeMemberships/alice"), { active: true }));
  await assertFails(setDoc(doc(user("alice"), "queues/securityQ/tickets/ticketB"), { userId: "alice" }));
});

test("ticket streams require queue membership or ownership", async () => {
  const tickets = db => query(collection(db, "queues/securityQ/tickets"),
    where("status", "==", "JOINED"), orderBy("sequence"));
  await assertSucceeds(getDocs(tickets(user("alice"))));
  await assertSucceeds(getDocs(tickets(user("business"))));
  await assertFails(getDocs(tickets(user("stranger"))));
  await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), "queues/securityQ")));
});

test("customer data and tokens are private", async () => {
  await assertSucceeds(getDoc(doc(user("alice"), "customers/alice")));
  await assertSucceeds(getDoc(doc(user("business"), "customers/alice")));
  await assertFails(getDoc(doc(user("stranger"), "customers/alice")));
  await assertFails(getDoc(doc(user("alice"), "pushDevices/private")));
  await assertFails(getDocs(collection(user("business"), "customers")));
});

test("profiles reject passwords, forged identities and self-verification", async () => {
  const db = user("newuser");
  await assertSucceeds(setDoc(doc(db, "customers/newuser"),
    { id: "newuser", name: "New user", email: "newuser@example.com" }));
  await assertFails(setDoc(doc(db, "customers/newuser"),
    { id: "newuser", name: "New user", email: "newuser@example.com", password: "never-store" }));
  await assertFails(setDoc(doc(db, "customers/someoneelse"),
    { id: "someoneelse", name: "Fake", email: "newuser@example.com" }));
  await assertFails(setDoc(doc(db, "stores/newuser"), {
    id: "newuser", name: "Owner", email: "newuser@example.com", businessName: "Salon",
    businessType: "Salon", phoneNumber: "", address: "", isVerified: true
  }));
});

test("leaving revokes access to other customers and their queue stream", async () => {
  await env.withSecurityRulesDisabled(async ctx => {
    await setDoc(doc(ctx.firestore(), "activeMemberships/alice"),
      { queueId: "securityQ", ticketId: "ticketA", active: false });
  });
  await assertFails(getDocs(collection(user("alice"), "queues/securityQ/tickets")));
  await assertSucceeds(getDoc(doc(user("alice"), "queues/securityQ/tickets/ticketA")));
  await assertFails(getDoc(doc(user("business"), "customers/alice")));
});
