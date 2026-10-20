import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { setGlobalOptions } from "firebase-functions/v2";
import { createService } from "./service.js";
import { QueueError } from "./domain.js";
import { notificationWorker } from "./notifications.js";

initializeApp();
setGlobalOptions({ region: "asia-south1", maxInstances: 5, minInstances: 0, concurrency: 20 });
const db = getFirestore();
const service = createService(db);
const deliver = notificationWorker(db, getMessaging());
const callable = operation => onCall({
  enforceAppCheck: process.env.FUNCTIONS_EMULATOR !== "true",
  timeoutSeconds: 60
}, async request => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Please sign in");
  try { return await operation(request.auth.uid, request.data ?? {}); }
  catch (error) {
    if (error instanceof QueueError) throw new HttpsError(error.code, error.message);
    console.error("Queue operation failed", { code: error.code ?? "unknown" });
    throw new HttpsError("internal", "The request could not be completed. Please retry.");
  }
});
export const rukoQueueCommand = callable(service.command);
export const rukoSaveQueue = callable(service.saveQueue);
export const rukoDeleteQueue = callable(service.deleteQueue);
export const rukoRegisterDevice = callable(service.registerDevice);
export const rukoUnregisterDevice = callable(service.unregisterDevice);

export const deliverQueueNotification = onDocumentCreated({
  document: "notificationJobs/{jobId}", retry: true, timeoutSeconds: 120
}, event => deliver(event.params.jobId));

export const retryQueueNotifications = onSchedule({
  schedule: "every 5 minutes", timeoutSeconds: 120, maxInstances: 1
}, async () => {
  const due = await db.collection("notificationJobs")
    .where("status", "in", ["PENDING", "PROCESSING"])
    .where("nextAttemptAt", "<=", Date.now()).limit(100).get();
  // Bound parallel FCM requests; never send messages directly from a mobile client.
  for (let i = 0; i < due.docs.length; i += 5) {
    await Promise.all(due.docs.slice(i, i + 5).map(doc => deliver(doc.id)));
  }
});
