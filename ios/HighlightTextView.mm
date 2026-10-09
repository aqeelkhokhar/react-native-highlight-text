#import "HighlightTextView.h"

#import <react/renderer/components/HighlightTextViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/HighlightTextViewSpec/EventEmitters.h>
#import <react/renderer/components/HighlightTextViewSpec/Props.h>
#import <react/renderer/components/HighlightTextViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@implementation RoundedBackgroundLayoutManager {
    // Per-font cache of single-character sizes. sizeWithAttributes: is
    // deterministic for a given (character, font), so memoizing it returns the
    // exact same values (pixel-identical drawing) without re-measuring every
    // character on every draw pass.
    NSMutableDictionary<UIFont *, NSMutableDictionary<NSString *, NSValue *> *> *_charSizeCache;
}

@synthesize backgroundColor, padding, paddingLeft, paddingRight, paddingTop, paddingBottom, cornerRadius, highlightBorderRadius, backgroundInsetTop, backgroundInsetBottom, backgroundInsetLeft, backgroundInsetRight;

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
                    unichar character = [textStorage.string characterAtIndex:i];
                    if (character == '\n' || character == '\r' || character == ' ' || character == '\t') {
                        continue;
                    }
                    
                    CGPoint glyphLocation = [self locationForGlyphAtIndex:glyphRange.location];
                    CGRect lineRect = [self lineFragmentRectForGlyphAtIndex:glyphRange.location effectiveRange:NULL];
                    
                    NSDictionary *attributes = [textStorage attributesAtIndex:i effectiveRange:NULL];
                    UIFont *font = attributes[NSFontAttributeName];
                    NSString *charString = [textStorage.string substringWithRange:NSMakeRange(i, 1)];
                    CGSize charSize = [self cachedSizeOfCharacter:charString font:font];
                    
                    CGRect boundingRect = CGRectMake(
                        glyphLocation.x,
                        lineRect.origin.y,
                        charSize.width,
                        lineRect.size.height
                    );
                    
                    if (boundingRect.size.width > 1.0 && boundingRect.size.height > 1.0) {
                        
                        boundingRect.origin.y += self.backgroundInsetTop;
                        boundingRect.size.height -= (self.backgroundInsetTop + self.backgroundInsetBottom);
                        boundingRect.origin.x += self.backgroundInsetLeft;
                        boundingRect.size.width -= (self.backgroundInsetLeft + self.backgroundInsetRight);
                        
                        // Then apply padding (expands outward)
                        boundingRect.origin.x += origin.x - self.paddingLeft;
                        boundingRect.origin.y += origin.y - self.paddingTop;
                        boundingRect.size.width += self.paddingLeft + self.paddingRight;
                        boundingRect.size.height += self.paddingTop + self.paddingBottom;
                        
                        // Use highlightBorderRadius if specified, otherwise use cornerRadius
                        CGFloat radius = self.highlightBorderRadius > 0 ? self.highlightBorderRadius : self.cornerRadius;
                        UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:boundingRect cornerRadius:radius];
                        [self.backgroundColor setFill];
                        [path fill];
                    }
                }
            }
        }
    }];
    
    // Don't call super to avoid default background drawing
}

- (CGSize)cachedSizeOfCharacter:(NSString *)charString font:(UIFont *)font
{
    if (!font) {
        return [charString sizeWithAttributes:@{}];
    }
    if (!_charSizeCache) {
        _charSizeCache = [NSMutableDictionary new];
    }
    NSMutableDictionary<NSString *, NSValue *> *sizes = _charSizeCache[font];
    if (!sizes) {
        // Fonts only change on prop updates; keep the cache small.
        if (_charSizeCache.count >= 8) {
            [_charSizeCache removeAllObjects];
        }
        sizes = [NSMutableDictionary new];
        _charSizeCache[font] = sizes;
    }
    NSValue *cached = sizes[charString];
    if (cached) {
        return cached.CGSizeValue;
    }
    CGSize size = [charString sizeWithAttributes:@{NSFontAttributeName: font}];
    sizes[charString] = [NSValue valueWithCGSize:size];
    return size;
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
    CGFloat _highlightBorderRadius;
    CGFloat _backgroundInsetTop;
    CGFloat _backgroundInsetBottom;
    CGFloat _backgroundInsetLeft;
    CGFloat _backgroundInsetRight;
    CGFloat _lineHeight;
    CGFloat _fontSize;
    CGFloat _letterSpacing; // in layout points, matches React Native's letterSpacing
    NSString * _fontFamily;
    NSString * _fontWeight;
    BOOL _isUpdatingText;
    NSString * _currentVerticalAlignment;
    NSTextAlignment _currentHorizontalAlignment;
    UILabel * _placeholderLabel;
    NSInteger _maxLength; // -1 = no limit
    BOOL _submitOnReturn; // returnKeyType set: Return fires onSubmitEditing instead of a new line
}

