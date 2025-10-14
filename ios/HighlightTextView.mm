#import "HighlightTextView.h"

#import <react/renderer/components/HighlightTextViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/HighlightTextViewSpec/EventEmitters.h>
#import <react/renderer/components/HighlightTextViewSpec/Props.h>
#import <react/renderer/components/HighlightTextViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@implementation RoundedBackgroundLayoutManager

- (void)drawBackgroundForGlyphRange:(NSRange)glyphsToShow atPoint:(CGPoint)origin {
    NSTextStorage *textStorage = self.textStorage;
    NSTextContainer *textContainer = self.textContainers.firstObject;
    
    [textStorage enumerateAttribute:NSBackgroundColorAttributeName 
                            inRange:glyphsToShow 
                            options:0 
                         usingBlock:^(id value, NSRange range, BOOL *stop) {
        if (value && [value isKindOfClass:[UIColor class]]) {
            // Draw background for each character individually to avoid line wrapping issues
            for (NSUInteger i = range.location; i < range.location + range.length; i++) {
                NSRange charRange = NSMakeRange(i, 1);
                NSRange glyphRange = [self glyphRangeForCharacterRange:charRange actualCharacterRange:NULL];
                
                if (glyphRange.length > 0) {
                    // Get the actual character to ensure it's not a whitespace or control character
                    unichar character = [textStorage.string characterAtIndex:i];
                    if (character == '\n' || character == '\r' || character == ' ' || character == '\t') {
                        continue;
                    }
                    
                    CGRect boundingRect = [self boundingRectForGlyphRange:glyphRange inTextContainer:textContainer];
                    
                    // Ensure the glyph has reasonable dimensions and isn't a line fragment artifact
                    // Also check that it's not spanning the full container width (which indicates line wrapping issues)
                    if (boundingRect.size.width > 1.0 && boundingRect.size.height > 1.0 && 
                        boundingRect.size.width < (textContainer.size.width * 0.8)) {
                        
                        // Apply individual padding values
                        boundingRect.origin.x += origin.x - self.paddingLeft;
                        boundingRect.origin.y += origin.y - self.paddingTop;
                        boundingRect.size.width += self.paddingLeft + self.paddingRight;
                        boundingRect.size.height += self.paddingTop + self.paddingBottom;
                        
                        UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:boundingRect cornerRadius:self.cornerRadius];
                        [self.backgroundColor setFill];
                        [path fill];
                    }
                }
            }
        }
    }];
    
    // Don't call super to avoid default background drawing
}

@end

@interface HighlightTextView () <RCTHighlightTextViewViewProtocol, UITextViewDelegate>

@end

