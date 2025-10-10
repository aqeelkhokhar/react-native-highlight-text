import { codegenNativeComponent, type ViewProps } from 'react-native';

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
}

export default codegenNativeComponent<HighlightTextViewProps>(
  'HighlightTextView'
);
