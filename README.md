# react-native-highlight-text

A native text input for React Native that supports inline text highlighting

## Installation

```sh
npm install react-native-highlight-text
```

## Usage

```js
import { useState } from 'react';
import { HighlightTextView } from 'react-native-highlight-text-view';

export default function App() {
  const [text, setText] = useState('Hello World');

  return (
    <HighlightTextView
      color="#00A4A3"
      textColor="#000000"
      textAlign="flex-start"
      fontSize="32"
      paddingLeft="8"
      paddingRight="8"
      paddingTop="4"
      paddingBottom="4"
      text={text}
      isEditable={true}
      onChange={(e) => {
        setText(e.nativeEvent.text);
      }}
      style={{ width: '100%', height: 200 }}
    />
  );
}
```

## Props

| Prop                    | Type                                                 | Default   | Description                                                                                                                                                                                                                  |
| ----------------------- | ---------------------------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `color`                 | `string`                                             | `#FFFF00` | Background highlight color (hex format)                                                                                                                                                                                      |
| `textColor`             | `string`                                             | -         | Text color (hex format)                                                                                                                                                                                                      |
| `textAlign`             | `string`                                             | `left`    | Text alignment. Supports: `'left'`, `'center'`, `'right'`, `'justify'`, `'flex-start'`, `'flex-end'`, `'top'`, `'bottom'`, `'top-left'`, `'top-center'`, `'top-right'`, `'bottom-left'`, `'bottom-center'`, `'bottom-right'` |
| `verticalAlign`         | `'top' \| 'center' \| 'middle' \| 'bottom'`          | -         | Vertical alignment (iOS only). Alternative to using combined `textAlign` values. **Note:** Android does not support vertical alignment and will use default vertical positioning.                                            |
| `fontFamily`            | `string`                                             | -         | Font family name                                                                                                                                                                                                             |
| `fontSize`              | `string`                                             | `32`      | Font size in points                                                                                                                                                                                                          |
| `lineHeight`            | `string`                                             | `0`       | Line height override (0 means use default line height)                                                                                                                                                                       |
| `highlightBorderRadius` | `string`                                             | `0`       | Border radius for the highlight background                                                                                                                                                                                   |
| `padding`               | `string`                                             | `4`       | Padding around each character highlight (expands background outward)                                                                                                                                                         |
| `paddingLeft`           | `string`                                             | -         | Left padding for character highlight                                                                                                                                                                                         |
| `paddingRight`          | `string`                                             | -         | Right padding for character highlight                                                                                                                                                                                        |
| `paddingTop`            | `string`                                             | -         | Top padding for character highlight                                                                                                                                                                                          |
| `paddingBottom`         | `string`                                             | -         | Bottom padding for character highlight                                                                                                                                                                                       |
| `backgroundInsetTop`    | `string`                                             | `0`       | Shrinks background from top (useful for fonts with large vertical metrics)                                                                                                                                                   |
| `backgroundInsetBottom` | `string`                                             | `0`       | Shrinks background from bottom (useful for fonts with large vertical metrics)                                                                                                                                                |
| `backgroundInsetLeft`   | `string`                                             | `0`       | Shrinks background from left                                                                                                                                                                                                 |
| `backgroundInsetRight`  | `string`                                             | `0`       | Shrinks background from right                                                                                                                                                                                                |
| `text`                  | `string`                                             | -         | Controlled text value                                                                                                                                                                                                        |
| `isEditable`            | `boolean`                                            | `true`    | Whether the text is editable                                                                                                                                                                                                 |
| `autoFocus`             | `boolean`                                            | `false`   | If true, automatically focuses the text input and opens the keyboard when component mounts (only works when `isEditable` is `true`)                                                                                          |
| `onChange`              | `(event: { nativeEvent: { text: string } }) => void` | -         | Callback fired when text changes                                                                                                                                                                                             |

### Understanding Padding vs Background Insets

- **Padding props** (`paddingTop`, `paddingBottom`, etc.): Expand the background **outward** from the text, adding extra colored area around glyphs.
- **Background inset props** (`backgroundInsetTop`, `backgroundInsetBottom`, etc.): Shrink the background **inward** from the font's line box, creating tighter wrapping around actual glyphs.

**Use case for background insets:** Some fonts (like Eczar, Georgia, etc.) have large built-in vertical metrics (ascender/descender), making highlights appear too tall. Use `backgroundInsetTop` and `backgroundInsetBottom` to create a tighter fit around the visible glyphs.

**Example with large-metric font:**

```jsx
<HighlightTextView
  fontFamily="Eczar"
  fontSize="32"
  paddingLeft="8"
  paddingRight="8"
  paddingTop="4"
  paddingBottom="4"
  backgroundInsetTop="6"
  backgroundInsetBottom="6"
  text="Tight Background"
/>
```

**Example with touching backgrounds (tight line spacing):**
To make backgrounds touch vertically across multiple lines, combine `lineHeight` with `backgroundInset`:

```jsx
<HighlightTextView
  fontSize="32"
  lineHeight="36" // Slightly larger than fontSize for tight spacing
  paddingLeft="8"
  paddingRight="8"
  paddingTop="4"
  paddingBottom="4"
  backgroundInsetTop="14" // Large inset reduces background height
  backgroundInsetBottom="14" // Creates room for lines to touch
  highlightBorderRadius="4"
  text="Multiple lines with touching backgrounds create smooth vertical flow"
/>
```

**Tip:** Set `lineHeight` to approximately `fontSize + 4` to `fontSize + 8`, then adjust `backgroundInsetTop` and `backgroundInsetBottom` until backgrounds touch smoothly.

**Note:** Vertical alignment is currently supported on iOS only. On Android, text will use default vertical positioning.

### Auto-focusing the Input

To automatically open the keyboard when the component mounts, use the `autoFocus` prop:

```jsx
<HighlightTextView
  color="#00A4A3"
  textColor="#FFFFFF"
  fontSize="20"
  text={text}
  isEditable={true}
  autoFocus={true} // Keyboard opens automatically
  onChange={(e) => setText(e.nativeEvent.text)}
  style={{ width: '100%', height: 100 }}
/>
```

This eliminates the need for double-tapping to open the keyboard - it will open on first render.

### Dynamic Font Family Changes

**IMPORTANT**: When changing `fontFamily` dynamically at runtime (especially to fonts with different ascender/descender values like Eczar, Georgia, etc.), you must use the `key` prop to force React to remount the component. This ensures the native layout recalculates with the new font metrics.

**Why this is needed**: Fonts like Eczar have significantly larger vertical metrics than system fonts. Without remounting, the highlight background may appear cut off at the bottom or lose corner radius.

**Solution**: Pass the `fontFamily` as the `key` prop:

```jsx
const [fontFamily, setFontFamily] = useState('system');

return (
  <HighlightTextView
    key={fontFamily} 
    fontFamily={fontFamily}
    fontSize="32"
    color="#00A4A3"
    textColor="#FFFFFF"
    paddingLeft="8"
    paddingRight="8"
    paddingTop="4"
    paddingBottom="4"
    backgroundInsetTop="6" 
    backgroundInsetBottom="6"
    highlightBorderRadius="8"
    text="Beautiful Eczar Font"
    style={{ width: '100%', height: 150 }}
  />
);
```

**What happens**:

- Font changes → `key` changes → React unmounts old component and mounts new one
- New mount → Native component calculates fresh layout with correct font metrics
- Perfect rendering → Background highlights render correctly without cutting

**Without key prop**: Background may cut off, corner radius may disappear  
**With key prop**: Perfect rendering every time ✅

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License MIT

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
