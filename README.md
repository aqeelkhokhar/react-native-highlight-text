# react-native-highlight-text-view

Instagram/TikTok-style highlighted text input for React Native: rounded per-character backgrounds, editable, and built natively for iOS and Android on the New Architecture (Fabric).

## Installation

```sh
npm install react-native-highlight-text-view
# or
yarn add react-native-highlight-text-view
```

**React Native CLI (iOS):** run `cd ios && pod install` after installing.

**Expo:**

```sh
npx expo install react-native-highlight-text-view
npx expo prebuild   # or build with EAS
```

This package contains native code, so it works in Expo **development builds** and EAS builds but **not in Expo Go**. No config plugin is needed.

## Compatibility

| Environment                             | Supported                  |
| --------------------------------------- | -------------------------- |
| React Native ≥ 0.76 (New Architecture)  | ✅                         |
| React Native with Old Architecture      | ❌ (Fabric-only component) |
| Expo SDK ≥ 52 (development build / EAS) | ✅                         |
| Expo Go                                 | ❌                         |
| iOS                                     | ✅                         |
| Android (minSdk 24)                     | ✅                         |

## Usage

```tsx
import { useRef, useState } from 'react';
import {
  HighlightTextView,
  type HighlightTextViewRef,
} from 'react-native-highlight-text-view';

export default function App() {
  const [text, setText] = useState('Hello World');
  const ref = useRef<HighlightTextViewRef>(null);

  return (
    <HighlightTextView
      ref={ref}
      color="#00A4A3"
      textColor="black"
      textAlign="flex-start"
      fontSize={32}
      paddingLeft={8}
      paddingRight={8}
      paddingTop={4}
      paddingBottom={4}
      highlightBorderRadius={6}
      placeholder="Write something"
      text={text}
      onChange={(e) => setText(e.nativeEvent.text)}
      returnKeyType="done"
      onSubmitEditing={() => ref.current?.blur()}
      style={{ width: '100%', height: 200 }}
    />
  );
}
```

Numeric props accept numbers (`fontSize={32}`) or strings (`fontSize="32"`), and colors accept any React Native color string (`"#00A4A3"`, `"rgb(0, 164, 163)"`, `"teal"`). The string-only API of earlier versions keeps working unchanged.

## Props

All props of `View` are supported too. `Numeric` means `number | string`.

