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

- [x] JS wrapper component: accept `number | string` for all numeric props and `ColorValue` for colors; convert to strings for native (non-breaking — string props keep working) (Phase 2b)
- [x] Events: `onFocus`, `onBlur`, `onSubmitEditing`, `onSelectionChange` (Phase 2b)
- [x] Props: `placeholder`, `placeholderTextColor`, `maxLength`, `autoCapitalize`, `keyboardType`, `returnKeyType`, `editable` alias for `isEditable`
- [x] Ref commands: `focus()`, `blur()`, `clear()`, `setText()` (codegen `codegenNativeCommands`; native name `setTextValue`)
- [x] Fix the "`key={fontFamily}` required" workaround natively (re-measure on font change)
- [x] iOS: `verticalAlign="center"` renders at the top (Android centers) (Phase 2b)
- [x] **iOS state bugs (pre-existing, found in regression check):** (Phase 2a: `+shouldBeRecycled NO`, `isEditable` is `WithDefault<boolean, true>`, iOS default `textAlign` is left) `updateProps` only applies props that differ from the previous props, so
  - Fabric view recycling leaks native state between views (e.g. `highlightBorderRadius`/padding of a previous screen appears on a new view) — fix with `+shouldBeRecycled NO` or a full reset in `prepareForRecycle`
  - `isEditable={false}` is ignored on a freshly mounted view (codegen default `false`, native default `YES`) — only works on recycled views today
  - Default `textAlign` on iOS fresh views is `center`, README says `left`
  - These must be fixed together (fixing recycling alone makes read-only fields editable) and are a visible behavior change → release note
- [x] Android vertical alignment parity with iOS (verified: top/center/bottom already work on Android; README "iOS only" note removed)
- [x] Android: remove seams between words in highlight background (all rects filled as one path)
- [x] Android: replace deprecated `DisplayMetrics.scaledDensity` with `TypedValue.applyDimension(COMPLEX_UNIT_SP, …)`
- [x] iOS perf: cache per-character sizes / use glyph bounding rects instead of `sizeWithAttributes` per draw (Phase 2b: memoized per font + character, same values so the drawing is identical)
- [x] Tests: Jest for prop conversion; Maestro flows on example (type → onChange, focus/blur)
- [x] Prepare `1.0.0`: `package.json` version, CHANGELOG `1.0.0 (unreleased)` with upgrade notes, release-it config (not released: no tag, no publish)
- [ ] Release `1.0.0` (`yarn release --no-increment`, see the release checklist)

### Phase 2a verification (2026-10-08)

Native correctness fixes only (no new API). Same e2e harness, Release builds of `feat/phase-2` before and after the fixes, iOS 26.1 sim + Android 17 emu. The flow gained a fresh-mount read-only check (type into an `isEditable={false}` view on the first screen, `s1_align_typed` must match `s1_align`); it passes on both platforms (61 steps).

