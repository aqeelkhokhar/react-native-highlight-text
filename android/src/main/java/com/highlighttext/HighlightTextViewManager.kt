package com.highlighttext

import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.viewmanagers.HighlightTextViewManagerInterface
import com.facebook.react.viewmanagers.HighlightTextViewManagerDelegate

@ReactModule(name = HighlightTextViewManager.NAME)
class HighlightTextViewManager : SimpleViewManager<HighlightTextView>(),
  HighlightTextViewManagerInterface<HighlightTextView> {
  private val mDelegate: ViewManagerDelegate<HighlightTextView>

  init {
    mDelegate = HighlightTextViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<HighlightTextView>? {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): HighlightTextView {
    val view = HighlightTextView(context)
    view.onTextChangeListener = { text ->
      val event: WritableMap = Arguments.createMap()
      event.putString("text", text)
      
      val reactContext = context as ReactContext
      reactContext
        .getJSModule(RCTEventEmitter::class.java)
        .receiveEvent(view.id, "onChange", event)
    }
    return view
  }

  @ReactProp(name = "color")
  override fun setColor(view: HighlightTextView?, color: String?) {
    color?.let {
      try {
        view?.setCharacterBackgroundColor(Color.parseColor(it))
      } catch (e: IllegalArgumentException) {
        // Invalid color format, use default
        view?.setCharacterBackgroundColor(Color.parseColor("#FFFF00"))
      }
    }
  }

  @ReactProp(name = "textColor")
  override fun setTextColor(view: HighlightTextView?, value: String?) {
    value?.let {
      try {
        view?.setTextColor(Color.parseColor(it))
      } catch (e: IllegalArgumentException) {
        // Invalid color format, use default
        view?.setTextColor(Color.BLACK)
      }
    }
  }

  @ReactProp(name = "textAlign")
  override fun setTextAlign(view: HighlightTextView?, value: String?) {
    // Parse combined alignment (e.g., "top-left", "bottom-center")
    val parts = value?.split("-") ?: emptyList()
    var horizontalAlign = value
    
    if (parts.size == 2) {
      // Combined format: use the second part for horizontal alignment
      horizontalAlign = parts[1]
    }
    
    // Apply horizontal alignment
    view?.gravity = when (horizontalAlign) {
      "left", "flex-start" -> Gravity.START or Gravity.CENTER_VERTICAL
      "right", "flex-end" -> Gravity.END or Gravity.CENTER_VERTICAL
      "center" -> Gravity.CENTER
      "justify" -> Gravity.START or Gravity.CENTER_VERTICAL // Android doesn't support justify natively
      else -> Gravity.CENTER
    }
  }

  @ReactProp(name = "verticalAlign")
  override fun setVerticalAlign(view: HighlightTextView?, value: String?) {
    // Android EditText doesn't support vertical alignment as easily as iOS
    // This would require custom implementation with layout adjustments
    // For now, we'll keep the default behavior
  }

  @ReactProp(name = "fontFamily")
  override fun setFontFamily(view: HighlightTextView?, value: String?) {
    // Font family handling can be added if needed
  }

  @ReactProp(name = "fontSize")
  override fun setFontSize(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { size ->
      view?.textSize = size
    }
  }

  @ReactProp(name = "padding")
  override fun setPadding(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { padding ->
      view?.setCharPadding(padding, padding, padding, padding)
    }
  }

  @ReactProp(name = "paddingLeft")
  override fun setPaddingLeft(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { padding ->
      view?.setCharPaddingLeft(padding)
    }
  }

  @ReactProp(name = "paddingRight")
  override fun setPaddingRight(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { padding ->
      view?.setCharPaddingRight(padding)
    }
  }

  @ReactProp(name = "paddingTop")
  override fun setPaddingTop(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { padding ->
      view?.setCharPaddingTop(padding)
    }
  }

  @ReactProp(name = "paddingBottom")
  override fun setPaddingBottom(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { padding ->
      view?.setCharPaddingBottom(padding)
    }
  }

  @ReactProp(name = "lineHeight")
  override fun setLineHeight(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { lineHeight ->
      view?.setCustomLineHeight(lineHeight)
    }
  }

  @ReactProp(name = "highlightBorderRadius")
  override fun setHighlightBorderRadius(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { radius ->
      view?.setCornerRadius(radius)
    }
  }

  @ReactProp(name = "backgroundInsetTop")
  override fun setBackgroundInsetTop(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { inset ->
      view?.setBackgroundInsetTop(inset)
    }
  }

  @ReactProp(name = "backgroundInsetBottom")
  override fun setBackgroundInsetBottom(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { inset ->
      view?.setBackgroundInsetBottom(inset)
    }
  }

  @ReactProp(name = "backgroundInsetLeft")
  override fun setBackgroundInsetLeft(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { inset ->
      view?.setBackgroundInsetLeft(inset)
    }
  }

  @ReactProp(name = "backgroundInsetRight")
  override fun setBackgroundInsetRight(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { inset ->
      view?.setBackgroundInsetRight(inset)
    }
  }

  @ReactProp(name = "text")
  override fun setText(view: HighlightTextView?, value: String?) {
    view?.setTextProp(value ?: "")
  }

  @ReactProp(name = "isEditable")
  override fun setIsEditable(view: HighlightTextView?, value: Boolean) {
    view?.apply {
      isFocusable = value
      isFocusableInTouchMode = value
      isEnabled = value
      inputType = if (value) {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
      } else {
        InputType.TYPE_NULL
      }
    }
  }

  companion object {
    const val NAME = "HighlightTextView"
  }
}
