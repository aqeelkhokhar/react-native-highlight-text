import { createRef } from 'react';
import { Platform } from 'react-native';
import TestRenderer, { act } from 'react-test-renderer';
import {
  HighlightTextView,
  type HighlightTextViewProps,
  type HighlightTextViewRef,
} from '../index';
import {
  toNativeColor,
  toNativeNumber,
  toNativeProps,
} from '../HighlightTextView';
import { Commands } from '../HighlightTextViewNativeComponent';

jest.mock('../HighlightTextViewNativeComponent', () => ({
  __esModule: true,
  // Rendered as a host element named like the native component
  default: 'HighlightTextView',
  Commands: {
    focus: jest.fn(),
    blur: jest.fn(),
    clear: jest.fn(),
    setTextValue: jest.fn(),
  },
}));

const NODE = { nativeNode: true };

function render(props: HighlightTextViewProps, ref?: any) {
  let renderer!: TestRenderer.ReactTestRenderer;
  act(() => {
    renderer = TestRenderer.create(<HighlightTextView ref={ref} {...props} />, {
      createNodeMock: () => NODE,
    });
  });
  return renderer.root.findByType('HighlightTextView' as any).props;
}

beforeEach(() => {
  jest.clearAllMocks();
});

describe('toNativeNumber', () => {
  it('converts numbers to strings', () => {
    expect(toNativeNumber(32)).toBe('32');
    expect(toNativeNumber(0)).toBe('0');
    expect(toNativeNumber(-0.8)).toBe('-0.8');
    expect(toNativeNumber(1.5)).toBe('1.5');
  });

  it('passes strings through unchanged', () => {
    expect(toNativeNumber('32')).toBe('32');
    expect(toNativeNumber('')).toBe('');
  });

  it('drops missing and non-finite values', () => {
    expect(toNativeNumber(undefined)).toBeUndefined();
    expect(toNativeNumber(null)).toBeUndefined();
    expect(toNativeNumber(NaN)).toBeUndefined();
    expect(toNativeNumber(Infinity)).toBeUndefined();
  });
});

describe('toNativeColor', () => {
  it('passes #RRGGBB and #AARRGGBB strings through untouched', () => {
    expect(toNativeColor('#00A4A3')).toBe('#00A4A3');
    expect(toNativeColor('#ffd54f')).toBe('#ffd54f');
    expect(toNativeColor('#80FF0000')).toBe('#80FF0000');
  });

  it('converts named, short hex and rgb() colors to hex', () => {
    expect(toNativeColor('red')).toBe('#FF0000');
    expect(toNativeColor('black')).toBe('#000000');
    expect(toNativeColor('#f00')).toBe('#FF0000');
    expect(toNativeColor('rgb(255, 204, 128)')).toBe('#FFCC80');
  });

  it('keeps alpha as #AARRGGBB', () => {
    expect(toNativeColor('rgba(255, 0, 0, 0.5)')).toBe('#80FF0000');
    expect(toNativeColor('transparent')).toBe('#00000000');
  });

  it('handles the signed ints processColor returns on Android', () => {
    const original = Platform.OS;
    Object.defineProperty(Platform, 'OS', {
      value: 'android',
      configurable: true,
    });
    try {
      expect(toNativeColor('white')).toBe('#FFFFFF');
      expect(toNativeColor('rgba(0, 0, 255, 0.5)')).toBe('#800000FF');
    } finally {
      Object.defineProperty(Platform, 'OS', {
        value: original,
        configurable: true,
      });
    }
  });

  it('passes unknown strings through and drops unsupported values', () => {
    expect(toNativeColor('not-a-color')).toBe('not-a-color');
    expect(toNativeColor(undefined)).toBeUndefined();
    expect(toNativeColor(null)).toBeUndefined();
  });
});