+ (BOOL)shouldBeRecycled
{
    // updateProps only applies props that differ from the previous props, and the
    // native state (padding, insets, radius, fonts, alignment...) lives in ivars.
    // A recycled view would carry that state over to the next view, so always
    // start from a freshly initialised view instead.
    return NO;
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
    _highlightBorderRadius = 0.0;
    _backgroundInsetTop = 0.0;
    _backgroundInsetBottom = 0.0;
    _backgroundInsetLeft = 0.0;
    _backgroundInsetRight = 0.0;
    _lineHeight = 0.0; // 0 means use default line height
    _fontSize = 32.0; // Default font size
    _letterSpacing = 0.0; // Default: no extra spacing
    _fontFamily = nil;
    _fontWeight = @"normal";
    _currentVerticalAlignment = nil;
    _currentHorizontalAlignment = NSTextAlignmentLeft; // README default: left
    _maxLength = -1;
    _submitOnReturn = NO;
    
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
    _layoutManager.highlightBorderRadius = _highlightBorderRadius;
    _layoutManager.backgroundInsetTop = _backgroundInsetTop;
    _layoutManager.backgroundInsetBottom = _backgroundInsetBottom;
    _layoutManager.backgroundInsetLeft = _backgroundInsetLeft;
    _layoutManager.backgroundInsetRight = _backgroundInsetRight;
    
    [textStorage addLayoutManager:_layoutManager];
    
    NSTextContainer *textContainer = [[NSTextContainer alloc] init];
    [_layoutManager addTextContainer:textContainer];
    
    _textView = [[UITextView alloc] initWithFrame:CGRectZero textContainer:textContainer];
    _textView.delegate = self;
    _textView.font = [UIFont systemFontOfSize:32];
    _textView.textAlignment = NSTextAlignmentLeft;
    _textView.textContainerInset = UIEdgeInsetsMake(10, 10, 10, 10);
    _textView.backgroundColor = [UIColor clearColor];
    _textView.editable = YES; // matches the codegen default (WithDefault<boolean, true>)
    _textView.scrollEnabled = YES;
    _textView.userInteractionEnabled = YES;

    _placeholderLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _placeholderLabel.numberOfLines = 0;
    _placeholderLabel.textColor = [UIColor placeholderTextColor];
    _placeholderLabel.font = _textView.font;
    _placeholderLabel.hidden = YES;
    _placeholderLabel.userInteractionEnabled = NO;
    [_textView addSubview:_placeholderLabel];

    self.contentView = _textView;
    self.userInteractionEnabled = YES;
  }

  return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    
    // Recalculate vertical alignment after layout
    if (_currentVerticalAlignment) {
        [self updateVerticalAlignment:_currentVerticalAlignment];
    }
    [self layoutPlaceholder];
}

- (void)updateVerticalAlignment:(NSString *)verticalAlign
{
    [self applyVerticalAlignment:verticalAlign];
    [self layoutPlaceholder];
}

