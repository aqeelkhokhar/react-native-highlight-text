import { useRef, useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
  type ColorValue,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import {
  HighlightTextView,
  type HighlightTextViewRef,
} from 'react-native-highlight-text-view';

type Align = 'left' | 'center' | 'right';

// Colors can be any React Native color: hex, rgb(), hsl() or a name.
const SWATCHES: { color: ColorValue; text: ColorValue }[] = [
  { color: '#FFFFFF', text: '#111111' },
  { color: '#FF3D7F', text: '#FFFFFF' },
  { color: 'rgb(255, 204, 0)', text: '#1A1A1A' },
  { color: 'hsl(158, 64%, 52%)', text: '#062A20' },
  { color: '#6C5CE7', text: '#FFFFFF' },
  { color: 'black', text: 'white' },
];

const ALIGNS: Align[] = ['center', 'left', 'right'];
const SIZES = [24, 32, 40];

function AlignIcon({ align }: { align: Align }) {
  const justify =
    align === 'left' ? 'flex-start' : align === 'right' ? 'flex-end' : 'center';
  return (
    <View style={[styles.alignIcon, { alignItems: justify }]}>
      {[18, 11, 15].map((width, i) => (
        <View key={i} style={[styles.alignBar, { width }]} />
      ))}
    </View>
  );
}

export function StoryEditor() {
  const ref = useRef<HighlightTextViewRef>(null);
  const [text, setText] = useState('');
  const [swatch, setSwatch] = useState(1);
  const [align, setAlign] = useState<Align>('center');
  const [size, setSize] = useState(32);
  const [focused, setFocused] = useState(false);

  const nextAlign = () =>
    setAlign(ALIGNS[(ALIGNS.indexOf(align) + 1) % ALIGNS.length]!);
  const nextSize = () =>
    setSize(SIZES[(SIZES.indexOf(size) + 1) % SIZES.length]!);

  const { color, text: textColor } = SWATCHES[swatch]!;

  return (
    <View style={styles.backdrop}>
      <SafeAreaView edges={['top', 'bottom']} style={styles.fill}>
        <KeyboardAvoidingView
          behavior="padding"
          keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 24}
          style={styles.fill}
        >
          <View style={styles.toolbar}>
            <View style={styles.toolGroup}>
              <Pressable
                testID="btn-align"
                accessibilityLabel="Text alignment"
                onPress={nextAlign}
                style={styles.toolButton}
              >
                <AlignIcon align={align} />
              </Pressable>
              <Pressable
                testID="btn-size"
                accessibilityLabel="Font size"
                onPress={nextSize}
                style={styles.toolButton}
              >
                <Text style={[styles.sizeLabel, { fontSize: 11 + size / 4 }]}>
                  Aa
                </Text>
              </Pressable>
            </View>
            <View style={styles.toolGroup}>
              <Pressable
                testID="btn-clear"
                onPress={() => ref.current?.clear()}
                style={styles.textButton}
              >
                <Text style={styles.textButtonLabel}>Clear</Text>
              </Pressable>
              <Pressable
                testID="btn-done"
                onPress={() => ref.current?.blur()}
                style={[styles.textButton, focused && styles.doneActive]}
              >
                <Text
                  style={[
                    styles.textButtonLabel,
                    focused && styles.doneActiveLabel,
                  ]}
                >
                  Done
                </Text>
              </Pressable>
            </View>
          </View>

          <HighlightTextView
            ref={ref}
            testID="story-input"
            text={text}
            onChange={(e) => setText(e.nativeEvent.text)}
            onFocus={() => setFocused(true)}
            onBlur={() => setFocused(false)}
            placeholder="Tap to type"
            placeholderTextColor="rgba(255, 255, 255, 0.6)"
            color={color}
            textColor={textColor}
            fontSize={size}
            fontWeight="800"
            textAlign={align}
            verticalAlign="center"
            paddingLeft={10}
            paddingRight={10}
            paddingTop={4}
            paddingBottom={4}
            highlightBorderRadius={10}
            autoCapitalize="sentences"
            style={styles.canvas}
          />

          <View style={styles.swatches}>
            {SWATCHES.map((s, i) => (
              <Pressable
                key={i}
                testID={`swatch-${i}`}
                accessibilityLabel={`Color ${i + 1}`}
                onPress={() => setSwatch(i)}
                style={[styles.swatchRing, i === swatch && styles.swatchActive]}
              >
                <View style={[styles.swatch, { backgroundColor: s.color }]} />
              </Pressable>
            ))}
          </View>
        </KeyboardAvoidingView>
      </SafeAreaView>
    </View>
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
  },
  backdrop: {
    flex: 1,
    backgroundColor: '#24133D',
    backgroundImage:
      'radial-gradient(circle at 15% 10%, rgba(255, 120, 150, 0.9) 0%, rgba(255, 120, 150, 0) 55%), ' +
      'radial-gradient(circle at 90% 85%, rgba(98, 84, 255, 0.95) 0%, rgba(98, 84, 255, 0) 60%), ' +
      'linear-gradient(165deg, #4A2366 0%, #1B1033 100%)',
  },
  toolbar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 52,
    paddingHorizontal: 16,
  },
  toolGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  toolButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  },
  alignIcon: {
    width: 18,
    gap: 3,
  },
  alignBar: {
    height: 2.5,
    borderRadius: 2,
    backgroundColor: '#FFFFFF',
  },
  sizeLabel: {
    color: '#FFFFFF',
    fontWeight: '700',
  },
  textButton: {
    height: 34,
    paddingHorizontal: 14,
    borderRadius: 17,
    justifyContent: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  },
  textButtonLabel: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '600',
  },
  doneActive: {
    backgroundColor: '#FFFFFF',
  },
  doneActiveLabel: {
    color: '#111111',
  },
  canvas: {
    flex: 1,
    marginHorizontal: 20,
    marginVertical: 12,
  },
  swatches: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 10,
    paddingBottom: 12,
  },
  swatchRing: {
    width: 38,
    height: 38,
    borderRadius: 19,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: 'transparent',
  },
  swatchActive: {
    borderColor: '#FFFFFF',
  },
  swatch: {
    width: 28,
    height: 28,
    borderRadius: 14,
    borderWidth: 1.5,
    borderColor: 'rgba(255, 255, 255, 0.85)',
  },
});
