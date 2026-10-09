# E2E regression check

Compares the rendering and behavior of `HighlightTextView` between two versions of the library (for example `main` vs a feature branch) on the iOS simulator and Android emulator.

## What's here

| File                          | Purpose                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `RegressionApp.tsx`           | Test app with 9 scenarios (Prev/Next to switch): `align`, `align2`, `vertical`, `styles`, `spacing`, `editable`, `readonly`, `autofocus`, `api`. Covers every prop plus controlled input, programmatic text, read-only and autoFocus. `api` (added in Phase 2b, after the original 8 so their screenshots stay comparable) covers number/color props, `editable`, placeholder, `maxLength`, `returnKeyType` + `onSubmitEditing`, focus/blur/selection events, the ref methods and a runtime font change. |
| `flows/regression.yaml`       | Maestro flow (96 steps): screenshots every scenario, types/deletes/sets/clears text and asserts the `onChange` round-trip, checks read-only ignores typing, then drives the `api` scenario (events, ref methods, `maxLength`).                                                                                                                                                                                                                                                                           |
| `flows/autofocus.yaml`        | Opens the autoFocus scenario without typing first, for checking the keyboard state.                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `scripts/diff_screenshots.py` | Pixel-diffs two screenshot folders and writes side-by-side diff images.                                                                                                                                                                                                                                                                                                                                                                                                                                  |

## How to run a comparison

Use Release builds so the JS bundle is embedded (no Metro needed) and both builds run the same way.

1. **Baseline build** (e.g. `main`) in a separate worktree, preferably at a path without spaces (Expo/RN scripts break on spaces):

   ```sh
   git worktree add /tmp/rnht-baseline main
   cp e2e/RegressionApp.tsx /tmp/rnht-baseline/example/src/App.tsx
   cd /tmp/rnht-baseline && yarn install
   ```

2. **New build**: temporarily copy the app into this checkout (don't commit it):

   ```sh
   cp e2e/RegressionApp.tsx example/src/App.tsx   # restore with: git checkout example/src/App.tsx
   ```

3. **Build Release** for each checkout:

   ```sh
   # iOS (from example/ios, after pod install)
   xcodebuild -workspace HighlightTextExample.xcworkspace -scheme HighlightTextExample \
     -configuration Release -sdk iphonesimulator \
     -destination 'generic/platform=iOS Simulator' -derivedDataPath build/dd build

   # Android (from example/android; use x86_64 for an x86_64 emulator)
   ./gradlew assembleRelease -PreactNativeArchitectures=x86_64
   ```

   RN 0.81 (`main` before Phase 1) doesn't compile from source on Xcode 26. Install its pods with prebuilt core instead: `env RCT_USE_RN_DEP=1 RCT_USE_PREBUILT_RNCORE=1 pod install`.

4. **Freeze the status bar** so the clock doesn't show up as a difference:

   ```sh
   xcrun simctl status_bar booted override --time "9:41" --batteryState charged --batteryLevel 100
   adb shell settings put global sysui_demo_allowed 1
   adb shell am broadcast -a com.android.systemui.demo -e command enter
   adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941
   ```

5. **Install and run** each build (uninstall between builds, same bundle id `highlighttext.example`):

   ```sh
   maestro test e2e/flows/regression.yaml -e OUT=$PWD/e2e/shots/ios-base   # then ios-new, android-base, android-new
   ```

6. **Diff**:

   ```sh
   python3 e2e/scripts/diff_screenshots.py e2e/shots/ios-base e2e/shots/ios-new e2e/shots/ios-diff
   ```

## Reading the results

- Expected noise: blinking text cursor on focused screens, and tiny antialiasing differences in React Native's own `<Text>` between RN versions.
- Android `autoFocus`: an emulator with a hardware keyboard only shows Gboard's floating toolbar. To test like a phone, run `adb shell settings put secure show_ime_with_hard_keyboard 1`, run `flows/autofocus.yaml`, then check `adb shell dumpsys input_method | grep mInputShown` (should be `true`).
- Fixed in Phase 2a: Fabric view recycling used to leak native state between views on iOS, so builds before that fix can differ in the `spacing` and `autofocus` scenarios.
- `s9_font_changed` (font switched at runtime) and `s9_font_remounted` (same font after a remount) must show the same highlight. On Android the `sel=` status line differs (a remount puts the cursor at 0), nothing else.
- Android: run with `adb shell settings put secure show_ime_with_hard_keyboard 1` so Gboard shows the full keyboard instead of a floating toolbar that can cover buttons; set it back to `0` afterwards. The Gboard suggestion strip changes between runs and shows up as a difference on focused screens.
- The iOS placeholder is a label that is not in the accessibility tree, so the flow only asserts `Type here` on Android; `s9_placeholder` covers iOS.

## Phase 1 result (2026-09-29)

`main` (RN 0.81.1) vs `chore/phase-1-compat` (RN 0.87.1): both pass all 56 steps on iOS 26.1 and Android 17. Screenshots match except for the expected noise and the pre-existing iOS recycling issue above.

## Phase 2b result (2026-10-09)

`feat/phase-2` before (`*-2a`) vs after (`*-2b`) Phase 2b (JS wrapper, new props, events, ref methods, native fixes): both pass all 96 steps on iOS 26.1 and Android 17 (the 2a builds ran the 61-step flow without S9).

- iOS: S1, S2 and S4 to S8 are pixel-identical. S3 changed as intended: `verticalAlign="center"` is now centered (it used to sit at the top).
- Android: S1 to S5 and the initial S6 are pixel-identical. S6 after typing, S7 and S8 differ only in the Gboard suggestion strip and the cursor blink.
- Runtime font change vs remount: identical on iOS. On Android only the `sel=` status line differs.

## Phase 2 final result (2026-10-09)

`main` (0.1.33, RN 0.81.1, S1 to S8 only) vs `feat/phase-2` (RN 0.87.1, full flow, passes): on iOS S1, S2, S4, S6 initial and S7 are pixel-identical; S3 (centred `verticalAlign="center"`), S5 and S8 (no recycled padding/radius) changed as intended; S6 after typing differs only in the cursor blink. On Android every screen differs only by 1 px anti-aliasing on highlight outlines (one-path fill) and RN's title text. `onFocus`/`onBlur` fire exactly once on iOS and Android (RN 0.87.1) and on Expo SDK 57 Android (RN 0.86.3), including an R8-minified build. For the baseline, the S1 tap and read-only assertion were made optional because `main` exposes no text to Maestro on iOS and fails that check.