- (void)applyVerticalAlignment:(NSString *)verticalAlign
{
    if ([verticalAlign isEqualToString:@"top"]) {
        _textView.textContainerInset = UIEdgeInsetsMake(10, 10, 0, 10);
    } else if ([verticalAlign isEqualToString:@"bottom"]) {
        // Force layout to get accurate content height
        [_textView.layoutManager ensureLayoutForTextContainer:_textView.textContainer];
        
        CGFloat contentHeight = [_textView.layoutManager usedRectForTextContainer:_textView.textContainer].size.height;
        CGFloat viewHeight = _textView.bounds.size.height;
        
        // Only apply bottom alignment if we have valid dimensions
        if (viewHeight > 0 && contentHeight > 0) {
            if (contentHeight + 20 <= viewHeight) {
                // Content fits in view - align to bottom with inset
                CGFloat topInset = MAX(10, viewHeight - contentHeight - 10);
                _textView.textContainerInset = UIEdgeInsetsMake(topInset, 10, 10, 10);
            } else {
                // Content exceeds view - use minimal inset and scroll to bottom
                _textView.textContainerInset = UIEdgeInsetsMake(10, 10, 10, 10);
                
                // Scroll to bottom to show the latest text
                dispatch_async(dispatch_get_main_queue(), ^{
                    CGFloat bottomOffset = self->_textView.contentSize.height - self->_textView.bounds.size.height + self->_textView.contentInset.bottom;
                    if (bottomOffset > 0) {
                        [self->_textView setContentOffset:CGPointMake(0, bottomOffset) animated:NO];
                    }
                });
            }
        }
    } else if ([verticalAlign isEqualToString:@"center"] || [verticalAlign isEqualToString:@"middle"]) {
        [_textView.layoutManager ensureLayoutForTextContainer:_textView.textContainer];

        CGFloat contentHeight = [_textView.layoutManager usedRectForTextContainer:_textView.textContainer].size.height;
        CGFloat viewHeight = _textView.bounds.size.height;

        if (viewHeight > 0 && contentHeight > 0 && contentHeight + 20 <= viewHeight) {
            // Split the free space evenly above and below the text (min 10, like the other modes)
            CGFloat freeSpace = viewHeight - contentHeight;
            CGFloat topInset = MAX(10, floor(freeSpace / 2.0));
            CGFloat bottomInset = MAX(10, freeSpace - topInset);
            _textView.textContainerInset = UIEdgeInsetsMake(topInset, 10, bottomInset, 10);
        } else {
            _textView.textContainerInset = UIEdgeInsetsMake(10, 10, 10, 10);
        }
    } else {
        // Default
        _textView.textContainerInset = UIEdgeInsetsMake(10, 10, 10, 10);
    }
}

- (void)layoutPlaceholder
{
    if (!_placeholderLabel) {
        return;
    }
    BOOL hasPlaceholder = _placeholderLabel.text.length > 0;
    _placeholderLabel.hidden = !hasPlaceholder || _textView.text.length > 0;
    if (_placeholderLabel.hidden) {
        return;
    }
    UIEdgeInsets inset = _textView.textContainerInset;
    CGFloat linePadding = _textView.textContainer.lineFragmentPadding;
    CGFloat width = MAX(0, _textView.bounds.size.width - inset.left - inset.right - 2 * linePadding);
    CGSize fitted = [_placeholderLabel sizeThatFits:CGSizeMake(width, CGFLOAT_MAX)];
    _placeholderLabel.frame = CGRectMake(inset.left + linePadding, inset.top, width, ceil(fitted.height));
}

- (void)updatePlaceholderStyle
{
    _placeholderLabel.font = _textView.font;
    _placeholderLabel.textAlignment =
        _currentHorizontalAlignment == NSTextAlignmentJustified ? NSTextAlignmentLeft : _currentHorizontalAlignment;
    [self layoutPlaceholder];
}

