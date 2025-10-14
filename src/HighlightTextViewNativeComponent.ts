import { codegenNativeComponent } from 'react-native';
import type { ViewProps } from 'react-native';
import type { BubblingEventHandler } from 'react-native/Libraries/Types/CodegenTypes';

export interface OnChangeEventData {
  readonly text: string;
}

export interface HighlightTextViewProps extends ViewProps {
  color?: string;
  textColor?: string;
  textAlign?: string;
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
