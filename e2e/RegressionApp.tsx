import { useRef, useState, type ReactNode } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import {
  HighlightTextView,
  type HighlightTextViewRef,
} from 'react-native-highlight-text-view';

const LONG = 'The quick brown fox jumps over the lazy dog';
const SERIF = Platform.select({ ios: 'Georgia', default: 'serif' });

function Box({ label, children }: { label: string; children: ReactNode }) {
  return (
    <View style={styles.box}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.boxInner}>{children}</View>
    </View>
  );
}

function Interactive({ editable }: { editable: boolean }) {
  const [text, setText] = useState('Edit me');
  return (
    <View style={styles.fill}>
      <Text testID="count" style={styles.status}>
        {`len=${text.length} text=[${text}]`}
      </Text>
      <View style={styles.row}>
        <Pressable testID="btn-clear" onPress={() => setText('')}>
          <Text style={styles.btn}>Clear</Text>
        </Pressable>
        <Pressable testID="btn-set" onPress={() => setText('Programmatic')}>
          <Text style={styles.btn}>Set text</Text>
        </Pressable>
      </View>
      <Box label={editable ? 'editable controlled' : 'isEditable=false'}>
        <HighlightTextView
          testID="input"
          color="#FFD54F"
          textColor="#000000"
          fontSize="30"
          paddingLeft="8"
          paddingRight="8"
          paddingTop="4"
          paddingBottom="4"
          highlightBorderRadius="6"
          text={text}
          isEditable={editable}
          onChange={(e) => setText(e.nativeEvent.text)}
          style={styles.fill}
        />
      </Box>
    </View>
  );
}

function AutoFocus() {
  const [text, setText] = useState('Focused');
  return (
    <Box label="autoFocus">
      <HighlightTextView
        color="#80DEEA"
        textColor="#000000"
        fontSize="30"
        text={text}
        isEditable={true}
        autoFocus={true}
        onChange={(e) => setText(e.nativeEvent.text)}
        style={styles.fill}
      />
    </Box>
  );
}

// S9 (Phase 2b): number/color props, editable alias, placeholder, maxLength,
// returnKeyType + onSubmitEditing, focus/blur/selection events, ref commands and
// a runtime font change without remounting (compare s9_font_changed with
// s9_font_remounted: they must match).
function ApiScenario() {
  const ref = useRef<HighlightTextViewRef>(null);
  const [text, setText] = useState('');
  const [focusCount, setFocusCount] = useState(0);
  const [blurCount, setBlurCount] = useState(0);
  const [submitted, setSubmitted] = useState('');
  const [selection, setSelection] = useState({ start: -1, end: -1 });
  const [serif, setSerif] = useState(false);
  const [mountKey, setMountKey] = useState(0);
  const buttons: [string, () => void][] = [
    ['Focus', () => ref.current?.focus()],
    ['Blur', () => ref.current?.blur()],
    ['Set', () => ref.current?.setText('Ref text')],
    ['Clear', () => ref.current?.clear()],
    ['Font', () => setSerif((v) => !v)],
    ['Remount', () => setMountKey((k) => k + 1)],
  ];
  return (
    <View style={styles.fill}>
      <View style={styles.row}>
        {buttons.map(([label, onPress]) => (
          <Pressable key={label} testID={`btn-${label}`} onPress={onPress}>
            <Text style={styles.smallBtn}>{label}</Text>
          </Pressable>
        ))}
      </View>
      <Text
        style={styles.status}
      >{`s9 len=${text.length} text=[${text}]`}</Text>
      <View style={styles.row}>
        <Text style={styles.status}>{`focus=${focusCount}`}</Text>
        <Text style={styles.status}>{`blur=${blurCount}`}</Text>
        <Text style={styles.status}>{`submit=[${submitted}]`}</Text>
      </View>
      <Text style={styles.status}>
        {`sel=${selection.start}-${selection.end}`}
      </Text>
      <Box label="numbers, rgb()/named colors, placeholder, maxLength 12">
        <HighlightTextView
          key={mountKey}
          ref={ref}
          testID="api-input"
          color="rgb(255, 204, 128)"
          textColor="black"
          fontSize={serif ? 34 : 28}
          fontFamily={serif ? SERIF : undefined}
          paddingLeft={8}
          paddingRight={8}
          paddingTop={4}
          paddingBottom={4}
          highlightBorderRadius={6}
          placeholder="Type here"
          placeholderTextColor="#9E9E9E"
          maxLength={12}
          autoCapitalize="none"
          returnKeyType="done"
          isEditable={false}
          editable={true}
          text={text}
          onChange={(e) => setText(e.nativeEvent.text)}
          onFocus={() => setFocusCount((c) => c + 1)}
          onBlur={() => setBlurCount((c) => c + 1)}
          onSubmitEditing={(e) => setSubmitted(e.nativeEvent.text)}
          onSelectionChange={(e) => setSelection(e.nativeEvent.selection)}
          style={styles.fill}
        />
      </Box>
    </View>
  );
}

const common = { isEditable: false, style: { flex: 1 } };

