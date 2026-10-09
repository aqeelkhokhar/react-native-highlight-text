import { useState, type ReactNode } from 'react';
import {
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
  type ColorValue,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { HighlightTextView } from 'react-native-highlight-text-view';

const SERIF = Platform.select({ ios: 'Georgia', default: 'serif' });

function Recipe({
  title,
  children,
  style,
}: {
  title: string;
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
}) {
  return (
    <View style={styles.recipe}>
      <Text style={styles.recipeTitle}>{title}</Text>
      <View style={[styles.card, style]}>{children}</View>
    </View>
  );
}

const CHIP_FONT_SIZE = 15;
const CHIP_PADDING = 10;

// A read-only tag chip. The native view is sized by its style, so the chip
// width comes from measuring the same text with a hidden <Text>.
function Chip({
  label,
  color,
  textColor,
}: {
  label: string;
  color: ColorValue;
  textColor: ColorValue;
}) {
  const [width, setWidth] = useState(0);
  if (width === 0) {
    return (
      <Text
        style={styles.chipMeasure}
        onLayout={(e) => setWidth(Math.ceil(e.nativeEvent.layout.width))}
      >
        {label}
      </Text>
    );
  }
  return (
    <HighlightTextView
      editable={false}
      text={label}
      color={color}
      textColor={textColor}
      fontSize={CHIP_FONT_SIZE}
      fontWeight="700"
      textAlign="center"
      verticalAlign="center"
      paddingLeft={CHIP_PADDING}
      paddingRight={CHIP_PADDING}
      paddingTop={6}
      paddingBottom={6}
      highlightBorderRadius={14}
      style={[styles.chip, { width: width + CHIP_PADDING * 2 + 36 }]}
    />
  );
}

export function Recipes() {
  return (
    <View style={styles.screen}>
      <SafeAreaView edges={['top', 'bottom']} style={styles.fill}>
        <ScrollView contentContainerStyle={styles.content}>
          <Recipe title="Story text" style={styles.storyCard}>
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
              style={styles.story}
            />
          </Recipe>

          <Recipe title="Marker" style={styles.paperCard}>
            <HighlightTextView
              editable={false}
              text="Ship the demo before Friday"
              color="#FFE066"
              textColor="#1D1D1F"
              fontSize={24}
              fontWeight="600"
              textAlign="left"
              verticalAlign="center"
              paddingLeft={3}
              paddingRight={3}
              backgroundInsetTop={14}
              highlightBorderRadius={3}
              style={styles.marker}
            />
          </Recipe>

          <Recipe title="Tag chips" style={styles.chipsCard}>
            <View style={styles.chips}>
              <Chip label="#reactnative" color="#E0F2FE" textColor="#075985" />
              <Chip label="#fabric" color="#FCE7F3" textColor="#9D174D" />
              <Chip label="#expo" color="#DCFCE7" textColor="#166534" />
              <Chip label="#ios" color="#FEF3C7" textColor="#92400E" />
              <Chip label="#android" color="#EDE9FE" textColor="#5B21B6" />
            </View>
          </Recipe>

          <Recipe title="Quote" style={styles.quoteCard}>
            <HighlightTextView
              editable={false}
              text={'“Make it simple, but significant.”'}
              color="#14213D"
              textColor="#FCA311"
              fontFamily={SERIF}
              fontSize={28}
              lineHeight={34}
              paddingLeft={8}
              paddingRight={8}
              paddingTop={4}
              paddingBottom={4}
              backgroundInsetTop={4}
              backgroundInsetBottom={4}
              highlightBorderRadius={4}
              textAlign="left"
              verticalAlign="center"
              style={styles.quote}
            />
          </Recipe>
        </ScrollView>
      </SafeAreaView>
    </View>
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
  },
  screen: {
    flex: 1,
    backgroundColor: '#0E0B16',
  },
  content: {
    paddingTop: 52,
    paddingHorizontal: 16,
    paddingBottom: 24,
  },
  recipe: {
    marginTop: 14,
  },
  recipeTitle: {
    color: 'rgba(255, 255, 255, 0.6)',
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 1,
    textTransform: 'uppercase',
    marginBottom: 6,
    marginLeft: 4,
  },
  card: {
    borderRadius: 18,
    overflow: 'hidden',
  },
  storyCard: {
    backgroundColor: '#E86A33',
    backgroundImage:
      'linear-gradient(160deg, #F7B267 0%, #F25F5C 55%, #6B2D5C 100%)',
  },
  story: {
    height: 130,
  },
  paperCard: {
    backgroundColor: '#FBF8F1',
    paddingHorizontal: 12,
  },
  marker: {
    height: 92,
  },
  chipsCard: {
    backgroundColor: '#1C1826',
    padding: 10,
  },
  chips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
  },
  chip: {
    height: 52,
  },
  chipMeasure: {
    opacity: 0,
    fontSize: CHIP_FONT_SIZE,
    fontWeight: '700',
  },
  quoteCard: {
    backgroundColor: '#F3EEE3',
    paddingHorizontal: 10,
  },
  quote: {
    height: 120,
  },
});
