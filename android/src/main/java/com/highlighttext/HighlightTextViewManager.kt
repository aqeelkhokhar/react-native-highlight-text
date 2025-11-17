package com.highlighttext

import android.graphics.Color
import android.graphics.Typeface
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
    var verticalAlign: String? = null
    var horizontalAlign = value
    
    if (parts.size == 2) {
      // Combined format: "top-left", "bottom-center", etc.
      verticalAlign = parts[0]
      horizontalAlign = parts[1]
    } else if (value == "top" || value == "bottom") {
      // Pure vertical alignment
      verticalAlign = value
      horizontalAlign = null
    }
    
    // Determine vertical gravity - preserve existing if not specified
    val vGravity = when (verticalAlign) {
      "top" -> Gravity.TOP
      "bottom" -> Gravity.BOTTOM
      "center" -> Gravity.CENTER_VERTICAL
      null -> view?.gravity?.and(Gravity.VERTICAL_GRAVITY_MASK) ?: Gravity.CENTER_VERTICAL
      else -> Gravity.CENTER_VERTICAL
    }
    
    // Determine horizontal gravity
    val hGravity = when (horizontalAlign) {
      "left", "flex-start" -> Gravity.START
      "right", "flex-end" -> Gravity.END
      "center" -> Gravity.CENTER_HORIZONTAL
      "justify" -> Gravity.START // Android doesn't support justify natively
      else -> Gravity.CENTER_HORIZONTAL
    }
    
    // Apply combined gravity
    view?.gravity = vGravity or hGravity
  }

  @ReactProp(name = "verticalAlign")
  override fun setVerticalAlign(view: HighlightTextView?, value: String?) {
    view?.setVerticalAlign(value)
  }

  @ReactProp(name = "fontFamily")
  override fun setFontFamily(view: HighlightTextView?, value: String?) {
    view?.setFontFamilyProp(value)
  }
  
  @ReactProp(name = "fontWeight")
  override fun setFontWeight(view: HighlightTextView?, value: String?) {
    value?.let { weight ->
      view?.setFontWeight(weight)
    }
  }

  @ReactProp(name = "fontSize")
  override fun setFontSize(view: HighlightTextView?, value: String?) {
    value?.toFloatOrNull()?.let { size ->
      view?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, size)
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
      view?.setHighlightBorderRadius(radius)
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
      // Always keep multiline flag to preserve newlines, even when not editable
      inputType = if (value) {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
      } else {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
      }
      // Prevent keyboard from showing when not editable
      if (!value) {
        setShowSoftInputOnFocus(false)
      }
    }
  }

  companion object {
    const val NAME = "HighlightTextView"
  }
}