- (void)reloadKeyboardIfNeeded
{
    if (_textView.isFirstResponder) {
        [_textView reloadInputViews];
    }
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
        
        // Parse combined alignment (e.g., "top-left", "bottom-center")
        NSArray *parts = [alignment componentsSeparatedByString:@"-"];
        NSString *verticalPart = nil;
        NSString *horizontalPart = nil;
        
        if (parts.count == 2) {
            // Combined format: "top-left", "bottom-center", etc.
            verticalPart = parts[0];
            horizontalPart = parts[1];
        } else {
            // Single value - determine if it's horizontal or vertical
            if ([alignment isEqualToString:@"top"] || [alignment isEqualToString:@"bottom"]) {
                verticalPart = alignment;
            } else {
                horizontalPart = alignment;
            }
        }
        
        // Apply horizontal alignment
        if (horizontalPart) {
            if ([horizontalPart isEqualToString:@"center"]) {
                _currentHorizontalAlignment = NSTextAlignmentCenter;
            } else if ([horizontalPart isEqualToString:@"right"] || [horizontalPart isEqualToString:@"flex-end"]) {
                _currentHorizontalAlignment = NSTextAlignmentRight;
            } else if ([horizontalPart isEqualToString:@"left"] || [horizontalPart isEqualToString:@"flex-start"]) {
                _currentHorizontalAlignment = NSTextAlignmentLeft;
            } else if ([horizontalPart isEqualToString:@"justify"]) {
                _currentHorizontalAlignment = NSTextAlignmentJustified;
            } else {
                _currentHorizontalAlignment = NSTextAlignmentLeft;
            }
            _textView.textAlignment = _currentHorizontalAlignment;
        }
        
        // Store and apply vertical alignment
        if (verticalPart) {
            _currentVerticalAlignment = verticalPart;
            [self updateVerticalAlignment:verticalPart];
        } else if (!verticalPart && horizontalPart) {
            // Preserve existing vertical alignment when only horizontal alignment is specified
            if (_currentVerticalAlignment) {
                [self updateVerticalAlignment:_currentVerticalAlignment];
            } else {
                // Default vertical centering only if no vertical alignment was previously set
                _textView.textContainerInset = UIEdgeInsetsMake(10, 10, 10, 10);
            }
        }
        
        [self applyCharacterBackgrounds]; // Reapply to update alignment
        [self updatePlaceholderStyle];
    }
    
    if (oldViewProps.fontSize != newViewProps.fontSize) {
        NSString *fontSizeStr = [[NSString alloc] initWithUTF8String: newViewProps.fontSize.c_str()];
        CGFloat fontSize = [fontSizeStr floatValue];
        if (fontSize > 0) {
            _fontSize = fontSize;
            [self updateFont];
        }
    }
    
    if (oldViewProps.lineHeight != newViewProps.lineHeight) {
        NSString *lineHeightStr = [[NSString alloc] initWithUTF8String: newViewProps.lineHeight.c_str()];
        CGFloat lineHeight = [lineHeightStr floatValue];
        if (lineHeight >= 0) {
            _lineHeight = lineHeight;
            [self applyCharacterBackgrounds]; // Reapply to update line height
        }
    }
    
    if (oldViewProps.letterSpacing != newViewProps.letterSpacing) {
        NSString *spacingStr = [[NSString alloc] initWithUTF8String: newViewProps.letterSpacing.c_str()];
        CGFloat spacing = [spacingStr floatValue];
        _letterSpacing = spacing;
        [self applyCharacterBackgrounds]; // Reapply to update glyph layout
    }
    
    if (oldViewProps.highlightBorderRadius != newViewProps.highlightBorderRadius) {
        NSString *radiusStr = [[NSString alloc] initWithUTF8String: newViewProps.highlightBorderRadius.c_str()];
        CGFloat radius = [radiusStr floatValue];
        if (radius >= 0) {
            _highlightBorderRadius = radius;
            _layoutManager.highlightBorderRadius = _highlightBorderRadius;
            [self applyCharacterBackgrounds]; // Reapply to update highlight border radius
        }
    }
    
    if (oldViewProps.fontFamily != newViewProps.fontFamily) {
        _fontFamily = [[NSString alloc] initWithUTF8String: newViewProps.fontFamily.c_str()];
        [self updateFont];
    }
    
    if (oldViewProps.fontWeight != newViewProps.fontWeight) {
        _fontWeight = [[NSString alloc] initWithUTF8String: newViewProps.fontWeight.c_str()];
        [self updateFont];
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
    
    if (oldViewProps.backgroundInsetTop != newViewProps.backgroundInsetTop) {
        NSString *insetStr = [[NSString alloc] initWithUTF8String: newViewProps.backgroundInsetTop.c_str()];
        CGFloat inset = [insetStr floatValue];
        if (inset >= 0) {
            _backgroundInsetTop = inset;
            _layoutManager.backgroundInsetTop = _backgroundInsetTop;
            [self applyCharacterBackgrounds];
        }
    }
    
    if (oldViewProps.backgroundInsetBottom != newViewProps.backgroundInsetBottom) {
        NSString *insetStr = [[NSString alloc] initWithUTF8String: newViewProps.backgroundInsetBottom.c_str()];
        CGFloat inset = [insetStr floatValue];
        if (inset >= 0) {
            _backgroundInsetBottom = inset;
            _layoutManager.backgroundInsetBottom = _backgroundInsetBottom;
            [self applyCharacterBackgrounds];
        }
    }
    
    if (oldViewProps.backgroundInsetLeft != newViewProps.backgroundInsetLeft) {
        NSString *insetStr = [[NSString alloc] initWithUTF8String: newViewProps.backgroundInsetLeft.c_str()];
        CGFloat inset = [insetStr floatValue];
        if (inset >= 0) {
            _backgroundInsetLeft = inset;
            _layoutManager.backgroundInsetLeft = _backgroundInsetLeft;
            [self applyCharacterBackgrounds];
        }
    }
    
    if (oldViewProps.backgroundInsetRight != newViewProps.backgroundInsetRight) {
        NSString *insetStr = [[NSString alloc] initWithUTF8String: newViewProps.backgroundInsetRight.c_str()];
        CGFloat inset = [insetStr floatValue];
        if (inset >= 0) {
            _backgroundInsetRight = inset;
            _layoutManager.backgroundInsetRight = _backgroundInsetRight;
            [self applyCharacterBackgrounds];
        }
    }
    
    if (oldViewProps.text != newViewProps.text) {
        NSString *text = [[NSString alloc] initWithUTF8String: newViewProps.text.c_str()];
        if (![_textView.text isEqualToString:text]) {
            _isUpdatingText = YES;
            _textView.text = text;
            [self applyCharacterBackgrounds];
            
            // Recalculate vertical alignment when text changes
            if (_currentVerticalAlignment) {
                [self updateVerticalAlignment:_currentVerticalAlignment];
            }
            [self layoutPlaceholder];
            
            _isUpdatingText = NO;
        }
    }
    
    if (oldViewProps.isEditable != newViewProps.isEditable) {
        _textView.editable = newViewProps.isEditable;
    }
    
    if (oldViewProps.autoFocus != newViewProps.autoFocus) {
        if (newViewProps.autoFocus && _textView.isEditable) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self->_textView becomeFirstResponder];
                NSUInteger textLength = self->_textView.text.length;
                self->_textView.selectedRange = NSMakeRange(textLength, 0);
            });
        }
    }
    
    if (oldViewProps.verticalAlign != newViewProps.verticalAlign) {
        NSString *verticalAlign = [[NSString alloc] initWithUTF8String: newViewProps.verticalAlign.c_str()];
        _currentVerticalAlignment = verticalAlign;
        [self updateVerticalAlignment:verticalAlign];
        [self applyCharacterBackgrounds];
    }

    if (oldViewProps.placeholder != newViewProps.placeholder) {
        _placeholderLabel.text = [[NSString alloc] initWithUTF8String: newViewProps.placeholder.c_str()];
        [self updatePlaceholderStyle];
    }

    if (oldViewProps.placeholderTextColor != newViewProps.placeholderTextColor) {
        NSString *colorStr = [[NSString alloc] initWithUTF8String: newViewProps.placeholderTextColor.c_str()];
        UIColor *color = colorStr.length > 0 ? [self hexStringToColor:colorStr] : nil;
        _placeholderLabel.textColor = color ?: [UIColor placeholderTextColor];
    }

    if (oldViewProps.maxLength != newViewProps.maxLength) {
        _maxLength = newViewProps.maxLength;
    }

    if (oldViewProps.autoCapitalize != newViewProps.autoCapitalize) {
        NSString *value = [[NSString alloc] initWithUTF8String: newViewProps.autoCapitalize.c_str()];
        UITextAutocapitalizationType type = UITextAutocapitalizationTypeSentences; // UITextView default
        if ([value isEqualToString:@"none"]) {
            type = UITextAutocapitalizationTypeNone;
        } else if ([value isEqualToString:@"words"]) {
            type = UITextAutocapitalizationTypeWords;
        } else if ([value isEqualToString:@"characters"]) {
            type = UITextAutocapitalizationTypeAllCharacters;
        }
        _textView.autocapitalizationType = type;
        [self reloadKeyboardIfNeeded];
    }

    if (oldViewProps.keyboardType != newViewProps.keyboardType) {
        NSString *value = [[NSString alloc] initWithUTF8String: newViewProps.keyboardType.c_str()];
        static NSDictionary<NSString *, NSNumber *> *keyboardTypes;
        static dispatch_once_t onceToken;
        dispatch_once(&onceToken, ^{
            keyboardTypes = @{
                @"default": @(UIKeyboardTypeDefault),
                @"email-address": @(UIKeyboardTypeEmailAddress),
                @"numeric": @(UIKeyboardTypeDecimalPad),
                @"phone-pad": @(UIKeyboardTypePhonePad),
                @"number-pad": @(UIKeyboardTypeNumberPad),
                @"decimal-pad": @(UIKeyboardTypeDecimalPad),
                @"url": @(UIKeyboardTypeURL),
                @"ascii-capable": @(UIKeyboardTypeASCIICapable),
                @"numbers-and-punctuation": @(UIKeyboardTypeNumbersAndPunctuation),
                @"visible-password": @(UIKeyboardTypeASCIICapable),
            };
        });
        NSNumber *type = keyboardTypes[value];
        _textView.keyboardType = type ? (UIKeyboardType)type.integerValue : UIKeyboardTypeDefault;
        [self reloadKeyboardIfNeeded];
    }

    if (oldViewProps.returnKeyType != newViewProps.returnKeyType) {
        NSString *value = [[NSString alloc] initWithUTF8String: newViewProps.returnKeyType.c_str()];
        static NSDictionary<NSString *, NSNumber *> *returnKeyTypes;
        static dispatch_once_t onceToken;
        dispatch_once(&onceToken, ^{
            returnKeyTypes = @{
                @"done": @(UIReturnKeyDone),
                @"go": @(UIReturnKeyGo),
                @"next": @(UIReturnKeyNext),
                @"search": @(UIReturnKeySearch),
                @"send": @(UIReturnKeySend),
                @"join": @(UIReturnKeyJoin),
                @"route": @(UIReturnKeyRoute),
                @"previous": @(UIReturnKeyDefault),
                @"none": @(UIReturnKeyDefault),
            };
        });
        NSNumber *type = returnKeyTypes[value];
        _textView.returnKeyType = type ? (UIReturnKeyType)type.integerValue : UIReturnKeyDefault;
        _submitOnReturn = value.length > 0 && ![value isEqualToString:@"default"];
        [self reloadKeyboardIfNeeded];
    }

    [super updateProps:props oldProps:oldProps];
}