| Prop                    | Type                                                                                                                                                                       | Default          | Description                                                                                                                                                                                        |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `text`                  | `string`                                                                                                                                                                   | -                | Controlled text value                                                                                                                                                                              |
| `color`                 | `ColorValue`                                                                                                                                                               | `#FFFF00`        | Highlight background color. Hex, `rgb()`/`rgba()`, `hsl()` or a named color (`PlatformColor` is not supported)                                                                                     |
| `textColor`             | `ColorValue`                                                                                                                                                               | -                | Text color                                                                                                                                                                                         |
| `textAlign`             | `TextAlignment`                                                                                                                                                            | `left`           | `'left'`, `'center'`, `'right'`, `'justify'`, `'flex-start'`, `'flex-end'`, `'top'`, `'bottom'`, `'top-left'`, `'top-center'`, `'top-right'`, `'bottom-left'`, `'bottom-center'`, `'bottom-right'` |
| `verticalAlign`         | `'top' \| 'center' \| 'middle' \| 'bottom'`                                                                                                                                | -                | Vertical alignment. Alternative to the combined `textAlign` values. When omitted, iOS draws at the top and Android centers                                                                         |
| `fontFamily`            | `string`                                                                                                                                                                   | -                | Font family name. Can be changed at runtime (no `key` needed)                                                                                                                                      |
| `fontSize`              | `Numeric`                                                                                                                                                                  | `32`             | Font size in points                                                                                                                                                                                |
| `fontWeight`            | `'normal' \| 'bold' \| '100'` … `'900'`                                                                                                                                    | `normal`         | Font weight                                                                                                                                                                                        |
| `letterSpacing`         | `Numeric`                                                                                                                                                                  | `0`              | Extra space between characters, in layout points (same semantics as React Native's `letterSpacing`)                                                                                                |
| `lineHeight`            | `Numeric`                                                                                                                                                                  | `0`              | Line height override (0 means use default line height)                                                                                                                                             |
| `lineSpacing`           | `Numeric`                                                                                                                                                                  | `0`              | Extra space between lines (Android)                                                                                                                                                                |
| `highlightBorderRadius` | `Numeric`                                                                                                                                                                  | `0`              | Border radius for the highlight background                                                                                                                                                         |
| `padding`               | `Numeric`                                                                                                                                                                  | `4`              | Padding around each character highlight (expands background outward)                                                                                                                               |
| `paddingLeft`           | `Numeric`                                                                                                                                                                  | -                | Left padding for character highlight                                                                                                                                                               |
| `paddingRight`          | `Numeric`                                                                                                                                                                  | -                | Right padding for character highlight                                                                                                                                                              |
| `paddingTop`            | `Numeric`                                                                                                                                                                  | -                | Top padding for character highlight                                                                                                                                                                |
| `paddingBottom`         | `Numeric`                                                                                                                                                                  | -                | Bottom padding for character highlight                                                                                                                                                             |
| `backgroundInsetTop`    | `Numeric`                                                                                                                                                                  | `0`              | Shrinks background from top (useful for fonts with large vertical metrics)                                                                                                                         |
| `backgroundInsetBottom` | `Numeric`                                                                                                                                                                  | `0`              | Shrinks background from bottom (useful for fonts with large vertical metrics)                                                                                                                      |
| `backgroundInsetLeft`   | `Numeric`                                                                                                                                                                  | `0`              | Shrinks background from left                                                                                                                                                                       |
| `backgroundInsetRight`  | `Numeric`                                                                                                                                                                  | `0`              | Shrinks background from right                                                                                                                                                                      |
| `editable`              | `boolean`                                                                                                                                                                  | `true`           | Whether the text can be edited. Same as `isEditable`; `editable` wins if both are given                                                                                                            |
| `isEditable`            | `boolean`                                                                                                                                                                  | `true`           | Original name of `editable`, still supported                                                                                                                                                       |
| `autoFocus`             | `boolean`                                                                                                                                                                  | `false`          | Focuses the input and opens the keyboard on mount (editable views only)                                                                                                                            |
| `placeholder`           | `string`                                                                                                                                                                   | -                | Text shown while the input is empty                                                                                                                                                                |
| `placeholderTextColor`  | `ColorValue`                                                                                                                                                               | platform default | Placeholder color                                                                                                                                                                                  |
| `maxLength`             | `number`                                                                                                                                                                   | no limit         | Maximum number of characters the user can type (text set through `text` or `setText()` is not cut)                                                                                                 |
| `autoCapitalize`        | `'none' \| 'sentences' \| 'words' \| 'characters'`                                                                                                                         | platform default | Automatic capitalization while typing                                                                                                                                                              |
| `keyboardType`          | `'default' \| 'email-address' \| 'numeric' \| 'phone-pad' \| 'number-pad' \| 'decimal-pad' \| 'url' \| 'ascii-capable' \| 'numbers-and-punctuation' \| 'visible-password'` | `default`        | Keyboard to show                                                                                                                                                                                   |
| `returnKeyType`         | `'default' \| 'done' \| 'go' \| 'next' \| 'search' \| 'send' \| 'previous' \| 'join' \| 'route' \| 'none'`                                                                 | `default`        | Label of the Return key. Any value other than `'default'` makes Return fire `onSubmitEditing` instead of inserting a new line                                                                      |

## Events

| Event               | Payload (`event.nativeEvent`)                   | Fired when                                                                         |
| ------------------- | ----------------------------------------------- | ---------------------------------------------------------------------------------- |
| `onChange`          | `{ text: string }`                              | The text changes (typing, `clear()`, `setText()`)                                  |
| `onFocus`           | `{ target: number }`                            | The input gains focus                                                              |
| `onBlur`            | `{ target: number }`                            | The input loses focus                                                              |
| `onSubmitEditing`   | `{ text: string }`                              | Return is pressed while `returnKeyType` is set to something other than `'default'` |
| `onSelectionChange` | `{ selection: { start: number; end: number } }` | The cursor moves or the selection changes                                          |

## Methods (ref)

Pass a `ref` (`useRef<HighlightTextViewRef>(null)`) to call:

| Method          | Description                                               |
| --------------- | --------------------------------------------------------- |
| `focus()`       | Focuses the input and opens the keyboard (editable views) |
| `blur()`        | Removes focus and closes the keyboard                     |
| `clear()`       | Clears the text and fires `onChange` with `''`            |
| `setText(text)` | Replaces the text and fires `onChange` with the new text  |

### Understanding Padding vs Background Insets

- **Padding props** (`paddingTop`, `paddingBottom`, etc.): Expand the background **outward** from the text, adding extra colored area around glyphs.
- **Background inset props** (`backgroundInsetTop`, `backgroundInsetBottom`, etc.): Shrink the background **inward** from the font's line box, creating tighter wrapping around actual glyphs.

**Use case for background insets:** Some fonts (like Eczar, Georgia, etc.) have large built-in vertical metrics (ascender/descender), making highlights appear too tall. Use `backgroundInsetTop` and `backgroundInsetBottom` to create a tighter fit around the visible glyphs.

**Example with large-metric font:**

```jsx
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

**Example with touching backgrounds (tight line spacing):**
To make backgrounds touch vertically across multiple lines, combine `lineHeight` with `backgroundInset`:

```jsx
<HighlightTextView
  fontSize={32}
  lineHeight={36} // Slightly larger than fontSize for tight spacing
  paddingLeft={8}
  paddingRight={8}
  paddingTop={4}
  paddingBottom={4}
  backgroundInsetTop={14} // Large inset reduces background height
  backgroundInsetBottom={14} // Creates room for lines to touch
  highlightBorderRadius={4}
  text="Multiple lines with touching backgrounds create smooth vertical flow"
/>
```

**Tip:** Set `lineHeight` to approximately `fontSize + 4` to `fontSize + 8`, then adjust `backgroundInsetTop` and `backgroundInsetBottom` until backgrounds touch smoothly.

### Auto-focusing the Input

To automatically open the keyboard when the component mounts, use the `autoFocus` prop:

```jsx
<HighlightTextView
  color="#00A4A3"
  textColor="#FFFFFF"
  fontSize={20}
  text={text}
  autoFocus={true} // Keyboard opens automatically
  onChange={(e) => setText(e.nativeEvent.text)}
  style={{ width: '100%', height: 100 }}
/>
```

This eliminates the need for double-tapping to open the keyboard - it will open on first render.

### Changing the font at runtime

`fontFamily`, `fontSize` and `fontWeight` can be changed at any time; the view re-measures and redraws the highlight with the new font metrics. The `key={fontFamily}` workaround needed by versions before 1.0 is no longer required (it still works, it just remounts the view).

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License MIT

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
