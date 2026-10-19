# Droibuntu

An experimental Android launcher and local Ubuntu desktop for ARM64 devices,
including phones, tablets and Quest headsets.

[Download the latest release](https://github.com/Jagger15/Droibuntu/releases/latest) · [Report an issue](https://github.com/Jagger15/Droibuntu/issues)

## Updates from inside Droibuntu

1. Open Settings → Updates → Droibuntu APK updates (optional).
2. Choose Set official GitHub repository and enter `Jagger15/Droibuntu`.
3. Choose Check stable release, then Download & verify APK when an update is available.
4. Choose Install verified APK and confirm in Android's installer.

Checks are manual in the current app. It verifies GitHub's SHA-256 asset digest,
package name, newer version code and matching signing certificate. Existing
Test101 installations need a newer APK to install an update.

## Project status

Experimental. Build checks are not device compatibility guarantees. Current
binaries target ARM64 and Android 8/API 26 or newer. Source and third-party
license material accompany each release. The private release signing key is
never published. GPL-3.0-or-later project; bundled dependencies retain their licenses.

## Publishing releases

Publish exactly one APK asset per regular latest release, plus its public source
archive and SHA256SUMS. The current app ignores draft and prerelease entries.
Keep test builds clearly labelled experimental. Reuse the private signing key and
increment Android versionCode for every update.

See SETUP.md for first-time repository/Pages setup and tools/publish_release.py
for a draft-first GitHub CLI publishing helper.
