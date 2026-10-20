# RUKO implementation status

The shared UI and queue fixes are in this branch. The follow-up now adds the trusted
Firestore backend, security rules, concurrency controls, notification delivery code,
and Android/iOS Firebase integration. See [BACKEND_SETUP.md](BACKEND_SETUP.md) for
architecture, account setup, migration, verification, and deployment.

The source is not a deployed service. Do not publish clients or deploy the new rules
until staging tests, legacy queue migration, App Check, APNs, and credential revocation
are complete. Device push tests need the project's Firebase/Apple configuration.

The original public service-account private key was deleted from this branch and
ignored. Revoke it in Google Cloud IAM; Git history still contains it.

Ten pure backend policy test bodies passed in the available JavaScript runtime.
Node/emulator, Gradle, Xcode, and physical-device results are reported separately in
the pull request; a local shell execution environment was unavailable.
