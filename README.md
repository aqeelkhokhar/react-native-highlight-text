# react-native-highlight-text-view

**Editable highlighted text for React Native: a rounded background behind every line of text, like Instagram stories and TikTok captions.** A real text input on iOS and Android, drawn natively, built for the New Architecture.

[![npm version](https://img.shields.io/npm/v/react-native-highlight-text-view?color=cb3837&logo=npm)](https://www.npmjs.com/package/react-native-highlight-text-view)
[![npm downloads](https://img.shields.io/npm/dm/react-native-highlight-text-view?color=cb3837)](https://www.npmjs.com/package/react-native-highlight-text-view)
[![license](https://img.shields.io/npm/l/react-native-highlight-text-view?color=blue)](https://github.com/aqeelkhokhar/react-native-highlight-text/blob/main/LICENSE)
![platforms](https://img.shields.io/badge/platforms-iOS%20%7C%20Android-lightgrey)
![New Architecture](https://img.shields.io/badge/New%20Architecture-Fabric-6f42c1)
![Expo](https://img.shields.io/badge/Expo-SDK%2052%2B%20dev%20build-000020?logo=expo)
![TypeScript](https://img.shields.io/badge/types-TypeScript-3178c6?logo=typescript&logoColor=white)

<p align="center">
  <img src="https://raw.githubusercontent.com/aqeelkhokhar/react-native-highlight-text/main/docs/assets/hero-ios.gif" width="300" alt="Typing story text on iOS: the rounded highlight grows line by line, then the color and alignment change" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/aqeelkhokhar/react-native-highlight-text/main/docs/assets/hero-android.gif" width="300" alt="The same story editor on Android" />
</p>

```sh
npm install react-native-highlight-text-view   # then: cd ios && pod install
```

```tsx
import { HighlightTextView } from 'react-native-highlight-text-view';

const [text, setText] = useState('Hello');

<HighlightTextView
  text={text}
  onChange={(e) => setText(e.nativeEvent.text)}
  color="#FF3D7F"
  textColor="white"
  highlightBorderRadius={10}
  style={{ height: 200 }}
/>;
```

Expo? It works in development builds and EAS builds, with no config plugin. See [Installation](#installation).

## Contents

- [Why this library](#why-this-library)
- [When to use it](#when-to-use-it)
- [Compatibility](#compatibility)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Recipes](#recipes)
- [API reference](#api-reference): [props](#props), [events](#events), [ref methods](#ref-methods), [types](#typescript)
- [Styling details](#styling-details)
- [FAQ](#faq)
- [Upgrading](#upgrading) · [Example app](#example-app) · [Contributing](#contributing) · [License](#license)

## Why this library

- **A real, editable text input.** Users type, select and delete text while the highlight redraws under it. Controlled `text`, `placeholder`, `maxLength`, keyboard and return key props, focus, blur, submit and selection events, and `focus()`, `blur()`, `clear()`, `setText()` ref methods.
- **A rounded highlight that follows wrapping.** The background hugs each line of text with rounded corners and updates as lines wrap, grow and shrink.
- **Drawn natively.** The highlight is painted by the native text view (`UITextView` with a custom layout manager on iOS, an `EditText` on Android), so there is no JavaScript measuring or layout pass while typing.
- **Same look on iOS and Android.** Padding, corner radius, insets and line height use the same units (points / dp) on both platforms. Set `verticalAlign` and the same props give the same result.
- **Typed like `TextInput`.** Familiar prop and event names, numbers for sizes (`fontSize={32}`), any React Native color string (`"#FF3D7F"`, `"rgb(255, 204, 0)"`, `"teal"`), and full TypeScript types.
- **Fine control.** Per-side padding, corner radius, background insets for fonts with tall metrics, line height, letter spacing, horizontal and vertical alignment. Change fonts, sizes, colors or alignment at runtime without remounting.
- **Expo friendly.** Works in Expo development builds and EAS builds. No config plugin, no extra setup.

Read more about how it works: [Instagram story text in React Native: rounded, highlighted text that you can edit](https://dev.to/aqeel_ahmad_331bef29ba12a/instagram-story-text-in-react-native-rounded-highlighted-text-that-you-can-edit-15cl).

## When to use it

A plain `<Text style={{ backgroundColor }}>` paints one rectangle behind the whole text block. It cannot round the corners of each wrapped line, add padding around each line, or do any of this inside an editable input.

Use this library when the background itself is part of the design:

- **Story and reel editors:** Instagram story text, TikTok captions, text stickers on photos and videos.
- **Captions and overlays:** titles and quotes on images.
- **Marker highlights:** a highlighter-pen stroke behind part of each line.
- **Chips and tags:** small rounded labels such as `#reactnative`.

If you only need a flat background behind a single line of static text, a regular `<Text>` or `<View>` is simpler.

## Compatibility

| Environment                                         | Status |
| --------------------------------------------------- | ------ |
| React Native 0.76+ on the New Architecture (Fabric) | ✅     |
| Expo SDK 52+ with a development build or EAS build  | ✅     |
| iOS                                                 | ✅     |
| Android (minSdk 24)                                 | ✅     |

Verified on React Native 0.87.1 (CLI) and Expo SDK 57 (React Native 0.86.3).

**New Architecture.** This is a Fabric component. The New Architecture is the default for new apps since React Native 0.76 and Expo SDK 52, Expo SDK 53 turns it on for all projects, and from React Native 0.82 it is always on. Apps that still run the Old Architecture (Paper) are not supported.

**Expo.** Expo Go ships a fixed set of native libraries and cannot load native code from any other package, so use a [development build](https://docs.expo.dev/develop/development-builds/introduction/) instead: `npx expo run:ios`, `npx expo run:android`, `npx expo prebuild`, or an EAS build. This is the usual setup for any library with native code.

## Installation

**React Native CLI**

```sh
npm install react-native-highlight-text-view
# or
yarn add react-native-highlight-text-view

cd ios && pod install
```

**Expo** (development build or EAS)

```sh
npx expo install react-native-highlight-text-view
npx expo run:ios   # or: npx expo run:android, npx expo prebuild, eas build
```

No config plugin is needed. Rebuild the app after installing so the native code is included.

## Quick start

```tsx
import { useRef, useState } from 'react';
import {
  HighlightTextView,
  type HighlightTextViewRef,
} from 'react-native-highlight-text-view';

export default function StoryText() {
  const [text, setText] = useState('');
  const ref = useRef<HighlightTextViewRef>(null);

  return (
    <HighlightTextView
      ref={ref}
      text={text}
      onChange={(e) => setText(e.nativeEvent.text)}
      placeholder="Tap to type"
      color="#FF3D7F"
      textColor="white"
      fontSize={32}
      fontWeight="800"
      textAlign="center"
      verticalAlign="center"
      paddingLeft={10}
      paddingRight={10}
      paddingTop={4}
      paddingBottom={4}
      highlightBorderRadius={10}
      returnKeyType="done"
      onSubmitEditing={() => ref.current?.blur()}
      style={{ width: '100%', height: 240 }}
    />
  );
}
```

Numeric props accept numbers (`fontSize={32}`) or strings (`fontSize="32"`), and colors accept any React Native color string (`"#00A4A3"`, `"rgb(0, 164, 163)"`, `"teal"`). The string-only API of earlier versions keeps working unchanged.

The view is sized by its `style` (like a `TextInput`); the highlight is drawn only behind the text inside it.

## Recipes

<p align="center">
  <img src="https://raw.githubusercontent.com/aqeelkhokhar/react-native-highlight-text/main/docs/assets/recipes.png" width="640" alt="Recipes on iOS and Android: story text, marker, tag chips and a quote" />
</p>

iOS on the left, Android on the right. All four are in the [example app](https://github.com/aqeelkhokhar/react-native-highlight-text/blob/main/example/src/Recipes.tsx). Read-only views use `editable={false}`.

### Story text

```tsx
<HighlightTextView
  editable={false}
  text={'golden hour\nin lisbon'}
  color="#FF3D7F"
  textColor="#FFFFFF"
  fontSize={30}
  fontWeight="800"
  textAlign="center"
  verticalAlign="center"
  paddingLeft={10}
  paddingRight={10}
  paddingTop={4}
  paddingBottom={4}
  highlightBorderRadius={10}
  style={{ height: 130 }}
/>
```

### Marker highlight

A soft color plus a large `backgroundInsetTop` paints only the lower part of each line, like a highlighter pen.

```tsx
<HighlightTextView
  editable={false}
  text="Ship the demo before Friday"
  color="#FFE066"
  textColor="#1D1D1F"
  fontSize={24}
  fontWeight="600"
  paddingLeft={3}
  paddingRight={3}
  backgroundInsetTop={14}
  highlightBorderRadius={3}
  verticalAlign="center"
  style={{ height: 92 }}
/>
```

### Tag chips

One small read-only view per tag. Give each chip a width (the example measures the label with a hidden `<Text>`).

```tsx
<HighlightTextView
  editable={false}
  text="#reactnative"
  color="#E0F2FE"
  textColor="#075985"
  fontSize={15}
  fontWeight="700"
  textAlign="center"
  verticalAlign="center"
  paddingLeft={10}
  paddingRight={10}
  paddingTop={6}
  paddingBottom={6}
  highlightBorderRadius={14}
  style={{ width: 130, height: 40 }}
/>
```

### Quote

```tsx
<HighlightTextView
  editable={false}
  text={'“Make it simple, but significant.”'}
  color="#14213D"
  textColor="#FCA311"
  fontFamily={Platform.select({ ios: 'Georgia', default: 'serif' })}
  fontSize={28}
  lineHeight={34}
  paddingLeft={8}
  paddingRight={8}
  paddingTop={4}
  paddingBottom={4}
  backgroundInsetTop={4}
  backgroundInsetBottom={4}
  highlightBorderRadius={4}
  verticalAlign="center"
  style={{ height: 120 }}
/>
```

## API reference

### Props

All props of `View` are supported too. `Numeric` means `number | string`. Sizes are in points (dp on Android).

| Prop                    | Type                                                                                                                                                                       | Default          | Description                                                                                                                                                                                        |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `text`                  | `string`                                                                                                                                                                   | -                | Controlled text value                                                                                                                                                                              |
| `color`                 | `ColorValue`                                                                                                                                                               | `#FFFF00`        | Highlight background color. Hex, `rgb()`/`rgba()`, `hsl()` or a named color (`PlatformColor` is not supported)                                                                                     |
| `textColor`             | `ColorValue`                                                                                                                                                               | platform default | Text color                                                                                                                                                                                         |
| `textAlign`             | `TextAlignment`                                                                                                                                                            | `left`           | `'left'`, `'center'`, `'right'`, `'justify'`, `'flex-start'`, `'flex-end'`, `'top'`, `'bottom'`, `'top-left'`, `'top-center'`, `'top-right'`, `'bottom-left'`, `'bottom-center'`, `'bottom-right'` |
| `verticalAlign`         | `'top' \| 'center' \| 'middle' \| 'bottom'`                                                                                                                                | -                | Vertical alignment. Alternative to the combined `textAlign` values. When omitted, iOS draws at the top and Android centers                                                                         |
| `fontFamily`            | `string`                                                                                                                                                                   | -                | Font family name. Can be changed at runtime (no `key` needed)                                                                                                                                      |
| `fontSize`              | `Numeric`                                                                                                                                                                  | `32`             | Font size in points                                                                                                                                                                                |
| `fontWeight`            | `'normal' \| 'bold' \| '100'` … `'900'`                                                                                                                                    | `normal`         | Font weight                                                                                                                                                                                        |
| `letterSpacing`         | `Numeric`                                                                                                                                                                  | `0`              | Extra space between characters, in layout points (same semantics as React Native's `letterSpacing`)                                                                                                |
| `lineHeight`            | `Numeric`                                                                                                                                                                  | `0`              | Distance between lines (0 means the font's own line height)                                                                                                                                        |
| `lineSpacing`           | `Numeric`                                                                                                                                                                  | `0`              | Extra space between lines (Android)                                                                                                                                                                |
| `highlightBorderRadius` | `Numeric`                                                                                                                                                                  | `4`              | Corner radius of the highlight. `0` or unset uses the default of 4                                                                                                                                 |
| `padding`               | `Numeric`                                                                                                                                                                  | `4`              | Padding on all four sides of the highlight (expands the background outward)                                                                                                                        |
| `paddingLeft`           | `Numeric`                                                                                                                                                                  | `4`              | Left padding of the highlight                                                                                                                                                                      |
| `paddingRight`          | `Numeric`                                                                                                                                                                  | `4`              | Right padding of the highlight                                                                                                                                                                     |
| `paddingTop`            | `Numeric`                                                                                                                                                                  | `4`              | Top padding of the highlight                                                                                                                                                                       |
| `paddingBottom`         | `Numeric`                                                                                                                                                                  | `4`              | Bottom padding of the highlight                                                                                                                                                                    |
| `backgroundInsetTop`    | `Numeric`                                                                                                                                                                  | `0`              | Shrinks the background from the top (useful for fonts with large vertical metrics)                                                                                                                 |
| `backgroundInsetBottom` | `Numeric`                                                                                                                                                                  | `0`              | Shrinks the background from the bottom (useful for fonts with large vertical metrics)                                                                                                              |
| `backgroundInsetLeft`   | `Numeric`                                                                                                                                                                  | `0`              | Shrinks the background from the left                                                                                                                                                               |
| `backgroundInsetRight`  | `Numeric`                                                                                                                                                                  | `0`              | Shrinks the background from the right                                                                                                                                                              |
| `editable`              | `boolean`                                                                                                                                                                  | `true`           | Whether the text can be edited. Same as `isEditable`; `editable` wins if both are given                                                                                                            |
| `isEditable`            | `boolean`                                                                                                                                                                  | `true`           | Original name of `editable`, still supported                                                                                                                                                       |
| `autoFocus`             | `boolean`                                                                                                                                                                  | `false`          | Focuses the input and opens the keyboard on mount (editable views only)                                                                                                                            |
| `placeholder`           | `string`                                                                                                                                                                   | -                | Text shown while the input is empty                                                                                                                                                                |
| `placeholderTextColor`  | `ColorValue`                                                                                                                                                               | platform default | Placeholder color                                                                                                                                                                                  |
| `maxLength`             | `number`                                                                                                                                                                   | no limit         | Maximum number of characters the user can type (text set through `text` or `setText()` is not cut)                                                                                                 |
| `autoCapitalize`        | `'none' \| 'sentences' \| 'words' \| 'characters'`                                                                                                                         | platform default | Automatic capitalization while typing                                                                                                                                                              |
| `keyboardType`          | `'default' \| 'email-address' \| 'numeric' \| 'phone-pad' \| 'number-pad' \| 'decimal-pad' \| 'url' \| 'ascii-capable' \| 'numbers-and-punctuation' \| 'visible-password'` | `default`        | Keyboard to show                                                                                                                                                                                   |
| `returnKeyType`         | `'default' \| 'done' \| 'go' \| 'next' \| 'search' \| 'send' \| 'previous' \| 'join' \| 'route' \| 'none'`                                                                 | `default`        | Label of the Return key. Any value other than `'default'` makes Return fire `onSubmitEditing` instead of inserting a new line                                                                      |

### Events

| Event               | Payload (`event.nativeEvent`)                   | Fired when                                                                         |
| ------------------- | ----------------------------------------------- | ---------------------------------------------------------------------------------- |
| `onChange`          | `{ text: string }`                              | The text changes (typing, `clear()`, `setText()`)                                  |
| `onFocus`           | `{ target: number }`                            | The input gains focus                                                              |
| `onBlur`            | `{ target: number }`                            | The input loses focus                                                              |
| `onSubmitEditing`   | `{ text: string }`                              | Return is pressed while `returnKeyType` is set to something other than `'default'` |
| `onSelectionChange` | `{ selection: { start: number; end: number } }` | The cursor moves or the selection changes                                          |

### Ref methods

Pass a `ref` (`useRef<HighlightTextViewRef>(null)`) to call:

| Method          | Description                                               |
| --------------- | --------------------------------------------------------- |
| `focus()`       | Focuses the input and opens the keyboard (editable views) |
| `blur()`        | Removes focus and closes the keyboard                     |
| `clear()`       | Clears the text and fires `onChange` with `''`            |
| `setText(text)` | Replaces the text and fires `onChange` with the new text  |

### TypeScript

Exported types: `HighlightTextViewProps`, `HighlightTextViewRef`, `NumericProp`, `VerticalAlignment`, `AutoCapitalize`, `KeyboardType`, `ReturnKeyType`, `TextAlignment`, the event payload types (`OnChangeEventData`, `OnFocusEventData`, `OnBlurEventData`, `OnSubmitEditingEventData`, `OnSelectionChangeEventData`) and `HighlightTextViewNativeProps`.

## Styling details

### Padding vs background insets

- **Padding props** (`padding`, `paddingTop`, `paddingBottom`, …) expand the background **outward** from the text, adding colored area around the glyphs.
- **Background inset props** (`backgroundInsetTop`, `backgroundInsetBottom`, …) shrink the background **inward** from the font's line box, for a tighter fit around the visible glyphs.

**Use case for background insets:** some fonts (Eczar, Georgia and others) have large built-in vertical metrics (ascender and descender), so the highlight looks too tall. Use `backgroundInsetTop` and `backgroundInsetBottom` to tighten it.

```tsx
<HighlightTextView
  fontFamily="Eczar"
  fontSize={32}
  paddingLeft={8}
  paddingRight={8}
  paddingTop={4}
  paddingBottom={4}
  backgroundInsetTop={6}
  backgroundInsetBottom={6}
  text="Tight Background"
/>
```

**Touching backgrounds across lines:** combine `lineHeight` with background insets.

```tsx
<HighlightTextView
  fontSize={32}
  lineHeight={36} // slightly larger than fontSize for tight spacing
  paddingLeft={8}
  paddingRight={8}
  paddingTop={4}
  paddingBottom={4}
  backgroundInsetTop={14} // a large inset reduces the background height
  backgroundInsetBottom={14} // and leaves room for lines to touch
  highlightBorderRadius={4}
  text="Multiple lines with touching backgrounds create smooth vertical flow"
/>
```

**Tip:** set `lineHeight` to about `fontSize + 4` to `fontSize + 8`, then adjust `backgroundInsetTop` and `backgroundInsetBottom` until the backgrounds touch smoothly.

### Auto-focusing the input

Use `autoFocus` to open the keyboard when the component mounts, so the user can start typing without tapping first:

```tsx
<HighlightTextView
  color="#00A4A3"
  textColor="#FFFFFF"
  fontSize={20}
  text={text}
  autoFocus
  onChange={(e) => setText(e.nativeEvent.text)}
  style={{ width: '100%', height: 100 }}
/>
```

### Changing the font at runtime

`fontFamily`, `fontSize` and `fontWeight` can be changed at any time; the view re-measures and redraws the highlight with the new font metrics. The `key={fontFamily}` workaround needed by versions before 1.0 is no longer required (it still works, it just remounts the view).

## FAQ

**Does it work with Expo?**
Yes, in Expo development builds and EAS builds (Expo SDK 52 and newer). Install with `npx expo install react-native-highlight-text-view` and rebuild with `npx expo run:ios`, `npx expo run:android` or EAS. No config plugin is needed.

**Why not Expo Go?**
Expo Go contains a fixed set of native libraries and cannot load native code from other packages. This applies to every library with its own native code. A development build is your own version of Expo Go that includes it.

**Does it support the Old Architecture?**
No. It is a Fabric component for the New Architecture, which is the default since React Native 0.76 and always on from React Native 0.82. If your app still runs the Old Architecture, you need to enable the New Architecture first.

**How is it different from `TextInput`?**
It is a native text input with a background drawn behind each line of text. It shares most of `TextInput`'s API (`editable`, `placeholder`, `maxLength`, `autoCapitalize`, `keyboardType`, `returnKeyType`, `onChange`, `onFocus`, `onBlur`, `onSubmitEditing`, `onSelectionChange`, `focus()`, `blur()`, `clear()`), and adds the highlight props. Text styling is set with props (`fontSize`, `textColor`, `fontFamily`, …) instead of a text `style`.

**Does it handle multi-line text and emoji?**
Yes. Text wraps onto new lines and each line gets its own rounded background. Return inserts a new line unless `returnKeyType` is set to something other than `'default'`. When `maxLength` trims typed or pasted text, it does not cut a single emoji in half.

**What about performance?**
The highlight is drawn by the native text view during its normal draw pass, with no JavaScript layout or measuring while the user types. On iOS, glyph sizes are cached between draws.

**How do I get the same look on iOS and Android?**
Set `verticalAlign` (for example `verticalAlign="center"`). Without it, iOS draws the text at the top of the view and Android centers it. All other props use the same units on both platforms. Fonts can still differ if a family is not available on both (for example `Georgia` on iOS and `serif` on Android).

**Can I use it for read-only text?**
Yes. Set `editable={false}`, as in the [recipes](#recipes).

## Upgrading

Upgrading from 0.1.x? Read the [1.0.0 upgrade notes](https://github.com/aqeelkhokhar/react-native-highlight-text/blob/main/CHANGELOG.md#upgrade-notes-1): a few defaults changed (for example the iOS default `textAlign` is now `left`). Version 1.1.0 changed the Android look to match iOS. All releases are listed in the [CHANGELOG](https://github.com/aqeelkhokhar/react-native-highlight-text/blob/main/CHANGELOG.md).

## Example app

The [example app](https://github.com/aqeelkhokhar/react-native-highlight-text/tree/main/example) is a story editor (live typing, color swatches, alignment, font size, ref methods) plus the recipes above:

```sh
yarn
yarn example ios      # or: yarn example android
```

## Contributing

Issues and pull requests are welcome.

- [Development workflow](https://github.com/aqeelkhokhar/react-native-highlight-text/blob/main/CONTRIBUTING.md#development-workflow)
- [Sending a pull request](https://github.com/aqeelkhokhar/react-native-highlight-text/blob/main/CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](https://github.com/aqeelkhokhar/react-native-highlight-text/blob/main/CODE_OF_CONDUCT.md)
- [Report a bug or request a feature](https://github.com/aqeelkhokhar/react-native-highlight-text/issues)

## License

MIT

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
