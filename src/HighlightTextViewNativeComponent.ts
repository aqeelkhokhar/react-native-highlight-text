import { codegenNativeComponent } from 'react-native';
import type { ViewProps } from 'react-native';
import type { BubblingEventHandler } from 'react-native/Libraries/Types/CodegenTypes';

export interface OnChangeEventData {
  readonly text: string;
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
 * Vertical alignment (iOS only):
 * - 'top': Align to top
 * - 'bottom': Align to bottom
 *
 * Combined alignment (iOS only):
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

export interface HighlightTextViewProps extends ViewProps {
  color?: string;
  textColor?: string;
  textAlign?: string;
  verticalAlign?: string;
  fontFamily?: string;
  fontSize?: string;
  padding?: string;
  paddingLeft?: string;
  paddingRight?: string;
  paddingTop?: string;
  paddingBottom?: string;
  text?: string;
  isEditable?: boolean;
  onChange?: BubblingEventHandler<OnChangeEventData>;
}

export default codegenNativeComponent<HighlightTextViewProps>(
  'HighlightTextView'
);