const SCENARIOS: { name: string; render: () => ReactNode }[] = [
  {
    name: 'align',
    render: () => (
      <>
        {(['left', 'center', 'right'] as const).map((a) => (
          <Box key={a} label={`textAlign=${a}`}>
            <HighlightTextView
              {...common}
              color="#B8E0D2"
              textColor="#000000"
              fontSize="24"
              textAlign={a}
              text={LONG}
            />
          </Box>
        ))}
      </>
    ),
  },
  {
    name: 'align2',
    render: () => (
      <>
        {(['justify', 'flex-start', 'flex-end'] as const).map((a) => (
          <Box key={a} label={`textAlign=${a}`}>
            <HighlightTextView
              {...common}
              color="#F8BBD0"
              textColor="#000000"
              fontSize="24"
              textAlign={a}
              text={LONG}
            />
          </Box>
        ))}
      </>
    ),
  },
  {
    name: 'vertical',
    render: () => (
      <>
        {(['top', 'center', 'bottom'] as const).map((v) => (
          <Box key={v} label={`verticalAlign=${v}`}>
            <HighlightTextView
              {...common}
              color="#C5CAE9"
              textColor="#000000"
              fontSize="24"
              verticalAlign={v}
              text="Vertical"
            />
          </Box>
        ))}
      </>
    ),
  },
  {
    name: 'styles',
    render: () => (
      <>
        <Box label="bold, radius 18, white on dark">
          <HighlightTextView
            {...common}
            color="#263238"
            textColor="#FFFFFF"
            fontSize="30"
            fontWeight="bold"
            highlightBorderRadius="18"
            paddingLeft="12"
            paddingRight="12"
            text="Bold rounded"
          />
        </Box>
        <Box label="letterSpacing 4 / -0.8">
          <HighlightTextView
            {...common}
            color="#FFE0B2"
            textColor="#000000"
            fontSize="26"
            letterSpacing="4"
            text="Spaced out"
          />
        </Box>
        <Box label="serif font">
          <HighlightTextView
            {...common}
            color="#DCEDC8"
            textColor="#000000"
            fontSize="28"
            fontFamily={SERIF}
            letterSpacing="-0.8"
            text="Serif family"
          />
        </Box>
      </>
    ),
  },
  {
    name: 'spacing',
    render: () => (
      <>
        <Box label="lineHeight 36 + insets 14 (touching)">
          <HighlightTextView
            {...common}
            color="#B39DDB"
            textColor="#000000"
            fontSize="32"
            lineHeight="36"
            paddingLeft="8"
            paddingRight="8"
            paddingTop="4"
            paddingBottom="4"
            backgroundInsetTop="14"
            backgroundInsetBottom="14"
            highlightBorderRadius="4"
            text="Multiple lines with touching backgrounds"
          />
        </Box>
        <Box label="padding 10, lineSpacing 12">
          <HighlightTextView
            {...common}
            color="#FFAB91"
            textColor="#000000"
            fontSize="22"
            padding="10"
            lineSpacing="12"
            text={LONG}
          />
        </Box>
        <Box label="insets left/right 3">
          <HighlightTextView
            {...common}
            color="#90CAF9"
            textColor="#000000"
            fontSize="26"
            backgroundInsetLeft="3"
            backgroundInsetRight="3"
            text="Inset sides"
          />
        </Box>
      </>
    ),
  },
  { name: 'editable', render: () => <Interactive editable={true} /> },
  { name: 'readonly', render: () => <Interactive editable={false} /> },
  { name: 'autofocus', render: () => <AutoFocus /> },
  { name: 'api', render: () => <ApiScenario /> },
];

export default function App() {
  const [index, setIndex] = useState(0);
  const scenario = SCENARIOS[index]!;
  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <Pressable
          testID="btn-prev"
          onPress={() => setIndex((i) => Math.max(0, i - 1))}
        >
          <Text style={styles.btn}>Prev</Text>
        </Pressable>
        <Text testID="title" style={styles.title}>
          {`S${index + 1}: ${scenario.name}`}
        </Text>
        <Pressable
          testID="btn-next"
          onPress={() => setIndex((i) => Math.min(SCENARIOS.length - 1, i + 1))}
        >
          <Text style={styles.btn}>Next</Text>
        </Pressable>
      </View>
      <View key={index} style={styles.fill}>
        {scenario.render()}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#FAFAFA', paddingTop: 70 },
  fill: { flex: 1 },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    marginBottom: 6,
  },
  title: { fontSize: 16, fontWeight: '600', color: '#000' },
  btn: {
    fontSize: 16,
    color: '#1565C0',
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  smallBtn: {
    fontSize: 15,
    color: '#1565C0',
    paddingVertical: 8,
    paddingHorizontal: 4,
  },
  box: { flex: 1, marginHorizontal: 12, marginBottom: 8 },
  label: { fontSize: 11, color: '#666', marginBottom: 2 },
  boxInner: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#DDD',
    backgroundColor: '#FFFFFF',
  },
  status: {
    fontSize: 14,
    color: '#000',
    marginHorizontal: 12,
    marginVertical: 8,
  },
});