describe('toNativeProps', () => {
  it('leaves the existing string API exactly as it was', () => {
    const props = {
      color: '#00A4A3',
      textColor: '#000000',
      textAlign: 'flex-start',
      verticalAlign: 'bottom',
      fontFamily: 'Georgia',
      fontSize: '32',
      fontWeight: 'bold',
      letterSpacing: '-0.8',
      lineHeight: '36',
      lineSpacing: '12',
      highlightBorderRadius: '6',
      padding: '10',
      paddingLeft: '8',
      paddingRight: '8',
      paddingTop: '4',
      paddingBottom: '4',
      backgroundInsetTop: '14',
      backgroundInsetBottom: '14',
      backgroundInsetLeft: '3',
      backgroundInsetRight: '3',
      text: 'Hello',
      isEditable: true,
      autoFocus: true,
    };
    expect(toNativeProps(props)).toEqual(props);
  });

  it('converts every numeric prop given as a number', () => {
    const native = toNativeProps({
      fontSize: 32,
      letterSpacing: 4,
      lineHeight: 36,
      lineSpacing: 12,
      highlightBorderRadius: 6,
      padding: 10,
      paddingLeft: 8,
      paddingRight: 8,
      paddingTop: 4,
      paddingBottom: 4,
      backgroundInsetTop: 14,
      backgroundInsetBottom: 14,
      backgroundInsetLeft: 3,
      backgroundInsetRight: 3,
    });
    expect(native).toEqual({
      fontSize: '32',
      letterSpacing: '4',
      lineHeight: '36',
      lineSpacing: '12',
      highlightBorderRadius: '6',
      padding: '10',
      paddingLeft: '8',
      paddingRight: '8',
      paddingTop: '4',
      paddingBottom: '4',
      backgroundInsetTop: '14',
      backgroundInsetBottom: '14',
      backgroundInsetLeft: '3',
      backgroundInsetRight: '3',
    });
  });

  it('converts all color props', () => {
    expect(
      toNativeProps({
        color: 'rgb(255, 204, 128)',
        textColor: 'white',
        placeholderTextColor: 'rgba(0, 0, 0, 0.5)',
      })
    ).toEqual({
      color: '#FFCC80',
      textColor: '#FFFFFF',
      placeholderTextColor: '#80000000',
    });
  });

  it('does not add props that were not given', () => {
    expect(toNativeProps({})).toEqual({});
    expect(toNativeProps({ text: 'x' })).toEqual({ text: 'x' });
  });

  describe('editable alias', () => {
    it('maps editable to isEditable', () => {
      expect(toNativeProps({ editable: false })).toEqual({ isEditable: false });
      expect(toNativeProps({ editable: true })).toEqual({ isEditable: true });
    });

    it('keeps isEditable when editable is not given', () => {
      expect(toNativeProps({ isEditable: false })).toEqual({
        isEditable: false,
      });
    });

    it('lets editable win when both are given', () => {
      expect(toNativeProps({ isEditable: true, editable: false })).toEqual({
        isEditable: false,
      });
      expect(toNativeProps({ isEditable: false, editable: true })).toEqual({
        isEditable: true,
      });
    });

    it('never passes editable itself to native', () => {
      expect(toNativeProps({ editable: true })).not.toHaveProperty('editable');
    });
  });

  it('normalizes maxLength to a non-negative integer', () => {
    expect(toNativeProps({ maxLength: 12 })).toEqual({ maxLength: 12 });
    expect(toNativeProps({ maxLength: 3.7 })).toEqual({ maxLength: 3 });
    expect(toNativeProps({ maxLength: 0 })).toEqual({ maxLength: 0 });
    expect(toNativeProps({ maxLength: -5 })).toEqual({ maxLength: 0 });
  });

  it('passes the new string props through', () => {
    const props = {
      placeholder: 'Type here',
      autoCapitalize: 'words' as const,
      keyboardType: 'email-address',
      returnKeyType: 'done',
    };
    expect(toNativeProps(props)).toEqual(props);
  });
});

describe('HighlightTextView component', () => {
  it('renders the native component with converted props', () => {
    const onChange = jest.fn();
    const native = render({
      fontSize: 32,
      color: 'red',
      editable: false,
      text: 'Hi',
      onChange,
    });
    expect(native.fontSize).toBe('32');
    expect(native.color).toBe('#FF0000');
    expect(native.isEditable).toBe(false);
    expect(native.editable).toBeUndefined();
    expect(native.text).toBe('Hi');
    expect(native.onChange).toBe(onChange);
  });

  it('forwards event handlers unchanged', () => {
    const handlers = {
      onFocus: jest.fn(),
      onBlur: jest.fn(),
      onSubmitEditing: jest.fn(),
      onSelectionChange: jest.fn(),
    };
    const native = render(handlers);
    expect(native.onFocus).toBe(handlers.onFocus);
    expect(native.onBlur).toBe(handlers.onBlur);
    expect(native.onSubmitEditing).toBe(handlers.onSubmitEditing);
    expect(native.onSelectionChange).toBe(handlers.onSelectionChange);
  });

  it('dispatches ref methods as native commands', () => {
    const ref = createRef<HighlightTextViewRef>();
    render({ text: 'Hi' }, ref);
    expect(ref.current).not.toBeNull();

    ref.current!.focus();
    expect(Commands.focus).toHaveBeenCalledWith(NODE);

    ref.current!.blur();
    expect(Commands.blur).toHaveBeenCalledWith(NODE);

    ref.current!.clear();
    expect(Commands.clear).toHaveBeenCalledWith(NODE);

    ref.current!.setText('New text');
    expect(Commands.setTextValue).toHaveBeenCalledWith(NODE, 'New text');

    expect(Commands.focus).toHaveBeenCalledTimes(1);
    expect(Commands.blur).toHaveBeenCalledTimes(1);
    expect(Commands.clear).toHaveBeenCalledTimes(1);
    expect(Commands.setTextValue).toHaveBeenCalledTimes(1);
  });
});
