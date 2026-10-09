package com.highlighttext

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

/** Direct event (onFocus, onBlur, onSubmitEditing, onSelectionChange) with a prebuilt payload. */
class HighlightTextEvent(
  surfaceId: Int,
  viewId: Int,
  private val name: String,
  private val data: WritableMap
) : Event<HighlightTextEvent>(surfaceId, viewId) {

  override fun getEventName(): String = name

  // Every selection/focus change matters; never merge consecutive events
  override fun canCoalesce(): Boolean = false

  override fun getEventData(): WritableMap = data

  companion object {
    const val FOCUS = "topFocus"
    const val BLUR = "topBlur"
    const val SUBMIT_EDITING = "topSubmitEditing"
    const val SELECTION_CHANGE = "topSelectionChange"
  }
}
