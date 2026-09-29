package com.highlighttext

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class OnChangeEvent(
  surfaceId: Int,
  viewId: Int,
  private val text: String
) : Event<OnChangeEvent>(surfaceId, viewId) {

  override fun getEventName(): String = EVENT_NAME

  override fun getEventData(): WritableMap {
    val eventData = Arguments.createMap()
    eventData.putString("text", text)
    return eventData
  }

  companion object {
    const val EVENT_NAME = "topChange"
  }
}
