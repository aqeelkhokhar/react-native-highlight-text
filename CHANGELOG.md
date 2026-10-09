# Changelog

## 1.0.1 (unreleased)

- Docs: new README with demo GIFs, recipes and full API reference; example app showcase.

## 1.0.0 (2026-10-09)

First stable release. `HighlightTextView` now behaves like a regular React Native input: numbers and any color string for props, `placeholder`, `maxLength`, keyboard props, focus/blur/submit/selection events and ref methods. It also fixes several long-standing iOS state bugs and the Android highlight seams. Existing code that passes strings keeps working.

**Compatibility:** React Native >= 0.76 with the New Architecture (Fabric only; the old architecture is not supported). Expo SDK 52+ in a development build or EAS build (`npx expo prebuild`); Expo Go cannot load the native code. Verified on React Native 0.87.1 (CLI) and Expo SDK 57 (React Native 0.86.3).

### Upgrade notes

Behaviour changes you may notice when upgrading from 0.1.x:

- **iOS default `textAlign` is now `left`.** Views without a `textAlign` prop used to be centred on iOS. Add `textAlign="center"` if you relied on that.
- **`isEditable` defaults to `true` in the codegen spec** (`WithDefault<boolean, true>`), and `isEditable={false}` is now honoured on a freshly mounted iOS view (before, a new read-only view could still be edited). `editable` is a new alias; `editable` wins when both are given.
- **iOS views are no longer recycled.** A new view no longer inherits padding, radius, fonts, colors or alignment from a previously unmounted one. If a screen only looked right because of that leak, set the props explicitly.
- **Android named colors follow CSS / React Native.** Named and functional colors are parsed in JS now, so `green`, `gray`, `lightgray` and a few others render with their CSS value instead of Android's. `#RRGGBB` and `#AARRGGBB` are unchanged.
- **iOS 8-digit hex colors include alpha** (`#AARRGGBB`, as on Android). They used to be drawn opaque.
- **Android 14+ font scaling:** with a large accessibility font size, `lineHeight`, `lineSpacing` and `letterSpacing` now scale non-linearly like `fontSize`. Nothing changes at the default font scale.
- **`returnKeyType` changes Return.** Any value other than `'default'` makes Return fire `onSubmitEditing` instead of inserting a new line. Leave it unset for multi-line input.
- **`onFocus` / `onBlur` payload is `{ target }`**, the same as `TextInput`.
- **Default vertical position is unchanged:** with no `verticalAlign`, iOS still draws the text at the top and Android centres it. Set `verticalAlign` for the same result on both. (`verticalAlign="center"` now centres on iOS too, see below.)
- The `key={fontFamily}` remount workaround is no longer needed (it still works).

### Added

- `HighlightTextView` is now a JS wrapper around the native component. Numeric props (`fontSize`, `padding*`, `lineHeight`, `lineSpacing`, `letterSpacing`, `highlightBorderRadius`, `backgroundInset*`) accept numbers as well as strings (`fontSize={32}`), and `color`, `textColor` and `placeholderTextColor` accept any React Native color string (`rgb()`, `rgba()`, `hsl()`, named colors, short hex). `PlatformColor`/`DynamicColorIOS` are not supported.
- Events: `onFocus`, `onBlur` (`{ target }`, like `TextInput`), `onSubmitEditing` (`{ text }`) and `onSelectionChange` (`{ selection: { start, end } }`) on iOS and Android.
- Props: `placeholder`, `placeholderTextColor`, `maxLength`, `autoCapitalize`, `keyboardType`, `returnKeyType`, and `editable` as an alias of `isEditable` (`editable` wins when both are given). Setting `returnKeyType` to anything other than `'default'` makes Return fire `onSubmitEditing` instead of inserting a new line.
- Ref methods: `focus()`, `blur()`, `clear()` and `setText(text)` (native commands). `clear()` and `setText()` fire `onChange` so a controlled `text` follows them.
- Typed props: `textAlign`, `verticalAlign`, `fontWeight`, `keyboardType` and `returnKeyType` are typed as unions (any string is still accepted). New exported types: `HighlightTextViewRef`, `NumericProp`, `VerticalAlignment`, `AutoCapitalize`, `KeyboardType`, `ReturnKeyType`, the event payload types and `HighlightTextViewNativeProps`.

### Fixed

- Changing `fontFamily`, `fontSize` or `fontWeight` at runtime now re-measures and redraws the highlight (and re-applies vertical alignment). The `key={fontFamily}` remount workaround is no longer needed.
- **iOS:** `verticalAlign="center"` / `"middle"` now centers the text vertically, as on Android. It used to render at the top.
- **iOS:** 8-digit hex colors are read as `#AARRGGBB` (alpha included), the same as Android. They used to be drawn opaque.
- **iOS:** the highlight drawing caches per-character glyph sizes instead of measuring every character on every draw pass (same output, less work while typing).
- **iOS:** views are no longer recycled by Fabric. Native state (padding, insets, corner radius, fonts, alignment, colors) used to leak from a previously unmounted `HighlightTextView` into a new one, so a view could render with another view's padding or radius.
- **iOS:** `isEditable={false}` is now honoured on a freshly mounted view. Before, it only took effect after the prop changed, so a new read-only view could still be edited.
- **iOS:** the default `textAlign` (when the prop is omitted) is now `left`, as documented and as on Android. It used to be `center`.
- **Android:** the highlight background no longer shows faint seams or uneven anti-aliasing where neighbouring character backgrounds meet or overlap; all backgrounds are now filled as one shape.
- **Android:** setting `isEditable` back to `true` re-enables the on-screen keyboard (it stayed suppressed after the view had been read-only).
- **Android:** `onFocus` and `onBlur` fire exactly once per focus change, including in minified (R8) release builds. React Native 0.81+ already sends these events for every view, so the library only sends its own on older versions; the check no longer depends on a class name that R8 renames.
- **Android:** replaced the deprecated `DisplayMetrics.scaledDensity` with `TypedValue.applyDimension(COMPLEX_UNIT_SP, …)` for `lineHeight`, `lineSpacing` and `letterSpacing`. Results are identical at the default font scale.

### Changed

- Color strings that are not `#RRGGBB`/`#AARRGGBB` (named colors, `rgb()`, short hex) are converted in JS with React Native's color parser. They used to be ignored on iOS, and a few Android named colors differ from CSS (for example `green`, `gray`, `lightgray`), so those now render with the CSS/React Native value on both platforms. `#RRGGBB` and `#AARRGGBB` strings are passed to native unchanged.
- **Android 14+:** with a large accessibility font scale, `lineHeight`, `lineSpacing` and `letterSpacing` now follow the system's non-linear text scaling (like `fontSize` already did), because they are converted with `TypedValue.applyDimension(COMPLEX_UNIT_SP, …)`. At the default font scale nothing changes.
- `isEditable` is now declared with a codegen default of `true` (`WithDefault<boolean, true>`), matching the documented default on both platforms. Omitting the prop keeps the view editable, as before.

### Docs

- README: props table with real types, the new props, events and ref methods, a usage example with numbers (`fontSize={32}`), and a note that with no `verticalAlign` iOS draws at the top while Android centers. The `key={fontFamily}` section is replaced by a short note that it is no longer needed.
- `verticalAlign` and the combined `textAlign` values (`top-left`, `bottom-center`, …) are no longer documented as iOS only; they already worked on Android.
