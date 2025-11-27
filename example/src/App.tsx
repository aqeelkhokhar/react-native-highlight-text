import { StyleSheet, SafeAreaView } from 'react-native';
import { HighlightTextView } from 'react-native-highlight-text-view';

export default function App() {
  return (
    <SafeAreaView style={styles.container}>
      <HighlightTextView
        color="#B8E0D2"
        textColor="#000000"
        fontFamily="satoshi"
        fontSize="52"
        fontWeight="bold"
        textAlign="left"
        verticalAlign="center"
        paddingLeft={'15'}
        paddingRight={'15'}
        paddingTop={'12'}
        paddingBottom={'12'}
        highlightBorderRadius="8"
        text="Hello World"
        autoFocus={true}
        style={styles.highlightText}
        lineHeight="53"
        letterSpacing="-0.8"
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  highlightText: {
    flex: 1,
    margin: 20,
  },
});
