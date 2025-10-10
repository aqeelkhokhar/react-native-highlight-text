import { View, StyleSheet } from 'react-native';
import { HighlightTextView } from 'react-native-highlight-text';

export default function App() {
  return (
    <View style={styles.container}>
      <HighlightTextView
        color="#00A4A3"
        textColor="#FFFFFF"
        textAlign="left"
        fontFamily="Helvetica"
        fontSize="44"
        paddingLeft="8"
        paddingRight="8"
        paddingTop="4"
        paddingBottom="4"
        style={styles.textInput}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  textInput: {
    width: '100%',
    height: 200,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
  },
});
