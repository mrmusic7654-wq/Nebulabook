# NebulaBook OS

NebulaBook is an Android/Jetpack Compose prototype for a mobile cloud-desktop control plane. It launches in an interactive simulation so the navigation, app deck, Blender-style viewport, shortcut ribbon, trackpad controls, quota dashboard, credential vault, and render queue can be explored without creating cloud resources.

## Build

Open this repository in Android Studio (JDK 17, Android SDK 35) and run the `app` configuration, or run `./gradlew :app:assembleDebug`. The checked-in Gradle 8.10.2 wrapper downloads Gradle on first use. GitHub Actions compiles the debug APK, runs the unit-test task, and publishes `app-debug.apk` as a workflow artifact. This sandbox does not include a JDK/Android SDK, so an Android compile could not be run locally here.

## Important live-mode limitations

- Simulation mode is explicit and performs no network requests, installs, cloud rendering, or quota consumption. Figures shown in the dashboard are illustrative, not account entitlements.
- Provider APIs, plan limits, machine availability, and billing are controlled by Google, GitHub, and Kaggle. Verify current terms and limits with each provider.
- A bearer token alone does not provide generic SSH connection details. Google Cloud Shell's environment API does not establish this app's assumed SSH host/port/username contract. GitHub Codespaces likewise requires an actual codespace and its supported connection workflow. The UI therefore avoids pretending that selecting Live mode started a VM.
- The low-level HTTP/SSH utilities and WebView bridge are building blocks; production live startup still needs provider-specific supported auth/host discovery, Android OAuth configuration, host-key verification, secure secret lifecycle handling, and end-to-end testing. The demo renderer intentionally does not submit Kaggle jobs or claim GPU access.
- A wake lock is held only while the foreground session service is active. Android notification runtime permission and foreground-service policy requirements must be handled in a production release.

Secrets use Android encrypted shared preferences where the platform supports them, with an explicitly documented unencrypted emulator fallback. Do not use the fallback for real credentials.
