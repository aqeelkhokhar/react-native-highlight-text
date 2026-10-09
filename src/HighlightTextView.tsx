import {
  forwardRef,
  useImperativeHandle,
  useRef,
  type ComponentRef,
} from 'react';
import {
  processColor,
  type ColorValue,
  type NativeSyntheticEvent,
  type ViewProps,
} from 'react-native';
import NativeHighlightTextView, {
  Commands,
  type NativeProps,
  type OnBlurEventData,
  type OnChangeEventData,
  type OnFocusEventData,
  type OnSelectionChangeEventData,
  type OnSubmitEditingEventData,
  type TextAlignment,
} from './HighlightTextViewNativeComponent';

/** A number (layout points) or a numeric string, e.g. `32` or `"32"`. */
export type NumericProp = number | string;

export type VerticalAlignment = 'top' | 'center' | 'middle' | 'bottom';

export type AutoCapitalize = 'none' | 'sentences' | 'words' | 'characters';

export type KeyboardType =
  | 'default'
  | 'email-address'
  | 'numeric'
  | 'phone-pad'
  | 'number-pad'
  | 'decimal-pad'
  | 'url'
  | 'ascii-capable'
  | 'numbers-and-punctuation'
  | 'visible-password';

export type ReturnKeyType =
  | 'default'
  | 'done'
  | 'go'
  | 'next'
  | 'search'
  | 'send'
  | 'previous'
  | 'join'
  | 'route'
  | 'none';

// `(string & {})` keeps editor autocompletion for the literals while still
// accepting any string (backward compatible with the old `string` types).
type LooseString<T extends string> = T | (string & {});

export interface HighlightTextViewProps extends ViewProps {
  /** Highlight background color. Any React Native color (hex, rgb(), named). */
  color?: ColorValue;
  /** Text color. Any React Native color. */
  textColor?: ColorValue;
  textAlign?: LooseString<TextAlignment>;
  verticalAlign?: LooseString<VerticalAlignment>;
  fontFamily?: string;
  fontSize?: NumericProp;
  fontWeight?: LooseString<
    | 'normal'
    | 'bold'
    | '100'
    | '200'
    | '300'
    | '400'
    | '500'
    | '600'
    | '700'
    | '800'
    | '900'
  >;
  /** Additional space between characters, in layout points (matches React Native's letterSpacing). */
  letterSpacing?: NumericProp;
  lineHeight?: NumericProp;
  lineSpacing?: NumericProp;
  highlightBorderRadius?: NumericProp;
  padding?: NumericProp;
  paddingLeft?: NumericProp;
  paddingRight?: NumericProp;
  paddingTop?: NumericProp;
  paddingBottom?: NumericProp;
  /** Reduces background height from the top (shrinks inward from font line box) */
  backgroundInsetTop?: NumericProp;
  /** Reduces background height from the bottom (shrinks inward from font line box) */
  backgroundInsetBottom?: NumericProp;
  /** Reduces background width from the left (shrinks inward from glyph bounds) */
  backgroundInsetLeft?: NumericProp;
  /** Reduces background width from the right (shrinks inward from glyph bounds) */
  backgroundInsetRight?: NumericProp;
  text?: string;
  /** Whether the text can be edited. Defaults to `true`. Alias: `editable`. */
  isEditable?: boolean;
  /** Same as `isEditable` (React Native `TextInput` name). Wins if both are given. */
  editable?: boolean;
  autoFocus?: boolean;
  /** Text shown when the input is empty. */
  placeholder?: string;
  placeholderTextColor?: ColorValue;
  /** Maximum number of characters the user can type. */
  maxLength?: number;
  autoCapitalize?: AutoCapitalize;
  keyboardType?: LooseString<KeyboardType>;
  /**
   * Label of the Return key. When set to anything other than `'default'`,
   * pressing Return fires `onSubmitEditing` instead of inserting a new line.
   */
  returnKeyType?: LooseString<ReturnKeyType>;
  onChange?: (event: NativeSyntheticEvent<OnChangeEventData>) => void;
  onFocus?: (event: NativeSyntheticEvent<OnFocusEventData>) => void;
  onBlur?: (event: NativeSyntheticEvent<OnBlurEventData>) => void;
  onSubmitEditing?: (
    event: NativeSyntheticEvent<OnSubmitEditingEventData>
  ) => void;
  onSelectionChange?: (
    event: NativeSyntheticEvent<OnSelectionChangeEventData>
  ) => void;
}

/** Methods available on a `ref` to `HighlightTextView`. */
export interface HighlightTextViewRef {
  /** Focuses the input and opens the keyboard (editable views only). */
  focus: () => void;
  /** Removes focus and closes the keyboard. */
  blur: () => void;
  /** Clears the text. Fires `onChange` with an empty string. */
  clear: () => void;
  /** Replaces the text. Fires `onChange` with the new text. */
  setText: (text: string) => void;
}

