# NebulaBook

NebulaBook is an Android/Jetpack Compose SSH desktop client. It does not render placeholder quotas, telemetry, or fake remote-desktop sessions. Home telemetry is shown only after a verified SSH connection and is sampled from the connected host. The App Deck sends commands over that active SSH session; GitHub and Kaggle status is displayed only after successful provider API responses.

## Build

Requires JDK 17 and Android SDK 35. Build with Android Studio or run:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The GitHub Actions workflow performs these checks and uploads a debug APK artifact.

## Connect a real SSH desktop

1. Open Vault and generate an RSA-4096 keypair.
2. Add the displayed public key to the target Linux account's `~/.ssh/authorized_keys`.
3. Save the SSH hostname, port, username, and a verified `known_hosts` entry for that host. The app enforces strict host-key checking; do not paste an unverified fingerprint.
4. Tap **Connect to SSH desktop**. The app runs its Webtop/Docker provisioning script on that host, forwards local port 3000, and then polls CPU, RAM, and round-trip time using SSH commands.
5. Closing the session stops the Webtop container and closes the SSH tunnel.

This is a generic SSH host workflow. A GitHub token is used to verify the GitHub account and list Codespaces; it does not yield a Codespace SSH endpoint. Cloud Shell, direct Codespaces desktop startup, Rclone configuration upload, and Kaggle render submission are not claimed as complete integrations. Kaggle status lookup is a live read-only API call for an existing kernel. Provider quotas, GPU availability, billing, and free-tier terms are not estimated by the app.

## Security notes

Secrets use Android EncryptedSharedPreferences where available. The emulator fallback is not encrypted and must not be used with production credentials. SSH host keys must be verified out of band before adding them to the vault. Review remote provisioning commands and provider terms before use.
