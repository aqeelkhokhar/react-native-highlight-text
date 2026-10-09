import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { HighlightTextView } from 'react-native-highlight-text-view';

export default function App() {
  const [text, setText] = useState('Hello from Expo');

  return (
    <View style={styles.container}>
      <HighlightTextView
        color="#B8E0D2"
        textColor="#000000"
        fontSize="40"
        fontWeight="bold"
        textAlign="center"
        paddingLeft="12"
        paddingRight="12"
        paddingTop="4"
        paddingBottom="4"
        highlightBorderRadius="12"
        text={text}
        isEditable={true}
        onChange={(e) => setText(e.nativeEvent.text)}
        style={styles.highlightText}
      />
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    paddingTop: 80,
  },
  highlightText: {
    flex: 1,
    margin: 20,
  },
});