const NUMERIC_PROPS = [
  'fontSize',
  'letterSpacing',
  'lineHeight',
  'lineSpacing',
  'highlightBorderRadius',
  'padding',
  'paddingLeft',
  'paddingRight',
  'paddingTop',
  'paddingBottom',
  'backgroundInsetTop',
  'backgroundInsetBottom',
  'backgroundInsetLeft',
  'backgroundInsetRight',
] as const;

const COLOR_PROPS = ['color', 'textColor', 'placeholderTextColor'] as const;

/** Converts a number (or numeric string) prop to the string native expects. */
export function toNativeNumber(
  value: NumericProp | null | undefined
): string | undefined {
  if (value === undefined || value === null) return undefined;
  if (typeof value === 'number') {
    return Number.isFinite(value) ? String(value) : undefined;
  }
  return value;
}

const HEX_PASSTHROUGH = /^#(?:[0-9a-f]{6}|[0-9a-f]{8})$/i;

function hex2(n: number): string {
  return n.toString(16).padStart(2, '0').toUpperCase();
}

/**
 * Converts a React Native color to the hex string native parses
 * (`#RRGGBB`, or `#AARRGGBB` when not opaque).
 *
 * `#RRGGBB` / `#AARRGGBB` strings are passed through untouched so existing
 * apps render exactly as before. Platform colors (`PlatformColor`,
 * `DynamicColorIOS`) are not supported and are ignored.
 */
export function toNativeColor(
  value: ColorValue | null | undefined
): string | undefined {
  if (value === undefined || value === null) return undefined;
  if (typeof value === 'string' && HEX_PASSTHROUGH.test(value)) return value;
  const processed = processColor(value);
  if (typeof processed !== 'number') {
    // Unknown string: hand it to native unchanged (old behaviour).
    return typeof value === 'string' ? value : undefined;
  }
  /* eslint-disable no-bitwise */
  const argb = processed >>> 0; // Android returns a signed int
  const a = (argb >>> 24) & 0xff;
  const r = (argb >>> 16) & 0xff;
  const g = (argb >>> 8) & 0xff;
  const b = argb & 0xff;
  /* eslint-enable no-bitwise */
  return a === 0xff
    ? `#${hex2(r)}${hex2(g)}${hex2(b)}`
    : `#${hex2(a)}${hex2(r)}${hex2(g)}${hex2(b)}`;
}

/** Maps the public props to the props of the native component. */
export function toNativeProps(props: HighlightTextViewProps): NativeProps {
  const { editable, isEditable, maxLength, ...rest } = props;
  const nativeProps: Record<string, unknown> = { ...rest };

  for (const key of NUMERIC_PROPS) {
    if (key in nativeProps) {
      nativeProps[key] = toNativeNumber(props[key]);
    }
  }
  for (const key of COLOR_PROPS) {
    if (key in nativeProps) {
      nativeProps[key] = toNativeColor(props[key]);
    }
  }

  const resolvedEditable = editable ?? isEditable;
  if (resolvedEditable !== undefined) {
    nativeProps.isEditable = resolvedEditable;
  }
  if (maxLength !== undefined && maxLength !== null) {
    nativeProps.maxLength = Number.isFinite(maxLength)
      ? Math.max(0, Math.floor(maxLength))
      : undefined;
  }

  return nativeProps as NativeProps;
}

/**
 * Text input with rounded per-character highlight backgrounds.
 *
 * Numeric props accept numbers or strings, colors accept any React Native
 * color. Use a `ref` for `focus()`, `blur()`, `clear()` and `setText()`.
 */
export const HighlightTextView = forwardRef<
  HighlightTextViewRef,
  HighlightTextViewProps
>(function HighlightTextViewWithRef(props, ref) {
  const nativeRef = useRef<ComponentRef<typeof NativeHighlightTextView>>(null);

  useImperativeHandle(
    ref,
    () => ({
      focus: () => {
        if (nativeRef.current) Commands.focus(nativeRef.current);
      },
      blur: () => {
        if (nativeRef.current) Commands.blur(nativeRef.current);
      },
      clear: () => {
        if (nativeRef.current) Commands.clear(nativeRef.current);
      },
      setText: (text: string) => {
        if (nativeRef.current) Commands.setTextValue(nativeRef.current, text);
      },
    }),
    []
  );

  return <NativeHighlightTextView ref={nativeRef} {...toNativeProps(props)} />;
});

HighlightTextView.displayName = 'HighlightTextView';
