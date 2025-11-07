import { useState } from 'react';
import { StyleSheet, SafeAreaView, ScrollView, Text, View } from 'react-native';
import { HighlightTextView } from 'react-native-highlight-text-view';

export default function App() {
  const [text, setText] = useState('Hello World');

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Border Radius Test</Text>
          <HighlightTextView
            color="#00A4A3"
            textColor="#FFFFFF"
            fontSize="24"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="33"
            text="Rounded Corners"
            isEditable={false}
            style={styles.textBox}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Font Family Test</Text>
          <HighlightTextView
            color="#FF6B6B"
            textColor="#000000"
            fontFamily="serif"
            fontSize="22"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="6"
            text="Serif Font Style"
            isEditable={false}
            style={styles.textBox}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Font Weight: Bold</Text>
          <HighlightTextView
            color="#9B59B6"
            textColor="#FFFFFF"
            fontSize="26"
            fontWeight="bold"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="8"
            text="Bold Text Style"
            isEditable={false}
            style={styles.textBox}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Font Weight: 300 (Light)</Text>
          <HighlightTextView
            color="#3498DB"
            textColor="#000000"
            fontSize="24"
            fontWeight="300"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="8"
            text="Light Weight Text"
            isEditable={false}
            style={styles.textBox}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Vertical Align: Top</Text>
          <HighlightTextView
            color="#4ECDC4"
            textColor="#000000"
            fontSize="20"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="8"
            verticalAlign="bottom"
            text="Aligned Top"
            isEditable={false}
            style={styles.textBoxTall}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Vertical Align: Bottom</Text>
          <HighlightTextView
            color="#FFD93D"
            textColor="#000000"
            fontSize="20"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="8"
            verticalAlign="bottom"
            text="Aligned Bottom"
            isEditable={false}
            style={styles.textBoxTall}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Combined Alignment: Top-Left</Text>
          <HighlightTextView
            color="#A8E6CF"
            textColor="#000000"
            fontSize="20"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="8"
            textAlign="top-left"
            text="Top Left Corner"
            isEditable={false}
            style={styles.textBoxTall}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>
            Combined Alignment: Bottom-Right
          </Text>
          <HighlightTextView
            color="#FFB6B9"
            textColor="#000000"
            fontSize="20"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="8"
            textAlign="bottom-right"
            text="Bottom Right"
            isEditable={false}
            style={styles.textBoxTall}
            fontWeight="900"
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Background Insets</Text>
          <HighlightTextView
            color="#C7CEEA"
            textColor="#000000"
            fontSize="28"
            paddingLeft="10"
            paddingRight="10"
            paddingTop="8"
            paddingBottom="8"
            backgroundInsetTop="4"
            backgroundInsetBottom="4"
            highlightBorderRadius="6"
            text="Tight Background"
            isEditable={false}
            style={styles.textBox}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Editable with All Props</Text>
          <HighlightTextView
            color="#B8E0D2"
            textColor="#000000"
            fontFamily="monospace"
            fontSize="18"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            highlightBorderRadius="10"
            textAlign="right"
            text={text}
            isEditable={true}
            onChange={(e) => setText(e.nativeEvent.text)}
            style={styles.textBoxEditable}
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
    padding: 16,
    marginVertical: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
  },
  textBox: {
    width: '100%',
    height: 80,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    backgroundColor: '#fff',
  },
  textBoxTall: {
    width: '100%',
    height: 120,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    backgroundColor: '#fff',
  },
  textBoxEditable: {
    width: '100%',
    height: 100,
    borderWidth: 2,
    borderColor: '#4CAF50',
    borderRadius: 8,
    backgroundColor: '#fff',
  },
});