@implementation HighlightTextView {
    UITextView * _textView;
    RoundedBackgroundLayoutManager * _layoutManager;
    NSString * _characterBackgroundColor;
    CGFloat _padding;
    CGFloat _paddingLeft;
    CGFloat _paddingRight;
    CGFloat _paddingTop;
    CGFloat _paddingBottom;
    CGFloat _cornerRadius;
    BOOL _isUpdatingText;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<HighlightTextViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const HighlightTextViewProps>();
    _props = defaultProps;

    _characterBackgroundColor = @"#FFFF00";
    _padding = 4.0;
    _paddingLeft = 4.0;
    _paddingRight = 4.0;
    _paddingTop = 4.0;
    _paddingBottom = 4.0;
    _cornerRadius = 4.0;
    
    // Create text storage, layout manager, and text container
    NSTextStorage *textStorage = [[NSTextStorage alloc] init];
    _layoutManager = [[RoundedBackgroundLayoutManager alloc] init];
    _layoutManager.backgroundColor = [self hexStringToColor:_characterBackgroundColor];
    _layoutManager.padding = _padding;
    _layoutManager.paddingLeft = _paddingLeft;
    _layoutManager.paddingRight = _paddingRight;
    _layoutManager.paddingTop = _paddingTop;
    _layoutManager.paddingBottom = _paddingBottom;
    _layoutManager.cornerRadius = _cornerRadius;
    
    [textStorage addLayoutManager:_layoutManager];
    
    NSTextContainer *textContainer = [[NSTextContainer alloc] init];
    [_layoutManager addTextContainer:textContainer];
    
    _textView = [[UITextView alloc] initWithFrame:CGRectZero textContainer:textContainer];
    _textView.delegate = self;
    _textView.font = [UIFont systemFontOfSize:32];
    _textView.textAlignment = NSTextAlignmentCenter;
    _textView.textContainerInset = UIEdgeInsetsMake(10, 10, 10, 10);
    _textView.backgroundColor = [UIColor clearColor];
    _textView.editable = YES;
    _textView.scrollEnabled = YES;
    _textView.userInteractionEnabled = YES;

    self.contentView = _textView;
    self.userInteractionEnabled = YES;
  }

  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
    const auto &oldViewProps = *std::static_pointer_cast<HighlightTextViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<HighlightTextViewProps const>(props);

    if (oldViewProps.color != newViewProps.color) {
        _characterBackgroundColor = [[NSString alloc] initWithUTF8String: newViewProps.color.c_str()];
        _layoutManager.backgroundColor = [self hexStringToColor:_characterBackgroundColor];
        [self applyCharacterBackgrounds];
    }
    
    if (oldViewProps.textColor != newViewProps.textColor) {
        NSString *textColorStr = [[NSString alloc] initWithUTF8String: newViewProps.textColor.c_str()];
        _textView.textColor = [self hexStringToColor:textColorStr];
        [self applyCharacterBackgrounds];
    }
    
    if (oldViewProps.textAlign != newViewProps.textAlign) {
        NSString *alignment = [[NSString alloc] initWithUTF8String: newViewProps.textAlign.c_str()];
        if ([alignment isEqualToString:@"center"]) {
            _textView.textAlignment = NSTextAlignmentCenter;
        } else if ([alignment isEqualToString:@"right"]) {
            _textView.textAlignment = NSTextAlignmentRight;
        } else {
            _textView.textAlignment = NSTextAlignmentLeft;
        }
        [self applyCharacterBackgrounds]; // Reapply to update alignment
    }
    
    if (oldViewProps.fontSize != newViewProps.fontSize) {
        NSString *fontSizeStr = [[NSString alloc] initWithUTF8String: newViewProps.fontSize.c_str()];
        CGFloat fontSize = [fontSizeStr floatValue];
        if (fontSize > 0) {
            NSString *fontFamily = _textView.font.familyName;
            _textView.font = [UIFont fontWithName:fontFamily size:fontSize] ?: [UIFont systemFontOfSize:fontSize];
        }
    }
    
    if (oldViewProps.fontFamily != newViewProps.fontFamily) {
        NSString *fontFamily = [[NSString alloc] initWithUTF8String: newViewProps.fontFamily.c_str()];
        CGFloat fontSize = _textView.font.pointSize;
        _textView.font = [UIFont fontWithName:fontFamily size:fontSize] ?: [UIFont systemFontOfSize:fontSize];
    }
    
    if (oldViewProps.padding != newViewProps.padding) {
        NSString *paddingStr = [[NSString alloc] initWithUTF8String: newViewProps.padding.c_str()];
        CGFloat padding = [paddingStr floatValue];
        if (padding > 0) {
            _padding = padding;
            _paddingLeft = _paddingRight = _paddingTop = _paddingBottom = padding;
            _layoutManager.padding = _padding;
            _layoutManager.paddingLeft = _paddingLeft;
            _layoutManager.paddingRight = _paddingRight;
            _layoutManager.paddingTop = _paddingTop;
            _layoutManager.paddingBottom = _paddingBottom;
        }
    }
    
    if (oldViewProps.paddingLeft != newViewProps.paddingLeft) {
        NSString *paddingStr = [[NSString alloc] initWithUTF8String: newViewProps.paddingLeft.c_str()];
        CGFloat padding = [paddingStr floatValue];
        if (padding >= 0) {
            _paddingLeft = padding;
            _layoutManager.paddingLeft = _paddingLeft;
        }
    }
    
    if (oldViewProps.paddingRight != newViewProps.paddingRight) {
        NSString *paddingStr = [[NSString alloc] initWithUTF8String: newViewProps.paddingRight.c_str()];
        CGFloat padding = [paddingStr floatValue];
        if (padding >= 0) {
            _paddingRight = padding;
            _layoutManager.paddingRight = _paddingRight;
        }
    }
    
    if (oldViewProps.paddingTop != newViewProps.paddingTop) {
        NSString *paddingStr = [[NSString alloc] initWithUTF8String: newViewProps.paddingTop.c_str()];
        CGFloat padding = [paddingStr floatValue];
        if (padding >= 0) {
            _paddingTop = padding;
            _layoutManager.paddingTop = _paddingTop;
        }
    }
    
    if (oldViewProps.paddingBottom != newViewProps.paddingBottom) {
        NSString *paddingStr = [[NSString alloc] initWithUTF8String: newViewProps.paddingBottom.c_str()];
        CGFloat padding = [paddingStr floatValue];
        if (padding >= 0) {
            _paddingBottom = padding;
            _layoutManager.paddingBottom = _paddingBottom;
        }
    }
    
    if (oldViewProps.text != newViewProps.text) {
        NSString *text = [[NSString alloc] initWithUTF8String: newViewProps.text.c_str()];
        if (![_textView.text isEqualToString:text]) {
            _isUpdatingText = YES;
            _textView.text = text;
            [self applyCharacterBackgrounds];
            _isUpdatingText = NO;
        }
    }
    
    if (oldViewProps.isEditable != newViewProps.isEditable) {
        _textView.editable = newViewProps.isEditable;
    }

    [super updateProps:props oldProps:oldProps];
}

