# Changelog

## Unreleased

### Fixed

- **iOS:** views are no longer recycled by Fabric. Native state (padding, insets, corner radius, fonts, alignment, colors) used to leak from a previously unmounted `HighlightTextView` into a new one, so a view could render with another view's padding or radius.
- **iOS:** `isEditable={false}` is now honoured on a freshly mounted view. Before, it only took effect after the prop changed, so a new read-only view could still be edited.
- **iOS:** the default `textAlign` (when the prop is omitted) is now `left`, as documented and as on Android. It used to be `center`.
- **Android:** the highlight background no longer shows faint seams or uneven anti-aliasing where neighbouring character backgrounds meet or overlap; all backgrounds are now filled as one shape.
- **Android:** setting `isEditable` back to `true` re-enables the on-screen keyboard (it stayed suppressed after the view had been read-only).
- **Android:** replaced the deprecated `DisplayMetrics.scaledDensity` with `TypedValue.applyDimension(COMPLEX_UNIT_SP, …)` for `lineHeight`, `lineSpacing` and `letterSpacing`. Results are identical at the default font scale.

### Changed

- `isEditable` is now declared with a codegen default of `true` (`WithDefault<boolean, true>`), matching the documented default on both platforms. Omitting the prop keeps the view editable, as before.

### Docs

- `verticalAlign` and the combined `textAlign` values (`top-left`, `bottom-center`, …) are no longer documented as iOS only; they already worked on Android.
