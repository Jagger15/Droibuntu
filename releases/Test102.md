Experimental ARM64 Android build for Android 8.0/API 26 or newer.

- Automatic portrait/landscape rotation across Droibuntu Android pages and custom Ubuntu tools. Native pages retain their live controls; narrow settings rows stack vertically. Android rotation lock is respected; Quest follows the OS-managed panel shape. Manual orientation locks remain available.
- Addresses the Ubuntu top bar crossing application windows after rotation by reanchoring panels after resize and window-manager reconfiguration.
- Recovers partially clipped Ubuntu windows after screen changes while retaining maximized states and running applications.
- Adds a small Home update dot for newer published APK releases. It opens the existing update screen. Release metadata checks occur when Home becomes visible, at most every six hours, and can be disabled. No pop-ups, automatic APK downloads or installations.
- Uses Jagger15/Droibuntu as the default update source, preserving any source already configured.
- Retains Test101's keyboard overlay fix so opening the keyboard does not shrink the desktop.

52 focused Python checks, two Java policy checks, resource/Java/D8 compilation, signing, alignment and packaged-content verification passed. Phone and Quest visual/device testing remains pending. Fixed-layout third-party Ubuntu apps may retain their own minimum-size constraints.

Includes the signed APK, corresponding public source (private signing key excluded), and SHA256SUMS. Install over Test101 to preserve app data; Android asks you to confirm installation. The signing certificate matches Test101.
