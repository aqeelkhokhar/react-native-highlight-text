import { useState } from 'react';
import { StyleSheet, SafeAreaView } from 'react-native';
import { HighlightTextView } from 'react-native-highlight-text-view';

export default function App() {
  const [text, setText] = useState('Hello World');

  return (
    <SafeAreaView style={styles.container}>
      <HighlightTextView
        color="#00A4A3"
        textColor="#000000"
        fontFamily="Helvetica"
        fontSize="32"
        paddingLeft="8"
        paddingRight="8"
        paddingTop="4"
        paddingBottom="4"
        // lineHeight='33'
        highlightBorderRadius="4"
        text={text}
        isEditable={true}
        onChange={(e) => {
          console.log('Text changed:', e.nativeEvent.text);
          setText(e.nativeEvent.text);
        }}
        style={styles.textInput}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  textInput: {
    width: '100%',
    height: '100%',
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    paddingBottom: 40,
  },
});
