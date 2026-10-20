Experimental ARM64 build for Android 8.0/API 26 or newer. Includes all changes since Test102, including Test103's background Ubuntu launch improvements.

- Ubuntu apps reuse the running session, with automatic desktop fallback when window detection fails.
- Compact, themed Actions handle frees the reserved bottom strip; everyday actions appear first, with advanced touch and Quest controls under More controls.
- Wallpaper sampling follows actual window positions during rotation and maximization.
- Window recovery, per-app placement memory for portrait/landscape, and one session-only pair of open Ubuntu windows that rotates between side-by-side and stacked layouts. Split ratios are adjustable; application minimum-size constraints still apply.
- More guarded Undo coverage for layout moves, toolbar changes and widget resizing.
- Clear launch stages and a themed recovery sheet with Retry, Open desktop and Copy diagnostics. Retry preserves the original app action, document, URL and recent item.

69 focused checks passed. Resource/Java/D8 compilation, APK integrity, alignment, packaged contents and signing certificate continuity verified. Phone and Quest runtime/visual testing remains pending.

Install over Test102 or Test103 to retain app data. The signing certificate is unchanged. This experimental build is marked Latest so the existing in-app updater can find it; downloads and Android installation confirmation remain manual.

Assets include the signed APK, corresponding public source with private signing material excluded, and SHA256SUMS. The repository's automatic Source code ZIP contains the download-site repository; use Droibuntu-Test104-source.zip for the Android application source.
