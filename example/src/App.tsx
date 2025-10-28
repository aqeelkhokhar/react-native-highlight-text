import { useState } from 'react';
import { StyleSheet, SafeAreaView, ScrollView, Text, View } from 'react-native';
import { HighlightTextView } from 'react-native-highlight-text-view';

export default function App() {
  const [text, setText] = useState('Hello World');

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView}>
        <View style={styles.section}>
          <Text style={styles.label}>Standard Font (Helvetica)</Text>
          <HighlightTextView
            color="#00A4A3"
            textColor="#000000"
            fontFamily="Helvetica"
            fontSize="32"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="4"
            text="Standard Font"
            isEditable={false}
            style={styles.textInput}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>
            With Background Insets + Tight Line Height
          </Text>
          <HighlightTextView
            color="#FFD700"
            textColor="#000000"
            fontFamily="Helvetica"
            fontSize="32"
            lineHeight="30"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            backgroundInsetTop="14"
            backgroundInsetBottom="14"
            highlightBorderRadius="4"
            text="Tight Background with lines touching each other smoothly"
            isEditable={false}
            style={styles.textInput}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Editable Text (Touching Lines)</Text>
          <HighlightTextView
            color="#00A4A3"
            textColor="#000000"
            fontFamily="Helvetica"
            fontSize="32"
            lineHeight="30"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            backgroundInsetTop="14"
            backgroundInsetBottom="14"
            backgroundInsetLeft="28"
            backgroundInsetRight="28"
            highlightBorderRadius="4"
            text={text}
            isEditable={true}
            onChange={(e) => {
              console.log('Text changed:', e.nativeEvent.text);
              setText(e.nativeEvent.text);
            }}
            style={styles.textInputEditable}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollView: {
    flex: 1,
  },
  section: {
    padding: 20,
    backgroundColor: '#fff',
    marginBottom: 2,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#666',
    marginBottom: 10,
  },
  textInput: {
    width: '100%',
    height: 80,
    backgroundColor: '#fff',
  },
  textInputEditable: {
    width: '100%',
    height: 120,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
  },
});
