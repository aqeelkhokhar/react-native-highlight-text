# react-native-highlight-text-view — Revival Plan

Status snapshot (2026-09-29): published `0.1.33` (2026-01-09), built against RN 0.81.1.
Latest ecosystem: React Native 0.87.1 (0.88 RC), Expo SDK 57.
Downloads: ~4.3k last 12 months, 90 last month.

Legend: `[ ]` todo · `[x]` done · `[~]` in progress

---

## Phase 1 — Compatibility (target release `0.2.0`)

Goal: builds and runs cleanly on the latest RN (CLI) and Expo (dev build) on iOS and Android, with no deprecated APIs on the hot path.

### Metadata / docs quick wins

- [x] README install command installs the wrong package (`react-native-highlight-text` → `react-native-highlight-text-view`)
- [x] Fix `.git.git` URLs in `package.json` (`repository`, `bugs`, `homepage`) and `HighlightText.podspec` (`source`)
- [x] `peerDependencies`: `react-native >= 0.76` (Fabric-only), `react >= 18`
- [x] Better `description` + `keywords` for npm search
- [x] README: compatibility table (RN / New Architecture / Expo Go ❌ / Expo dev build ✅) + Expo install instructions

### JS / TypeScript

- [x] Replace deep import `react-native/Libraries/Types/CodegenTypes` with root `CodegenTypes` export (RN 0.87 sets `types: null` for `Libraries/*`)
- [x] Import `codegenNativeComponent` from `react-native` root (already) and delete `src/codegen-types.d.ts` shim

### Android

- [x] Replace deprecated `RCTEventEmitter` (`getJSModule(...).receiveEvent`) with `UIManagerHelper.getEventDispatcherForReactTag` + `OnChangeEvent` class; package moved to `BaseReactPackage`
- [x] Java/Kotlin JVM target 17 (was 1.8) to avoid "Inconsistent JVM-target" errors
- [x] Library `build.gradle` aligned with current create-react-native-library template (Java 17, compileSdk 36 default, no targetSdk pin, AGP 9 built-in Kotlin guard). _AGP 8.7.2 classpath kept, as the upstream template still does_
- [x] Replace deprecated `InputMethodManager.SHOW_FORCED` (now flag `0`)

### iOS

- [x] Fix podspec `source` URL
- [x] Verify build on Xcode 26 / iOS 26 SDK with RN 0.87

### Tooling / example / CI

- [x] Upgrade example app to RN 0.87.x (CLI)
- [x] Add Expo SDK 57 example (`example-expo/`, dev build via `expo prebuild`) to prove Expo compatibility
- [x] Upgrade `react-native-builder-bob` 0.40 → 0.43
- [x] Build library (`bob build`), typecheck, lint green
- [x] Build example on iOS + Android locally
- [x] CI: checkout v5, Xcode 26, new `build-expo-android` job

### Phase 1 verification (2026-09-29)

| Target                                                                | Build                                   | Runtime (render + typing + `onChange`) |
| --------------------------------------------------------------------- | --------------------------------------- | -------------------------------------- |
| CLI example, RN 0.87.1, iOS 26.1 sim (Xcode 26.5)                     | ✅ Debug                                | ✅ Maestro: counter 12 → 15            |
| CLI example, RN 0.87.1, Android 17 / API 37 emu (AGP 9, Gradle 9.4.1) | ✅ Debug + Release                      | ✅ counter matches field text          |
| Expo SDK 57 (RN 0.86.3), iOS 26.1 sim                                 | ✅ Release (prebuild, no config plugin) | ✅ renders + edits                     |
| Expo SDK 57 (RN 0.86.3), Android 17 emu                               | ✅ Release                              | ✅ renders                             |
| `expo-doctor`                                                         | 21/21 checks passed                     |                                        |

Notes found during verification:

- Expo example needed `react-native.config.js` pointing at the repo root (same as CLI example) — example setup only, not a consumer issue.
- Expo SDK 57 generated iOS scripts break when the project path contains a space (`EXConstants` app.config phase, bundle phase). Upstream Expo bug; worked around locally in gitignored generated files only.
- Android: faint vertical seams between words in the highlight background (pre-existing) → Phase 2.
- Android `scaledDensity` deprecation warnings (pre-existing) → Phase 2.

### Phase 1 regression check: old (`main`, RN 0.81.1) vs new (this branch, RN 0.87.1)

