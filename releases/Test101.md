# Droibuntu Test101

Experimental ARM64 Android build for Android 8.0/API 26 or newer.

- Fixes desktop shrinking when the keyboard opens. The keyboard overlays the lower desktop while size, zoom and pan stay unchanged.
- Includes touch-home improvements, Quest desktop launch and scrolling changes, default wallpaper choices, and visible vertical desktop controls.

Build, signing, package-content and focused regression checks passed. Phone and Quest behavior still needs device testing.

Downloads include the signed APK, corresponding public source and SHA256SUMS. The private signing key is excluded from public source.

For in-app updates: Settings → Updates → Droibuntu APK updates → Set official GitHub repository → `Jagger15/Droibuntu`. Checks are manual; Android confirms installation. Test101 is the starting release; an installed Test101 requires a newer APK before an update can be installed.
