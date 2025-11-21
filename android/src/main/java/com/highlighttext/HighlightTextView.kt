package com.highlighttext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText

/**
 * Custom EditText that mimics the iOS implementation by drawing per-character
 * rounded highlights directly in onDraw(), instead of using spans.
 *
 * This avoids layout thrashing/flicker when lines auto-wrap and keeps padding
 * logic independent from Android's line breaking.
 */
class HighlightTextView : AppCompatEditText {
  // Visual props
  private var characterBackgroundColor: Int = Color.parseColor("#FFFF00")
  private var textColorValue: Int = Color.BLACK
  private var cornerRadius: Float = 4f
  private var highlightBorderRadius: Float = 0f

  // Per-character padding
  private var charPaddingLeft: Float = 4f
  private var charPaddingRight: Float = 4f
  private var charPaddingTop: Float = 4f
  private var charPaddingBottom: Float = 4f

  // Background insets (shrink from line box)
  private var backgroundInsetTop: Float = 0f
  private var backgroundInsetBottom: Float = 0f
  private var backgroundInsetLeft: Float = 0f
  private var backgroundInsetRight: Float = 0f

  // Line height control
  private var customLineHeight: Float = 0f

  // Font + alignment state
  private var currentFontFamily: String? = null
  private var currentFontWeight: String = "normal"
  private var currentVerticalAlign: String? = null

  // Internal flags
  private var isUpdatingText: Boolean = false

