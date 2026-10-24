# S.A.H.I.L. Launcher — cloud-build starter

This is an early native Android TV launcher prototype. It installs as a separate app and does **not** disable the stock Google TV launcher.

## Build without installing Android Studio (GitHub Actions)
1. Sign in to GitHub and create a **new private repository** (recommended).
2. On the new repository page, choose **Add file → Upload files**.
3. Extract this ZIP on your laptop and upload the contents of the `S.A.H.I.L.Launcher` folder, including the hidden `.github` folder. If the browser upload does not show `.github`, create `.github/workflows/android-build.yml` using the file included here.
4. Commit the files to the `main` branch.
5. Open the repository's **Actions** tab. Select **Build S.A.H.I.L. Launcher APK**.
6. If prompted, enable GitHub Actions. Choose **Run workflow** if no build started automatically.
7. Wait for the run to finish successfully. Open that run and download the artifact named `jarvis-launcher-debug-apk`.
8. Extract the artifact ZIP to get the `.apk`.

The workflow uses Java 17 and Gradle 8.9 in GitHub's cloud runner. It does not require Android Studio on your laptop.

## Install on TV
Installing the APK is a separate step. You can use an Android TV-compatible file transfer / APK installer, or use ADB from a laptop if you are comfortable with developer settings. Enable only the needed installation/debugging access and turn it off afterwards. Never disable the stock Google TV launcher or system packages.

## Important limitations
- This is a prototype, not a finished launcher.
- The APK is a debug build, not a Play Store release.
- It is not set up to become the default Home handler; test it as an ordinary app first.
- Some apps may not expose a compatible TV launch activity.
- Keep the original launcher enabled and test HDMI, Chromecast, remote navigation, and app launching before considering any default-home changes.
- If the cloud build fails, open the failed run and share the first relevant error lines so the project can be corrected.


Branding update: application label and launcher icon are now S.A.H.I.L.; the package/application ID is intentionally unchanged so this build can update the existing installation.
