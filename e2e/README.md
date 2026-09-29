# E2E regression check

Compares the rendering and behavior of `HighlightTextView` between two versions of the library (for example `main` vs a feature branch) on the iOS simulator and Android emulator.

## What's here

| File                          | Purpose                                                                                                                                                                                                                        |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `RegressionApp.tsx`           | Test app with 8 scenarios (Prev/Next to switch): `align`, `align2`, `vertical`, `styles`, `spacing`, `editable`, `readonly`, `autofocus`. Covers every prop plus controlled input, programmatic text, read-only and autoFocus. |
| `flows/regression.yaml`       | Maestro flow (56 steps): screenshots every scenario, types/deletes/sets/clears text and asserts the `onChange` round-trip, checks read-only ignores typing.                                                                    |
| `flows/autofocus.yaml`        | Opens the autoFocus scenario without typing first, for checking the keyboard state.                                                                                                                                            |
| `scripts/diff_screenshots.py` | Pixel-diffs two screenshot folders and writes side-by-side diff images.                                                                                                                                                        |

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
- Known pre-existing iOS issue (see `PLAN.md`, Phase 2): Fabric view recycling leaks native state between views, so the `spacing` scenario can differ between builds depending on which view gets reused.

## Phase 1 result (2026-09-29)

`main` (RN 0.81.1) vs `chore/phase-1-compat` (RN 0.87.1): both pass all 56 steps on iOS 26.1 and Android 17. Screenshots match except for the expected noise and the pre-existing iOS recycling issue above.