Class<RCTComponentViewProtocol> HighlightTextViewCls(void)
{
    return HighlightTextView.class;
}

- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args
{
    RCTHighlightTextViewHandleCommand(self, commandName, args);
}

- (void)focus
{
    if (_textView.isEditable) {
        [_textView becomeFirstResponder];
    }
}

- (void)blur
{
    [_textView resignFirstResponder];
}

- (void)clear
{
    [self setTextValue:@""];
}

- (void)setTextValue:(NSString *)text
{
    NSString *newText = text ?: @"";
    if (![_textView.text isEqualToString:newText]) {
        _isUpdatingText = YES;
        _textView.text = newText;
        [self applyCharacterBackgrounds];
        if (_currentVerticalAlignment) {
            [self updateVerticalAlignment:_currentVerticalAlignment];
        }
        [self layoutPlaceholder];
        _isUpdatingText = NO;
    }
    // Report the new text so a controlled `text` prop can follow it
    [self emitChange:newText];
}

- (void)emitChange:(NSString *)text
{
    if (_eventEmitter != nullptr) {
        std::dynamic_pointer_cast<const HighlightTextViewEventEmitter>(_eventEmitter)
            ->onChange(HighlightTextViewEventEmitter::OnChange{
                .text = std::string([text UTF8String] ?: "")
            });
    }
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text
{
    if (_submitOnReturn && [text isEqualToString:@"\n"]) {
        if (_eventEmitter != nullptr) {
            std::dynamic_pointer_cast<const HighlightTextViewEventEmitter>(_eventEmitter)
                ->onSubmitEditing(HighlightTextViewEventEmitter::OnSubmitEditing{
                    .text = std::string([textView.text UTF8String] ?: "")
                });
        }
        return NO;
    }

    if (_maxLength >= 0 && textView.markedTextRange == nil) {
        NSUInteger keptLength = textView.text.length - range.length;
        if (keptLength + text.length > (NSUInteger)_maxLength) {
            NSInteger allowed = _maxLength - (NSInteger)keptLength;
            if (allowed > 0 && text.length > 0) {
                // Insert as much as fits, without splitting a composed character
                NSRange fit = [text rangeOfComposedCharacterSequencesForRange:NSMakeRange(0, allowed)];
                while (fit.length > (NSUInteger)allowed && fit.length > 0) {
                    NSRange last = [text rangeOfComposedCharacterSequenceAtIndex:fit.length - 1];
                    fit.length = last.location;
                }
                if (fit.length > 0) {
                    UITextPosition *start = [textView positionFromPosition:textView.beginningOfDocument offset:range.location];
                    UITextPosition *end = start ? [textView positionFromPosition:start offset:range.length] : nil;
                    if (start && end) {
                        [textView replaceRange:[textView textRangeFromPosition:start toPosition:end]
                                      withText:[text substringWithRange:fit]];
                    }
                }
            }
            return NO;
        }
    }
    return YES;
}

- (void)textViewDidBeginEditing:(UITextView *)textView
{
    if (_eventEmitter != nullptr) {
        std::dynamic_pointer_cast<const HighlightTextViewEventEmitter>(_eventEmitter)
            ->onFocus(HighlightTextViewEventEmitter::OnFocus{
                .target = (int)self.tag
            });
    }
}

- (void)textViewDidEndEditing:(UITextView *)textView
{
    if (_eventEmitter != nullptr) {
        std::dynamic_pointer_cast<const HighlightTextViewEventEmitter>(_eventEmitter)
            ->onBlur(HighlightTextViewEventEmitter::OnBlur{
                .target = (int)self.tag
            });
    }
}

- (void)textViewDidChangeSelection:(UITextView *)textView
{
    if (_eventEmitter != nullptr) {
        NSRange selected = textView.selectedRange;
        HighlightTextViewEventEmitter::OnSelectionChange event{};
        event.selection.start = (int)selected.location;
        event.selection.end = (int)(selected.location + selected.length);
        std::dynamic_pointer_cast<const HighlightTextViewEventEmitter>(_eventEmitter)->onSelectionChange(event);
    }
}

- (void)textViewDidChange:(UITextView *)textView
{
    [self applyCharacterBackgrounds];
    [self layoutPlaceholder];
    
    // Recalculate vertical alignment when text changes
    if (_currentVerticalAlignment) {
        [self updateVerticalAlignment:_currentVerticalAlignment];
    }
    
    if (!_isUpdatingText) {
        if (_eventEmitter != nullptr) {
            std::dynamic_pointer_cast<const HighlightTextViewEventEmitter>(_eventEmitter)
                ->onChange(HighlightTextViewEventEmitter::OnChange{
                    .text = std::string([textView.text UTF8String] ?: "")
                });
        }
    }
}

- (void)updateFont
{
    CGFloat fontSize = _fontSize > 0 ? _fontSize : 32.0;
    UIFont *newFont = nil;
    
    // Parse font weight
    UIFontWeight fontWeight = UIFontWeightRegular;
    if (_fontWeight) {
        if ([_fontWeight isEqualToString:@"bold"] || [_fontWeight isEqualToString:@"700"]) {
            fontWeight = UIFontWeightBold;
        } else if ([_fontWeight isEqualToString:@"100"]) {
            fontWeight = UIFontWeightUltraLight;
        } else if ([_fontWeight isEqualToString:@"200"]) {
            fontWeight = UIFontWeightThin;
        } else if ([_fontWeight isEqualToString:@"300"]) {
            fontWeight = UIFontWeightLight;
        } else if ([_fontWeight isEqualToString:@"400"] || [_fontWeight isEqualToString:@"normal"]) {
            fontWeight = UIFontWeightRegular;
        } else if ([_fontWeight isEqualToString:@"500"]) {
            fontWeight = UIFontWeightMedium;
        } else if ([_fontWeight isEqualToString:@"600"]) {
            fontWeight = UIFontWeightSemibold;
        } else if ([_fontWeight isEqualToString:@"700"]) {
            fontWeight = UIFontWeightBold;
        } else if ([_fontWeight isEqualToString:@"800"]) {
            fontWeight = UIFontWeightHeavy;
        } else if ([_fontWeight isEqualToString:@"900"]) {
            fontWeight = UIFontWeightBlack;
        }
    }
    
    if (_fontFamily && _fontFamily.length > 0) {
        // Try to get custom font with weight
        UIFontDescriptor *fontDescriptor = [UIFontDescriptor fontDescriptorWithName:_fontFamily size:fontSize];
        UIFontDescriptor *weightedDescriptor = [fontDescriptor fontDescriptorByAddingAttributes:@{
            UIFontDescriptorTraitsAttribute: @{
                UIFontWeightTrait: @(fontWeight)
            }
        }];
        newFont = [UIFont fontWithDescriptor:weightedDescriptor size:fontSize];
        
        // Fallback if custom font not found
        if (!newFont) {
            newFont = [UIFont systemFontOfSize:fontSize weight:fontWeight];
        }
    } else {
        // Use system font with weight
        newFont = [UIFont systemFontOfSize:fontSize weight:fontWeight];
    }
    
    _textView.font = newFont;
    [self applyCharacterBackgrounds];

    // Font metrics (ascender/descender, line height) changed: re-measure so the
    // highlight, vertical alignment and placeholder follow the new font without
    // the view having to be remounted (the old `key={fontFamily}` workaround).
    [_textView.layoutManager invalidateLayoutForCharacterRange:NSMakeRange(0, _textView.textStorage.length)
                                          actualCharacterRange:NULL];
    [_textView.layoutManager ensureLayoutForTextContainer:_textView.textContainer];
    if (_currentVerticalAlignment) {
        [self updateVerticalAlignment:_currentVerticalAlignment];
    }
    [self updatePlaceholderStyle];
    [_textView setNeedsDisplay];
    [self setNeedsLayout];
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
    
    // Apply letter spacing (kern) if specified
    if (_letterSpacing != 0) {
        [attributedString addAttribute:NSKernAttributeName
                                 value:@(_letterSpacing)
                                 range:NSMakeRange(0, text.length)];
    }
    
    // Apply text color if available
    if (_textView.textColor) {
        [attributedString addAttribute:NSForegroundColorAttributeName 
                                 value:_textView.textColor 
                                 range:NSMakeRange(0, text.length)];
    }
    
    NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
    paragraphStyle.alignment = _textView.textAlignment;
    
    // Apply line height if specified (allows tight line spacing for touching backgrounds)
    if (_lineHeight > 0) {
        // Always use absolute line height for precise control
        paragraphStyle.minimumLineHeight = _lineHeight;
        paragraphStyle.maximumLineHeight = _lineHeight;
        paragraphStyle.lineHeightMultiple = 0;
        
        // For tight line spacing, reduce line spacing to bring lines closer
        if (_lineHeight <= _textView.font.pointSize * 1.2) {
            // Negative line spacing brings lines closer together
            paragraphStyle.lineSpacing = -(_textView.font.pointSize * 0.1);
        }
    }
    
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
    // 8 digits = #AARRGGBB (same as Android's Color.parseColor); 6 digits stay opaque
    CGFloat alpha = noHashString.length == 8 ? ((hex >> 24) & 0xFF) / 255.0f : 1.0f;

    return [UIColor colorWithRed:r / 255.0f green:g / 255.0f blue:b / 255.0f alpha:alpha];
}

@end
