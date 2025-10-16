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

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `color` | `string` | `#FFFF00` | Background highlight color (hex format) |
| `textColor` | `string` | - | Text color (hex format) |
| `textAlign` | `string` | `left` | Text alignment. Supports: `'left'`, `'center'`, `'right'`, `'justify'`, `'flex-start'`, `'flex-end'`, `'top'`, `'bottom'`, `'top-left'`, `'top-center'`, `'top-right'`, `'bottom-left'`, `'bottom-center'`, `'bottom-right'` |
| `verticalAlign` | `'top' \| 'center' \| 'middle' \| 'bottom'` | - | Vertical alignment (iOS only). Alternative to using combined `textAlign` values |
| `fontFamily` | `string` | - | Font family name |
| `fontSize` | `string` | `32` | Font size in points |
| `padding` | `string` | `4` | Padding around each character highlight |
| `paddingLeft` | `string` | - | Left padding for character highlight |
| `paddingRight` | `string` | - | Right padding for character highlight |
| `paddingTop` | `string` | - | Top padding for character highlight |
| `paddingBottom` | `string` | - | Bottom padding for character highlight |
| `text` | `string` | - | Controlled text value |
| `isEditable` | `boolean` | `true` | Whether the text is editable |
| `onChange` | `(event: { nativeEvent: { text: string } }) => void` | - | Callback fired when text changes |


**Note:** Vertical alignment is currently supported on iOS only. On Android, text will use default vertical positioning.



## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)


## License MIT

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
