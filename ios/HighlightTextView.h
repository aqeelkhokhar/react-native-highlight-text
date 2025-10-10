#import <React/RCTViewComponentView.h>
#import <UIKit/UIKit.h>

#ifndef HighlightTextViewNativeComponent_h
#define HighlightTextViewNativeComponent_h

NS_ASSUME_NONNULL_BEGIN

@interface RoundedBackgroundLayoutManager : NSLayoutManager
@property (nonatomic, strong) UIColor *backgroundColor;
@property (nonatomic, assign) CGFloat padding;
@property (nonatomic, assign) CGFloat paddingLeft;
@property (nonatomic, assign) CGFloat paddingRight;
@property (nonatomic, assign) CGFloat paddingTop;
@property (nonatomic, assign) CGFloat paddingBottom;
@property (nonatomic, assign) CGFloat cornerRadius;
@end

@interface HighlightTextView : RCTViewComponentView
@end

NS_ASSUME_NONNULL_END

#endif /* HighlightTextViewNativeComponent_h */