Class<RCTComponentViewProtocol> HighlightTextViewCls(void)
{
    return HighlightTextView.class;
}

- (void)textViewDidChange:(UITextView *)textView
{
    [self applyCharacterBackgrounds];
    if (!_isUpdatingText) {
        if (_eventEmitter != nullptr) {
            std::dynamic_pointer_cast<const HighlightTextViewEventEmitter>(_eventEmitter)
                ->onChange(HighlightTextViewEventEmitter::OnChange{
                    .text = std::string([textView.text UTF8String] ?: "")
                });
        }
    }
}

- (void)applyCharacterBackgrounds
{
    NSString *text = _textView.text;
    if (text.length == 0) {
        return;
    }
    
    NSMutableAttributedString *attributedString = [[NSMutableAttributedString alloc] initWithString:text];
    UIColor *bgColor = [self hexStringToColor:_characterBackgroundColor];
    
    // Apply font and paragraph style
    [attributedString addAttribute:NSFontAttributeName 
                             value:_textView.font 
                             range:NSMakeRange(0, text.length)];
    
    // Apply text color if available
    if (_textView.textColor) {
        [attributedString addAttribute:NSForegroundColorAttributeName 
                                 value:_textView.textColor 
                                 range:NSMakeRange(0, text.length)];
    }
    
    NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
    paragraphStyle.alignment = _textView.textAlignment;
    [attributedString addAttribute:NSParagraphStyleAttributeName 
                             value:paragraphStyle 
                             range:NSMakeRange(0, text.length)];
    
    for (NSUInteger i = 0; i < text.length; i++) {
        unichar character = [text characterAtIndex:i];
        
        // Skip newlines and spaces for background
        if (character != '\n' && character != ' ') {
            [attributedString addAttribute:NSBackgroundColorAttributeName 
                                     value:bgColor 
                                     range:NSMakeRange(i, 1)];
        }
    }
    
    _textView.attributedText = attributedString;
    [_textView setNeedsDisplay];
}

- (UIColor *)hexStringToColor:(NSString *)stringToConvert
{
    NSString *noHashString = [stringToConvert stringByReplacingOccurrencesOfString:@"#" withString:@""];
    NSScanner *stringScanner = [NSScanner scannerWithString:noHashString];

    unsigned hex;
    if (![stringScanner scanHexInt:&hex]) return nil;
    int r = (hex >> 16) & 0xFF;
    int g = (hex >> 8) & 0xFF;
    int b = (hex) & 0xFF;

    return [UIColor colorWithRed:r / 255.0f green:g / 255.0f blue:b / 255.0f alpha:1.0f];
}

@end