Harness saved in [`e2e/`](e2e/README.md). Same test app (8 scenarios covering every prop: alignment, vertical alignment, fonts/weight/letter spacing, line height, padding, insets, radius, editable controlled input, read-only, autoFocus) built in Release for both versions, driven by the same 56-step Maestro flow, screenshots pixel-diffed.

|                                         | Flow (56 steps: typing, delete, programmatic set, clear, read-only, autoFocus) | Screenshots vs old                                                                                               |
| --------------------------------------- | ------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------- |
| iOS 26.1 sim, old                       | ✅ pass                                                                        | —                                                                                                                |
| iOS 26.1 sim, new                       | ✅ pass                                                                        | identical in 7/8 scenarios (only clock/cursor blink); S5 differs because of the pre-existing recycling bug below |
| Android 17 emu, old                     | ✅ pass                                                                        | —                                                                                                                |
| Android 17 emu, new                     | ✅ pass                                                                        | identical in all 8 (only RN's own title-text antialiasing + cursor blink)                                        |
| Android autoFocus (`SHOW_FORCED` → `0`) | keyboard shown (`mInputShown=true`) in both                                    | identical                                                                                                        |

Also found: `main` (RN 0.81.1) **does not build on Xcode 26** from source (`fmt` consteval error); only with prebuilt RN core. The upgrade fixes this.

---

## Phase 2 — Developer experience (target release `1.0.0`)

Goal: an API that feels like a normal React Native component, plus the TextInput features people expect.

- [ ] JS wrapper component: accept `number | string` for all numeric props and `ColorValue` for colors; convert to strings for native (non-breaking — string props keep working)
- [ ] Events: `onFocus`, `onBlur`, `onSubmitEditing`, `onSelectionChange`
- [ ] Props: `placeholder`, `placeholderTextColor`, `maxLength`, `autoCapitalize`, `keyboardType`, `returnKeyType`, `editable` alias for `isEditable`
- [ ] Ref commands: `focus()`, `blur()`, `clear()`, `setText()` (codegen `codegenNativeCommands`)
- [ ] Fix the "`key={fontFamily}` required" workaround natively (re-measure on font change)
- [ ] **iOS state bugs (pre-existing, found in regression check):** `updateProps` only applies props that differ from the previous props, so
  - Fabric view recycling leaks native state between views (e.g. `highlightBorderRadius`/padding of a previous screen appears on a new view) — fix with `+shouldBeRecycled NO` or a full reset in `prepareForRecycle`
  - `isEditable={false}` is ignored on a freshly mounted view (codegen default `false`, native default `YES`) — only works on recycled views today
  - Default `textAlign` on iOS fresh views is `center`, README says `left`
  - These must be fixed together (fixing recycling alone makes read-only fields editable) and are a visible behavior change → release note
- [ ] Android vertical alignment parity with iOS (verify — example looked vertically centered on Android already)
- [ ] Android: remove seams between words in highlight background
- [ ] Android: replace deprecated `DisplayMetrics.scaledDensity` with `TypedValue.applyDimension(COMPLEX_UNIT_SP, …)`
- [ ] iOS perf: cache per-character sizes / use glyph bounding rects instead of `sizeWithAttributes` per draw
- [ ] Tests: Jest for prop conversion; Maestro flows on example (type → onChange, focus/blur)
- [ ] Release `1.0.0` with CHANGELOG

---

## Phase 3 — Growth / discoverability (ongoing)

- [ ] README rewrite: hero GIF (typing + Instagram-story style), recipes (story text, marker highlight, tag chips), full typed props table
- [ ] GitHub repo: description, topics, social preview image; align repo name with npm name
- [ ] Submit to reactnative.directory (New Arch + Expo badges), awesome-react-native
- [ ] Blog post (dev.to / Medium): "Instagram story text in React Native"
- [ ] LinkedIn / X post with demo GIF at 1.0 launch
- [ ] Answer evergreen Stack Overflow questions (per-line text background in RN)
- [ ] Demo video / GitHub Pages gallery (Expo Go can't run native code, so no Snack)
- [ ] Move `WARP.md` out of repo root; keep CHANGELOG via release-it

---

## Release checklist (each release)

1. `yarn lint && yarn typecheck && yarn test && yarn prepare`
2. Build example iOS + Android (CLI) and Expo dev build
3. `yarn release` (release-it: version, changelog, tag, npm publish, GitHub release)