  // Drawing helpers
  private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }
  private val backgroundRect = RectF()

  var onTextChangeListener: ((String) -> Unit)? = null

  constructor(context: Context?) : super(context!!) {
    init()
  }

  constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
    init()
  }

  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context!!,
    attrs,
    defStyleAttr
  ) {
    init()
  }

  private fun init() {
    setBackgroundColor(Color.TRANSPARENT)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
    gravity = Gravity.CENTER
    setPadding(20, 20, 20, 20)
    textColorValue = currentTextColor

    // Enable text wrapping
    maxLines = Int.MAX_VALUE
    isSingleLine = false
    setHorizontallyScrolling(false)

    applyLineHeightAndSpacing()

    addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(s: Editable?) {
        if (!isUpdatingText) {
          onTextChangeListener?.invoke(s?.toString() ?: "")
          // Text changed → redraw backgrounds
          invalidate()
        }
      }
    })
  }

  // --- Drawing -----------------------------------------------------------------

  override fun onDraw(canvas: Canvas) {
    val layout = layout
    val text = text

    if (layout != null && text != null && text.isNotEmpty()) {
      val save = canvas.save()

      // Canvas is already translated by scrollX/scrollY in View.draw().
      // Here we only need to account for view padding so our coordinates
      // match the Layout's internal coordinate system.
      val translateX = totalPaddingLeft
      val translateY = totalPaddingTop
      canvas.translate(translateX.toFloat(), translateY.toFloat())

      drawCharacterBackgrounds(canvas, text, layout)

      canvas.restoreToCount(save)
    }

    // Let EditText draw text, cursor, selection, etc. on top of backgrounds
    super.onDraw(canvas)
  }

  private fun drawCharacterBackgrounds(canvas: Canvas, text: CharSequence, layout: android.text.Layout) {
    backgroundPaint.color = characterBackgroundColor
    val paint = paint
    val radius = if (highlightBorderRadius > 0f) highlightBorderRadius else cornerRadius

    val length = text.length
    if (length == 0) return

    for (i in 0 until length) {
      val ch = text[i]
      // Match iOS: skip spaces and control characters for background
      if (ch == '\n' || ch == '\t' || ch == ' ') continue

      val line = layout.getLineForOffset(i)
      val lineStart = layout.getLineStart(line)
      val lineEnd = layout.getLineEnd(line)

      // Horizontal bounds based on layout positions
      val xStart = layout.getPrimaryHorizontal(i)
      val isLastCharInLine = i == lineEnd - 1
      val xEnd = if (!isLastCharInLine && i + 1 < length) {
        layout.getPrimaryHorizontal(i + 1)
      } else {
        // Fallback for last character in the line/text
        xStart + paint.measureText(text, i, i + 1)
      }

      // Vertical bounds based on line box (includes line spacing)
      val lineTop = layout.getLineTop(line).toFloat()
      val lineBottom = layout.getLineBottom(line).toFloat()

      var left = xStart
      var right = xEnd
      var top = lineTop
      var bottom = lineBottom

      // First shrink by background insets (from the line box)
      top += backgroundInsetTop
      bottom -= backgroundInsetBottom
      left += backgroundInsetLeft
      right -= backgroundInsetRight

      // Then expand outward by per-character padding
      left -= charPaddingLeft
      right += charPaddingRight
      top -= charPaddingTop
      bottom += charPaddingBottom

      if (right <= left || bottom <= top) continue

      backgroundRect.set(left, top, right, bottom)
      canvas.drawRoundRect(backgroundRect, radius, radius, backgroundPaint)
    }
  }

  // --- Public API used from the ViewManager ------------------------------------

  fun setCharacterBackgroundColor(color: Int) {
    characterBackgroundColor = color
    invalidate()
  }

  override fun setTextColor(color: Int) {
    super.setTextColor(color)
    textColorValue = color
    invalidate()
  }

  fun setCharPadding(left: Float, top: Float, right: Float, bottom: Float) {
    charPaddingLeft = left
    charPaddingTop = top
    charPaddingRight = right
    charPaddingBottom = bottom
    updateViewPadding()
    applyLineHeightAndSpacing()
    invalidate()
  }

  fun setCharPaddingLeft(padding: Float) {
    charPaddingLeft = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    invalidate()
  }

  fun setCharPaddingRight(padding: Float) {
    charPaddingRight = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    invalidate()
  }

  fun setCharPaddingTop(padding: Float) {
    charPaddingTop = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    invalidate()
  }

  fun setCharPaddingBottom(padding: Float) {
    charPaddingBottom = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    invalidate()
  }

  private fun updateViewPadding() {
    // Keep view padding in sync so backgrounds are not clipped at the edges
    setPadding(
      charPaddingLeft.toInt(),
      charPaddingTop.toInt(),
      charPaddingRight.toInt(),
      charPaddingBottom.toInt()
    )
  }

  fun setCornerRadius(radius: Float) {
    cornerRadius = radius
    invalidate()
  }

  fun setHighlightBorderRadius(radius: Float) {
    highlightBorderRadius = radius
    invalidate()
  }

  fun setFontWeight(weight: String) {
    currentFontWeight = weight
    updateFont()
  }

  fun setFontFamilyProp(family: String?) {
    currentFontFamily = family
    updateFont()
  }

  private fun updateFont() {
    // Parse font weight to integer (100-900)
    val weight = when (currentFontWeight) {
      "100" -> 100
      "200" -> 200
      "300" -> 300
      "400", "normal" -> 400
      "500" -> 500
      "600" -> 600
      "700", "bold" -> 700
      "800" -> 800
      "900" -> 900
      else -> 400
    }

    // Get base typeface
    val baseTypeface = if (currentFontFamily != null) {
      when (currentFontFamily?.lowercase()) {
        "system" -> Typeface.DEFAULT
        "sans-serif" -> Typeface.SANS_SERIF
        "serif" -> Typeface.SERIF
        "monospace" -> Typeface.MONOSPACE
        else -> try {
          Typeface.create(currentFontFamily, Typeface.NORMAL)
        } catch (e: Exception) {
          Typeface.DEFAULT
        }
      }
    } else {
      Typeface.DEFAULT
    }

    // Apply font weight - use API 28+ method for better weight support
    val typeface = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
      Typeface.create(baseTypeface, weight, false)
    } else {
      // Fallback for older Android versions
      val style = if (weight >= 600) Typeface.BOLD else Typeface.NORMAL
      Typeface.create(baseTypeface, style)
    }

    this.typeface = typeface
    applyLineHeightAndSpacing()
    invalidate()
  }

  fun setVerticalAlign(align: String?) {
    currentVerticalAlign = align
    updateVerticalAlignment()
    invalidate()
  }

  private fun updateVerticalAlignment() {
    // Preserve horizontal alignment when updating vertical
    val horizontalGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
    val verticalGravity = when (currentVerticalAlign) {
      "top" -> Gravity.TOP
      "bottom" -> Gravity.BOTTOM
      else -> Gravity.CENTER_VERTICAL
    }

    gravity = horizontalGravity or verticalGravity
  }

  fun setBackgroundInsetTop(inset: Float) {
    backgroundInsetTop = inset
    invalidate()
  }

  fun setBackgroundInsetBottom(inset: Float) {
    backgroundInsetBottom = inset
    invalidate()
  }

  fun setBackgroundInsetLeft(inset: Float) {
    backgroundInsetLeft = inset
    invalidate()
  }

  fun setBackgroundInsetRight(inset: Float) {
    backgroundInsetRight = inset
    invalidate()
  }

  fun setCustomLineHeight(lineHeight: Float) {
    customLineHeight = lineHeight
    applyLineHeightAndSpacing()
    invalidate()
  }

  override fun setTextSize(unit: Int, size: Float) {
    super.setTextSize(unit, size)
    applyLineHeightAndSpacing()
  }

  fun setTextProp(newText: String) {
    if (this.text?.toString() != newText) {
      isUpdatingText = true
      setText(newText)
      // Move cursor to end of text after setting
      post {
        if (hasFocus()) {
          text?.length?.let { setSelection(it) }
        }
      }
      isUpdatingText = false
      invalidate()
    }
  }

  fun setAutoFocus(autoFocus: Boolean) {
    if (autoFocus && isFocusable && isFocusableInTouchMode) {
      postDelayed({
        requestFocus()
        // Move cursor to end of text
        text?.length?.let { setSelection(it) }
        val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        imm?.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_FORCED)
      }, 100)
    }
  }

  // --- Layout helpers ----------------------------------------------------------

  private fun applyLineHeightAndSpacing() {
    if (customLineHeight > 0f) {
      // customLineHeight comes from JS as "points"; convert to px using scaledDensity
      val metrics = resources.displayMetrics
      val desiredLineHeightPx = customLineHeight * metrics.scaledDensity
      val textHeightPx = textSize
      if (textHeightPx > 0f) {
        val multiplier = desiredLineHeightPx / textHeightPx
        setLineSpacing(0f, multiplier)
      }
    } else {
      // Default: add extra spacing equal to vertical padding so backgrounds don't collide
      val extraSpacing = charPaddingTop + charPaddingBottom
      setLineSpacing(extraSpacing, 1.0f)
    }
  }
}
