# NebulaBook

NebulaBook is an Android/Jetpack Compose remote development client. There is no demo desktop or fabricated quota, RAM, storage, CPU, ping, or render progress. Host telemetry appears only after an SSH connection and successful host samples. Provider data is shown only after a successful API response.

## Build

Requires JDK 17 and Android SDK 35. Build with Android Studio or:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

GitHub Actions runs the debug compile and unit tests, then uploads the APK artifact.

## Connect a regular SSH Linux host

1. In **Settings**, generate an RSA-4096 keypair.
2. Add the displayed public key to the Linux account's `~/.ssh/authorized_keys`.
3. Save the hostname, port, username, and a verified `known_hosts` entry. Host-key checking is strict; the app will not silently trust an unknown host.
4. On Home, tap **Connect to SSH desktop**. The app runs the Webtop/Docker setup script, forwards `127.0.0.1:3000`, and then reads CPU, RAM, and command round-trip time from that SSH host.
5. The App Deck sends its install commands over the connected SSH session. **Close Lid** tries to stop the Webtop container and closes the tunnel; the UI reports when remote shutdown cannot be confirmed.

## Connect Google Cloud Shell

1. Generate a keypair in Settings.
2. Paste a current user OAuth access token with Cloud Shell API authorization (the `cloud-platform` scope) into **Google OAuth Access Token**. Token pasting is currently supported; in-app Google sign-in and refresh are not yet implemented. See the [Cloud Shell REST API](https://cloud.google.com/shell/docs/reference/rest) and its [environment resource](https://cloud.google.com/shell/docs/reference/rest/v1/users.environments).
3. Tap **Start / Connect Google Cloud Shell**. NebulaBook calls the Cloud Shell REST API, registers the public key, starts the environment if needed, and reads the actual SSH host, port, username, and running state from the environment response.
4. For safety, the first connection pauses until the host key is trusted. From a trusted computer, run the `ssh-keyscan -p PORT HOST` command shown by the app, verify the fingerprint independently, then save the resulting line under **SSH known hosts**.
5. Tap the Cloud Shell connect action again. NebulaBook then establishes verified SSH, provisions Webtop if the host supports it, and begins reporting actual SSH telemetry.

Cloud Shell's lifecycle remains managed by Google; closing NebulaBook's desktop tunnel is not a promise that the Cloud Shell VM itself has been stopped.

## Connect GitHub Codespaces

1. In Settings, paste a fine-grained GitHub token with Codespaces read/write permission and press **Verify token & load Codespaces**.
2. Open **Codespaces**, select an existing Codespace, and review the current machine and available machine types returned by GitHub.
3. **Start · 2 Core** selects an available 2-vCPU machine from GitHub's machine API, applies it to a stopped Codespace if needed, starts it, and polls its actual state. If the Codespace is already running on a different size, stop it first. NebulaBook does not silently fall back to a larger machine.
4. **Open Codespace in browser** opens GitHub's returned editor URL. This is Codespaces editor access—not an SSH desktop connection. A PAT alone does not give the app a Codespace port-forward tunnel or Codespace-side `GITHUB_TOKEN`. See GitHub's [machine type API](https://docs.github.com/en/rest/codespaces/machines) and [port-forwarding guide](https://docs.github.com/en/codespaces/developing-in-a-codespace/forwarding-ports-in-your-codespace).

RAM and machine-storage figures shown for Codespaces come from GitHub's machine metadata. Confirm account billing and included usage in GitHub; Codespaces are not guaranteed to be free. Direct Codespaces Webtop streaming, automatic Codespace creation, and port-forward authentication are not implemented.

## Remaining boundaries

Kaggle integration is read-only status lookup for an existing kernel; job submission and downloads are not implemented. Rclone configuration is not uploaded. The app does not claim provider quota or GPU availability. Review provider billing and terms before starting resources.

## Security

Secrets use Android EncryptedSharedPreferences where available. The emulator fallback is unencrypted and must not be used with production credentials. Verify SSH host keys out of band. Google OAuth tokens and GitHub PATs are pasted by the user and are not automatically refreshed. Remote provisioning commands should be reviewed before use.