| Screen            | iOS before → after                                                                               | Android before → after                                          |
| ----------------- | ------------------------------------------------------------------------------------------------ | --------------------------------------------------------------- |
| S1–S4, S6 initial | identical                                                                                        | identical (S4: anti-aliasing at overlapping round corners only) |
| S5 spacing        | padding-10 box now drawn with its own props (before: radius/padding leaked from a recycled view) | 8 px of edge anti-aliasing (seam fix)                           |
| S6 after, S7      | cursor blink only                                                                                | Gboard suggestion strip only                                    |
| S8 autoFocus      | default padding/radius (before: S7's padding 8 / radius 6 leaked via recycling)                  | Gboard suggestion strip only                                    |

Not fixed here (pre-existing, out of scope): on iOS `verticalAlign="center"` renders at the top (same as omitting it), Android centers it; iOS still draws per-character rounded rects (visible bumps with large padding), Android merges them.

### Phase 2b verification (2026-10-09)

Developer-facing API (JS wrapper with number/color conversion, `editable` alias, placeholder, `maxLength`, `autoCapitalize`, `keyboardType`, `returnKeyType`, focus/blur/submit/selection events, ref methods), the native font re-measure, the iOS `verticalAlign="center"` fix and the iOS per-character size cache. Same harness, Release builds of `feat/phase-2` before (`e2e/shots/*-2a`) and after (`*-2b`), plus a new S9 `api` scenario after the original 8 (flow now 96 steps). `yarn lint`, `typecheck`, `test` (21 Jest tests) and `prepare` green; Expo example (SDK 57) Android Release build green.

| Screen         | iOS before → after                             | Android before → after                           |
| -------------- | ---------------------------------------------- | ------------------------------------------------ |
| S1, S2, S4, S5 | identical                                      | identical                                        |
| S3 vertical    | `center` box now centered (intended fix)       | identical                                        |
| S6–S8          | identical                                      | Gboard suggestion strip and cursor blink only    |
| S9 api (new)   | passes; font change = remount, pixel-identical | passes; font change = remount except `sel=` line |

Still open: with no `verticalAlign` iOS draws at the top and Android centers (documented, kept for 1.0.0); iOS still draws per-character rounded rects. Android: newer React Native already sends `topFocus`/`topBlur` from `BaseViewManager`, so the view only sends its own on versions that do not (avoids double events).

### Phase 2 final verification (2026-10-09)

**Focus/blur events (exactly once per focus and per blur).** S9 `api` scenario, counters after the flow (Focus, Blur, Focus, Blur): `focus=2 blur=2` everywhere, and the flow's `focus=1`/`blur=1` steps pass.

| Target                                           | Result                                                                                                     |
| ------------------------------------------------ | ---------------------------------------------------------------------------------------------------------- |
| Expo SDK 57 (RN 0.86.3), Android 17 emu, Release | once each (full 96-step flow passes)                                                                       |
| Expo SDK 57, Android, Release with R8 minify     | **twice per focus before the fix** (R8 renamed the class the check looked for); once each after the fix    |
| CLI RN 0.87.1, iOS 26.1 sim, Release             | once each                                                                                                  |
| CLI RN 0.87.1, Android 17 emu, Release           | once each                                                                                                  |
| Expo iOS                                         | not built: Expo SDK 57 generated scripts break on the space in the repo path (upstream issue, see Phase 1) |

React Native 0.81+ attaches `BaseVMFocusChangeListener` and sends `topFocus`/`topBlur` for every view; 0.76 to 0.79 attach no focus listener at all (checked in the RN sources). The view now sends its own events only when no focus listener is attached.

**Final regression: `main` (0.1.33, RN 0.81.1) vs `feat/phase-2` (RN 0.87.1).** Release builds, `main` in a temporary worktree with prebuilt RN core. `main` has no S9 and no props for it, so the baseline ran S1 to S8 (the S1 fresh-mount read-only check made optional, since `main` fails it on iOS); `feat/phase-2` ran the full 96-step flow and passed on both platforms.

| Screen         | iOS 26.1 sim                                                           | Android 17 emu                                               |
| -------------- | ---------------------------------------------------------------------- | ------------------------------------------------------------ |
| S1, S2, S4     | identical                                                              | highlight edge anti-aliasing (one-path fill) + title text AA |
| S3 vertical    | `verticalAlign="center"` now centred (iOS center fix)                  | edge AA + title text AA                                      |
| S5 spacing     | inset box drawn with its own radius/padding (iOS recycling fix)        | edge AA + title text AA                                      |
| S6 initial, S7 | identical                                                              | edge AA + title text AA                                      |
| S6 after       | cursor blink                                                           | edge AA + title text AA                                      |
| S8 autofocus   | default padding/radius instead of S7's leaked ones (iOS recycling fix) | edge AA + title text AA                                      |

Every Android body difference is a 1 px anti-aliasing change on highlight outlines (no pixel differs by more than 60 in S5/S8, one row in S4), which is the CHANGELOG's Android seams / one-shape entry. The iOS default `textAlign` change does not show up in any of these screenshots (no visible difference in S1 to S8). `yarn lint`, `typecheck`, `test`, `prepare` and `npm pack --dry-run` green.

---

## Phase 3 — Growth / discoverability (in progress)

- [x] README rewrite: hero GIF (typing + Instagram-story style), recipes (story text, marker highlight, tag chips), full typed props table (3a)
- [ ] GitHub repo: description, topics, social preview image (image made in `docs/assets/social-preview.png`, not uploaded); align repo name with npm name
- [ ] Submit to reactnative.directory (New Arch + Expo badges), awesome-react-native
- [ ] Blog post (dev.to / Medium): "Instagram story text in React Native"
- [ ] LinkedIn / X post with demo GIF at 1.0 launch
- [ ] Answer evergreen Stack Overflow questions (per-line text background in RN)
- [ ] Demo video / GitHub Pages gallery (Expo Go can't run native code, so no Snack)
- [x] Move `WARP.md` out of repo root (removed, prop guide moved to CONTRIBUTING); keep CHANGELOG via release-it

### Phase 3a — README and showcase (done)

Example app rebuilt as a showcase (Story editor + Recipes screens), demo media in `docs/assets/` (`hero-ios.gif`, `hero-android.gif`, `recipes.png`, `social-preview.png`), README rewritten with recipes and the full API reference.

### Phase 3b — Android rendering parity with iOS (done, 1.1.0)

The 3a recipes screenshot showed Android lagging iOS: a box per word with a seam at every space, nearly square tag chips, markers filling most of the line. Root causes and fixes (Android only, iOS untouched):

- **Units:** `padding*`, `highlightBorderRadius`, `backgroundInset*` (and the view padding derived from the padding) were applied as raw pixels; iOS uses points. They are now converted with the display density (dp), so on a 420 dpi phone they are 2.625x larger than before and equal to iOS.
- **Gaps at spaces:** a consequence of the units (padding too small to cover a space) plus per-character corner logic. Character rects use the iOS geometry (glyph advance and line box, shrunk by the insets, expanded by the padding, spaces/newlines skipped), and characters whose padded rects touch or overlap are merged into one run per line, drawn as one rounded rect. Runs separated by a real gap are fully rounded, like iOS.
- **Line box / `lineHeight`:** the background now starts from the line box like iOS (custom `lineHeight`, otherwise the font height), anchored on the descent. `lineHeight` is the exact baseline distance (it was multiplied by the font's height ratio, about 17% too tall). Without `lineHeight` no extra spacing is added (it used to add the vertical padding), so lines overlap like iOS. `lineHeight` stays in sp like `fontSize` (equal to dp at the default font scale).
- **iOS oddities, reported not fixed:** iOS draws each character as its own fully rounded rect, so large radii show slightly scalloped top/bottom edges (tag chips, `Bold rounded`, insets left/right). Android draws runs and has clean edges. iOS also sets a negative `lineSpacing` for tight `lineHeight`, which TextKit ignores.

Regression (Release, Android 17 emu, `android-3a` vs `android-3b`): the 96-step flow passes before and after. Every screen differs only by the intended highlight changes (continuous per-line runs, dp padding/radius/insets, exact `lineHeight` in S5, text shifted by the dp view padding); controls, events and status lines are unchanged. Showcase Recipes and Story now look equivalent on iOS and Android (fonts differ). `hero-android.gif` and the Android half of `recipes.png` re-recorded with the fixed build.

---

## Release checklist (each release)

1. `yarn lint && yarn typecheck && yarn test && yarn prepare`
2. Build example iOS + Android (CLI) and Expo dev build
3. `yarn release` (release-it: version, tag, npm publish, GitHub release). For 1.0.0 the version is already set, so run `yarn release --no-increment`.
4. CHANGELOG.md is hand-written: the conventional-changelog plugin has `infile: false` (it only recommends the version), and the GitHub release body is the matching `## <version>` section of CHANGELOG.md (`github.releaseNotes`). Change `## 1.0.0 (unreleased)` to the release date before releasing.
