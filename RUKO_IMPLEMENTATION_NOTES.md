# RUKO queue and UI update

This branch implements the customer and owner UI and repairs the existing client queue flow.
It is a draft for review, not a verified production release.

## Changes

- Shared teal Material 3 colors, including dark mode.
- Owner dashboard with queue cards, business context, and creation action.
- Customer home, join preview, and live position screen.
- Customer check-in and owner start/complete service actions.
- Actual membership-based queue positions; milliseconds throughout the data layer.
- Wait estimates exclude the customer's own service and deduct elapsed service at the front.
- Stable UID entry keys; legacy push-key entries can still be left.
- Explicit service start timestamps rather than counting waiting as service.
- Completion history uses visit-specific document IDs.
- Firebase UID profiles, corrected login account types, missing compatibility models restored.
- Passwords excluded from profile serialization and future local profile writes.
- QR payload validation, visible scan errors, and malformed payload handling.
- Forms use minutes, show errors, scroll, and navigate only after edit success.
- Internet/notification manifest declarations and malformed dependency version repaired.
- Exposed service-account file removed from this branch and ignored for future commits.

## Immediate credential action

The public repository contained a service-account private key at
`composeApp/src/androidMain/assets/key.json`. Revoke that key in Google Cloud IAM.
Removing the file does not revoke it or remove it from existing Git history.
No replacement server credential belongs in an Android or iOS app.

## Verification status

The source was reviewed through the GitHub connection. A shell, Gradle runtime,
Android emulator, and Xcode were unavailable during this change.
The Kotlin tests below were added but have NOT been compiled or executed.

On a configured development machine run:

```sh
./gradlew :composeApp:testDebugUnitTest :composeApp:assembleDebug
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Open the iOS app in Xcode and verify Firebase initialization, native Firebase
dependencies, camera permissions, and device deployment. The current iOS Swift entry
point does not initialize Firebase and the native dependency configuration must be
completed before an iOS end-to-end test can pass.

## Two-device acceptance checks (not executed)

1. Sign up as owner; create a queue with ten-minute service duration and capacity five.
2. Sign up two separate customers and scan the generated QR; positions must be one and two.
3. Repeated join taps must produce only one active row for that customer.
4. Check in the first customer; the owner sees checked-in status.
5. Start service; the second customer's estimate decreases with elapsed service.
6. Complete service; the second customer moves to position one.
7. Leave the queue and verify membership and local joined-queue state clear.
8. Retry with a malformed QR, missing customer profile, full queue, paused queue, and network loss.
9. Reopen the app, log out/in, test large fonts, and repeat on Android and iOS.
10. Inspect stored profiles to confirm new password fields are absent.

## Remaining work before release

- Trusted backend operations and tested database security rules. Client-side ownership
  checks improve normal app behavior but are not an authorization boundary.
- Atomic queue admission/capacity checks, single active membership across devices, and
  server timestamps. The current client preflight checks can race under simultaneous joins.
- Completion history currently spans Realtime Database and Firestore; it is not atomic.
  Add a durable backend outbox/reconciliation mechanism before relying on history for accounting.
- Retry/concurrent service operations need server-side idempotency and transaction tests.
- Closed queue changes are fetched when screens load; live membership is observed, but
  business settings are not currently streamed into already-open screens.
- Current wait estimates assume one service counter. Daily limits, schedules, no-shows,
  skipped customers, multiple staff, and appointments need explicit business rules.
- Push notifications are NOT implemented by this branch. Add token registration/refresh,
  a trusted FCM sender, deduplication, and Android/iOS foreground/background handlers.
  Configure APNs capabilities/credentials and permission handling for iOS.
- Do not put service-account keys or FCM server credentials back into the client.
- Customer restoration currently uses the local joined-queue pointer; cross-device
  restoration requires an authoritative per-user active-membership record.
- Audit older Firebase documents for plaintext passwords and generated IDs, and migrate
  legacy profile collections if they used names other than `customers`/`stores`.
- This branch retains newer Customer/BusinessAccount models and adds compatibility
  User/Owner models used by existing forms. Consolidate the schema after migration.
- Existing queue durations stored as seconds need explicit migration; they cannot be
  safely identified from their numeric values alone.

## Regression tests added

`QueueTimingTest`: membership lookup, front-of-line zero wait, elapsed service,
service overrun, and rounding.
`RukoQrTest`: generated/scanned payloads, malformed JSON, unsafe IDs, oversized input.
