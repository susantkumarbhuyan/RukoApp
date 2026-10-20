# RUKO trusted backend and push setup

## What this change implements

- A single authoritative Firestore queue, with live ticket snapshots.
- Authenticated/App Check protected callable functions in `asia-south1`.
- Transactions serialize capacity, daily limits, token sequence, cross-queue membership,
  customer arrival, owner-only service actions, service history, and notification jobs.
- Stable persisted command IDs make retries idempotent across app restarts.
- Private device-token bindings, account switching, logout detach, and token invalidation.
- A notification outbox, delivery leases, bounded retries, invalid-token cleanup, and
  scheduled recovery after worker crashes.
- Android notification channels, foreground display, background FCM notification display,
  token-refresh work, and validated notification-tap navigation.
- iOS native Firebase SDKs through SPM, Firebase initialization, App Check, manual APNs
  token mapping, foreground presentation, notification taps, and signing entitlements.

FCM/APNs delivery is best effort. It is not exactly once, and it cannot guarantee arrival
before a customer's turn. The live queue remains the authoritative status.
Notification text contains no names, contact details, or medical information.

## Required account configuration

The code is prepared but has NOT been deployed to a Firebase project.
Firebase Cloud Functions deployment requires the Blaze plan. Do not assume that usage
will always cost zero; review usage, set budget alerts, and keep the instance limits.

1. Revoke the service-account key previously committed to the public repository.
   It was removed from this branch, but remains in Git history. Do not create a new
   service-account file in either mobile app.
2. Enable email/password Firebase Authentication and Cloud Firestore for the project
   used by the existing Android and iOS configuration files.
3. Firebase Console → App Check: register Android with Play Integrity and iOS with
   App Attest. Register your own locally generated debug tokens for development.
   Production callable functions require App Check; debug tokens are credentials.
4. Apple Developer account: enable Push Notifications for the explicit bundle ID
   `com.ruko.system`. In Firebase → Project settings → Cloud Messaging upload an
   APNs authentication key and enter its Key ID and Team ID. Keep the .p8 key private.
5. Set your Apple team in `iosApp/Configuration/Config.xcconfig`; the app bundle ID
   now matches `GoogleService-Info.plist`. Use a physical iPhone for APNs testing.
6. Resolve Xcode's Swift packages. Kotlin and Xcode targets are aligned for ARM64 devices and simulators. Firebase iOS 11.8.0 matches the native version used
   by GitLive 2.3.0. CocoaPods integration was removed to retain the existing direct
   Kotlin/Xcode framework build; do not add a second Firebase installation method.

The callable region is fixed to `asia-south1` in both Kotlin and the backend.
If you choose another region, change both before building/deploying.

## Migration and rollout

This is a schema cutover, not a transparent migration of active legacy queues.
Do not deploy the new default-deny rules while customers still use the old client.

1. Test against the Firebase Emulator Suite or a separate staging project first.
2. Stop admissions and drain the old Realtime Database queues.
3. Back up the old Firestore and Realtime Database data with your normal administrator tools.
4. Confirm old profile documents have no stored passwords. The new app writes
   `customers/{firebaseUid}` and `stores/{firebaseUid}`. Migrate older `users`/`owners`
   collections and generated-ID references deliberately if present.
5. Archive or retire legacy queue configuration documents. Recreate each queue through
   the new client, which calls `rukoSaveQueue` and creates `schemaVersion: 2` plus runtime state.
   Legacy queues are explicitly rejected rather than silently losing active tickets.
6. Release the new clients and deploy functions, indexes, and rules in a coordinated
   maintenance window. Old clients cannot write under these rules.
7. The Realtime Database rules in this change deny all reads/writes because the new queue
   flow no longer uses it. Review other consumers before deploying those database rules.

The backend allows at most 200 active tickets per queue. Average service duration is
entered in minutes by owners and stored in milliseconds. Daily limits use the business
timezone (Asia/Kolkata by default). This release supports one service counter per queue.
No-show removal, skip/reorder, appointments, multiple staff, and automatic daily closing
remain separate product features.

## Local verification

Requires Node 22, Java 21 for the emulators, Android SDK/JDK 17, and macOS/Xcode for iOS.

```sh
npm --prefix functions install
npm --prefix functions run check
npm --prefix functions test
npx firebase-tools@14.12.0 emulators:exec --project demo-ruko --only firestore,auth "npm --prefix functions run test:emulator"
./gradlew :composeApp:testDebugUnitTest :composeApp:assembleDebug
xcodebuild -resolvePackageDependencies -project iosApp/iosApp.xcodeproj -scheme iosApp
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
```

The emulator integration tests refuse to run without FIRESTORE_EMULATOR_HOST, and
use the reserved `demo-ruko` project ID. They never target production.
The GitHub Actions workflow runs these checks without deployment credentials.
Commit the npm lockfile generated during your first successful dependency install
before treating dependency resolution as reproducible.

## Deploy (administrator action, not performed here)

After staging checks, key revocation, billing, App Check, APNs, and migration are complete:

```sh
npx firebase-tools@14.12.0 deploy --project YOUR_FIREBASE_PROJECT_ID --only functions,firestore:rules,firestore:indexes
# Only after reviewing existing RTDB consumers:
npx firebase-tools@14.12.0 deploy --project YOUR_FIREBASE_PROJECT_ID --only database
```

Create TTL policies for the `expiresAt` field in the `receipts` and
`notificationJobs` collection groups, according to your retention policy.
History is retained until an administrator applies the business retention policy.
Investigate notification jobs with status `DEAD`; after fixing the cause, an
administrator can reset status to PENDING and nextAttemptAt to the current epoch milliseconds.

## Device acceptance tests

- Two customers join at once; capacity and order remain correct.
- One account joins different queues from two devices; only one membership succeeds.
- Retry join and completion after a timeout; no extra ticket or history record appears.
- Customer checks in; owner gets the arrival alert and sees the check-in.
- Complete one service; waiting customers get a progress notification and live position.
- Verify foreground/background/cold-start taps on Android and a physical iPhone.
- Deny permission: queue features still work, with no push-delivery promise.
- Refresh tokens, switch accounts, and log out: old-account messages must not be delivered
  after token detach/invalidation has completed. Offline logout cannot synchronously
  guarantee server-side token invalidation; test reconnection and account switching.
- Simulate a transient messaging error and a crashed delivery lease; retries recover.
- Verify rules deny direct queue changes, forged profiles, and access by unrelated users.

## References

- https://firebase.google.com/docs/functions/get-started
- https://firebase.google.com/docs/firestore/manage-data/transactions
- https://firebase.google.com/docs/cloud-messaging/ios/client
- https://firebase.google.com/docs/app-check
- https://github.com/GitLiveApp/firebase-kotlin-sdk/tree/v2.3.0

Build/device verification status and CI results are recorded in the pull request.
