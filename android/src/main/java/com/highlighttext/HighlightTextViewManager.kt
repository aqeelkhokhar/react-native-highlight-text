package com.highlighttext

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
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
    return HighlightTextView(context)
  }

  @ReactProp(name = "color")
  override fun setColor(view: HighlightTextView?, color: String?) {
    view?.setBackgroundColor(Color.parseColor(color))
  }

  companion object {
    const val NAME = "HighlightTextView"
  }
}
