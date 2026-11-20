# Droibuntu Test107 — foldables and controller navigation

Based on the separate Test106 application, package `org.ubuntuquest.pocket.test`.
Version 107 / `0.1-test107`. Updates Test106 with the same signing certificate;
the original Droibuntu app remains a separate application.

## Foldable screens

- Bundled Jetpack WindowManager 1.3.0 observes actual window folding features.
- Existing controls and desktop views remeasure in place as the window changes.
- A separating vertical hinge uses the larger unobstructed pane (left on a tie).
  Tabletop posture uses the upper pane. A flat, non-occluding fold uses the full window.
- The same hinge avoidance applies to launcher action sheets. This is a safe
  single-pane layout, not a new two-pane desktop or lower-screen touchpad.
- Window resize remains available on devices that do not report fold features.
- Keyboard insets respect the usable pane; the desktop Actions handle stays inside it.
- Observers unregister when a window detaches. Folding does not stop the Ubuntu service.

## Controller navigation

| Control | Native launcher/settings/menu | Ubuntu desktop/application |
| --- | --- | --- |
| D-pad / left stick | Move focus, scroll through controls | Arrow keys |
| A | Activate focused control; open keyboard for text input | Enter |
| B | Hide keyboard, then back/close menu | Escape |
| X | Focused item's long-press actions, where available | Show keyboard |
| Y / Start | Workspace actions | Workspace actions |
| LB / RB | Shift+Tab / Tab | Shift+Tab / Tab |

- White focus outline, device deadzones, dominant-axis selection and bounded repeat.
- Repeat stops on neutral input, touch, focus loss, pause, or controller removal.
- Menu input is scoped to its window. Canceled A presses do not launch an item.
- Existing Quest desktop mouse-button/trigger mappings retain priority when enabled.
- Controller attachment changes no longer recreate native activities.
- Controls are described under Device layout & input.

## Validation and limits

Android resources, Java and DEX compiled; APK alignment and signatures verified.
Signing SHA-256: `239c075d181e1199681e666394dcb3a0b7345c9463f156c77b2f925a5f274f38`.
525 Python tests pass (31 skipped by their environment guards).
4,018 Java policy assertions cover fold geometry and controller axes.
The two live terminal tests now use an explicit prompt, so they work for non-root users.

Physical folding/unfolding, Bluetooth/USB controller behavior, Quest controls,
and live Ubuntu interaction still require device acceptance testing. No hardware
runtime result is claimed. Native focus navigation cannot make arbitrary Ubuntu
applications controller-aware; their keyboard accessibility still determines behavior.

Suggested device checks: cover screen → unfolded → tabletop → flat; fold while
editing/scrolling and with the keyboard visible; open/close an action sheet; hold
a stick while disconnecting, switching apps or opening a modal; check sliders,
search, app launch, B/back and Quest trigger clicks. Confirm Ubuntu stays running.

Sources: Android's [foldable guidance](https://developer.android.com/develop/adaptive-apps/guides/foldables/make-your-app-fold-aware)
and [controller input guidance](https://developer.android.com/games/sdk/game-controller/controller-input).
