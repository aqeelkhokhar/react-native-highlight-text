import type * as React from 'react';
import {
  codegenNativeCommands,
  codegenNativeComponent,
  type CodegenTypes,
  type HostComponent,
  type ViewProps,
} from 'react-native';

export interface OnChangeEventData {
  readonly text: string;
}

/** Same shape as React Native's `TextInput` focus event: the view's tag. */
export interface OnFocusEventData {
  readonly target: CodegenTypes.Int32;
}

/** Same shape as React Native's `TextInput` blur event: the view's tag. */
export interface OnBlurEventData {
  readonly target: CodegenTypes.Int32;
}

export interface OnSubmitEditingEventData {
  readonly text: string;
}

export interface OnSelectionChangeEventData {
  readonly selection: {
    readonly start: CodegenTypes.Int32;
    readonly end: CodegenTypes.Int32;
  };
}

/**
 * Text alignment options
 *
 * Horizontal alignment:
 * - 'left' or 'flex-start': Align text to the left
 * - 'center': Center align text
 * - 'right' or 'flex-end': Align text to the right
 * - 'justify': Justify text (distribute evenly)
 *
 * Vertical alignment:
 * - 'top': Align to top
 * - 'bottom': Align to bottom
 *
 * Combined alignment:
 * - 'top-left', 'top-center', 'top-right'
 * - 'bottom-left', 'bottom-center', 'bottom-right'
 */
export type TextAlignment =
  | 'left'
  | 'center'
  | 'right'
  | 'justify'
  | 'flex-start'
  | 'flex-end'
  | 'top'
  | 'bottom'
  | 'top-left'
  | 'top-center'
  | 'top-right'
  | 'bottom-left'
  | 'bottom-center'
  | 'bottom-right';

/**
 * Props of the native (codegen) component. Numeric values and colors are
 * strings here; the `HighlightTextView` wrapper converts numbers and any
 * React Native color into these strings.
 */
export interface NativeProps extends ViewProps {
  color?: string;
  textColor?: string;
  textAlign?: string;
  verticalAlign?: string;
  fontFamily?: string;
  fontSize?: string;
  fontWeight?: string;
  /** Additional space between characters, in layout points (matches React Native's letterSpacing). */
  letterSpacing?: string;
  lineHeight?: string;
  lineSpacing?: string;
  highlightBorderRadius?: string;
  padding?: string;
  paddingLeft?: string;
  paddingRight?: string;
  paddingTop?: string;
  paddingBottom?: string;
  /** Reduces background height from the top (shrinks inward from font line box) */
  backgroundInsetTop?: string;
  /** Reduces background height from the bottom (shrinks inward from font line box) */
  backgroundInsetBottom?: string;
  /** Reduces background width from the left (shrinks inward from glyph bounds) */
  backgroundInsetLeft?: string;
  /** Reduces background width from the right (shrinks inward from glyph bounds) */
  backgroundInsetRight?: string;
  text?: string;
  /** Defaults to true (native default), so omitting it keeps the view editable. */
  isEditable?: CodegenTypes.WithDefault<boolean, true>;
  autoFocus?: boolean;
  placeholder?: string;
  placeholderTextColor?: string;
  /** -1 (default) means no limit. */
  maxLength?: CodegenTypes.WithDefault<CodegenTypes.Int32, -1>;
  /** Empty string (default) keeps the platform default. */
  autoCapitalize?: string;
  /** Empty string (default) keeps the platform default. */
  keyboardType?: string;
  /** Empty string (default) keeps the platform default (Return inserts a new line). */
  returnKeyType?: string;
  onChange?: CodegenTypes.BubblingEventHandler<OnChangeEventData>;
  onFocus?: CodegenTypes.DirectEventHandler<OnFocusEventData>;
  onBlur?: CodegenTypes.DirectEventHandler<OnBlurEventData>;
  onSubmitEditing?: CodegenTypes.DirectEventHandler<OnSubmitEditingEventData>;
  onSelectionChange?: CodegenTypes.DirectEventHandler<OnSelectionChangeEventData>;
}

export type HighlightTextViewNativeComponentType = HostComponent<NativeProps>;

interface NativeCommands {
  focus: (
    viewRef: React.ElementRef<HighlightTextViewNativeComponentType>
  ) => void;
  blur: (
    viewRef: React.ElementRef<HighlightTextViewNativeComponentType>
  ) => void;
  clear: (
    viewRef: React.ElementRef<HighlightTextViewNativeComponentType>
  ) => void;
  // Named setTextValue (not setText) so the generated Android interface does not
  // clash with the `text` prop setter.
  setTextValue: (
    viewRef: React.ElementRef<HighlightTextViewNativeComponentType>,
    text: string
  ) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ['focus', 'blur', 'clear', 'setTextValue'],
});

export default codegenNativeComponent<NativeProps>(
  'HighlightTextView'
) as HighlightTextViewNativeComponentType;
