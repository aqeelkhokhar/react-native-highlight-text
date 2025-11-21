package com.highlighttext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ReplacementSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText

/**
 * iOS-style span: Draws rounded background for each character.
 * Padding is only applied at line boundaries (first/last character of each line).
 */
class RoundedBackgroundSpan(
  internal val backgroundColor: Int,
  internal val textColor: Int,
  internal val paddingLeft: Float,
  internal val paddingRight: Float,
  internal val paddingTop: Float,
  internal val paddingBottom: Float,
  internal val backgroundInsetTop: Float,
  internal val backgroundInsetBottom: Float,
  internal val backgroundInsetLeft: Float,
  internal val backgroundInsetRight: Float,
  internal val cornerRadius: Float,
  internal val isLineStart: Boolean = false,
  internal val isLineEnd: Boolean = false,
  internal val isFirstLine: Boolean = false,
  internal val isLastLine: Boolean = false
) : ReplacementSpan() {

  override fun getSize(
    paint: Paint,
    text: CharSequence?,
    start: Int,
    end: Int,
    fm: Paint.FontMetricsInt?
  ): Int {
    // Only add padding at line boundaries (matches iOS behavior)
    val width = paint.measureText(text, start, end)
    val leftPad = if (isLineStart) paddingLeft else 0f
    val rightPad = if (isLineEnd) paddingRight else 0f
    return (width + leftPad + rightPad).toInt()
  }

  override fun draw(
    canvas: Canvas,
    text: CharSequence?,
    start: Int,
    end: Int,
    x: Float,
    top: Int,
    y: Int,
    bottom: Int,
    paint: Paint
  ) {
    if (text == null) return
    
    val bgPaint = Paint().apply {
      color = backgroundColor
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    
    val width = paint.measureText(text, start, end)
    
    // Use font metrics for consistent height (matches iOS)
    val fontMetrics = paint.fontMetrics
    val textHeight = fontMetrics.descent - fontMetrics.ascent
    val textTop = y + fontMetrics.ascent
    
    // Apply background insets first (shrinks from line box - EXACTLY like iOS line 45-48)
    val insetTop = textTop + backgroundInsetTop
    val insetHeight = textHeight - (backgroundInsetTop + backgroundInsetBottom)
    
    // Only apply padding at line boundaries (matches iOS behavior)
    val leftPad = if (isLineStart) paddingLeft else 0f
    val rightPad = if (isLineEnd) paddingRight else 0f
    
    // SELECTIVE ROUNDING STRATEGY:
    // 1. Line Start: Round Left corners.
    // 2. Line End: Round Right corners.
    // 3. Middle: Square (no rounding).
    // 4. Overlap: Minimal (1px) to seal seams.
    
    val overlapExtension = 1f
    
    // No extension needed for start/end boundaries
    val leftExtend = 0f
    
    // Extend right slightly for middle characters to seal the gap
    val rightExtend = if (!isLineEnd) {
      if (isLineStart) leftPad + overlapExtension else overlapExtension
    } else {
      0f
    }
    
    // Vertical overlap to eliminate gaps (reduced to prevent descender clipping)
    val topExtend = 0f
    val bottomExtend = 0f
    
    // Calculate background rect
    // NOTE: Since this is a ReplacementSpan, 'x' is the start of the span (including padding).
    // So we draw from 'x', not 'x - leftPad'.
    val rect = RectF(
      x + backgroundInsetLeft - leftExtend,
      insetTop - paddingTop - topExtend,
      x + leftPad + width + rightPad - backgroundInsetRight + rightExtend,
      insetTop + insetHeight + paddingBottom + bottomExtend
    )
    
    // Draw based on position
    when {
      isLineStart && isLineEnd -> {
        // Single character - round all corners
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
      }
      isLineStart -> {
        // Line START - round LEFT corners (top and bottom)
        val path = android.graphics.Path()
        path.addRoundRect(
          rect,
          floatArrayOf(
            cornerRadius, cornerRadius,  // top-left
            0f, 0f,                       // top-right
            0f, 0f,                       // bottom-right
            cornerRadius, cornerRadius    // bottom-left
          ),
          android.graphics.Path.Direction.CW
        )
        canvas.drawPath(path, bgPaint)
      }
      isLineEnd -> {
        // Line END - round RIGHT corners (top and bottom)
        val path = android.graphics.Path()
        path.addRoundRect(
          rect,
          floatArrayOf(
            0f, 0f,                       // top-left
            cornerRadius, cornerRadius,   // top-right
            cornerRadius, cornerRadius,   // bottom-right
            0f, 0f                        // bottom-left
          ),
          android.graphics.Path.Direction.CW
        )
        canvas.drawPath(path, bgPaint)
      }
      else -> {
        // Middle characters - NO rounded corners (square) for smooth edges
        canvas.drawRect(rect, bgPaint)
      }
    }
    
    // Draw text offset by left padding only if at line start
    val textPaint = Paint(paint).apply {
      color = textColor
      isAntiAlias = true
    }
    canvas.drawText(text, start, end, x + leftPad, y.toFloat(), textPaint)
  }
}

class HighlightTextView : AppCompatEditText {
  private var characterBackgroundColor: Int = Color.parseColor("#FFFF00")
  private var textColorValue: Int = Color.BLACK
  private var cornerRadius: Float = 4f
  private var highlightBorderRadius: Float = 0f
  private var charPaddingLeft: Float = 4f
  private var charPaddingRight: Float = 4f
  private var charPaddingTop: Float = 4f
  private var charPaddingBottom: Float = 4f
  private var backgroundInsetTop: Float = 0f
  private var backgroundInsetBottom: Float = 0f
  private var backgroundInsetLeft: Float = 0f
  private var backgroundInsetRight: Float = 0f
  private var customLineHeight: Float = 0f
  private var currentFontFamily: String? = null
  private var currentFontWeight: String = "normal"
  private var currentVerticalAlign: String? = null
  private var isUpdatingText: Boolean = false
  
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
    
    addTextChangedListener(object : TextWatcher {
      private var changeStart = 0
      private var changeEnd = 0
      
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        changeStart = start
        changeEnd = start + after
      }
      
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      
      override fun afterTextChanged(s: Editable?) {
        if (!isUpdatingText) {
          onTextChangeListener?.invoke(s?.toString() ?: "")
          
          applyCharacterBackgroundsIncremental(s, changeStart, changeEnd)
        }
      }
    })
  }

  fun setCharacterBackgroundColor(color: Int) {
    characterBackgroundColor = color
    applyCharacterBackgrounds()
  }
  
  override fun setTextColor(color: Int) {
    super.setTextColor(color)
    textColorValue = color
    applyCharacterBackgrounds()
  }

  fun setCharPadding(left: Float, top: Float, right: Float, bottom: Float) {
    charPaddingLeft = left
    charPaddingTop = top
    charPaddingRight = right
    charPaddingBottom = bottom
    updateViewPadding()
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingLeft(padding: Float) {
    charPaddingLeft = padding
    updateViewPadding()
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingRight(padding: Float) {
    charPaddingRight = padding
    updateViewPadding()
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingTop(padding: Float) {
    charPaddingTop = padding
    updateViewPadding()
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingBottom(padding: Float) {
    charPaddingBottom = padding
    updateViewPadding()
    applyCharacterBackgrounds()
  }
  
  private fun updateViewPadding() {
    // Sync View padding with char padding to prevent clipping of background
    setPadding(
      charPaddingLeft.toInt(),
      charPaddingTop.toInt(),
      charPaddingRight.toInt(),
      charPaddingBottom.toInt()
    )
  }
  
  fun setCornerRadius(radius: Float) {
    cornerRadius = radius
    applyCharacterBackgrounds()
  }
  
  fun setHighlightBorderRadius(radius: Float) {
    highlightBorderRadius = radius
    applyCharacterBackgrounds()
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
    applyCharacterBackgrounds()
  }
  
  fun setVerticalAlign(align: String?) {
    currentVerticalAlign = align
    updateVerticalAlignment()
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
    applyCharacterBackgrounds()
  }
  
  fun setBackgroundInsetBottom(inset: Float) {
    backgroundInsetBottom = inset
    applyCharacterBackgrounds()
  }
  
  fun setBackgroundInsetLeft(inset: Float) {
    backgroundInsetLeft = inset
    applyCharacterBackgrounds()
  }
  
  fun setBackgroundInsetRight(inset: Float) {
    backgroundInsetRight = inset
    applyCharacterBackgrounds()
  }
  
  fun setCustomLineHeight(lineHeight: Float) {
    customLineHeight = lineHeight
    applyCharacterBackgrounds()
  }

  fun setTextProp(text: String) {
    if (this.text?.toString() != text) {
      isUpdatingText = true
      setText(text)
      applyCharacterBackgrounds()
      // Move cursor to end of text after setting
      post {
        if (hasFocus()) {
          text.length.let { setSelection(it) }
        }
      }
      isUpdatingText = false
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
  
  /**
   * iOS-style incremental update: Only update spans for changed region.
   * This is called during typing and only touches the modified characters.
   */
  private fun applyCharacterBackgroundsIncremental(editable: Editable?, start: Int, end: Int) {
    if (editable == null) return
    val textStr = editable.toString()
    if (textStr.isEmpty()) return
    
    isUpdatingText = true
    
    // Check if a newline was inserted - if so, expand region to include char before it
    val hasNewline = textStr.substring(start, minOf(end, textStr.length)).contains('\n')
    
    // Expand the region to include entire lines that were affected
    val layout = layout
    val expandedStart: Int
    val expandedEnd: Int
    
    if (layout != null && textStr.isNotEmpty()) {
      val startLine = layout.getLineForOffset(minOf(start, textStr.length - 1))
      val endLine = layout.getLineForOffset(minOf(end, textStr.length - 1))
      expandedStart = layout.getLineStart(startLine)
      expandedEnd = layout.getLineEnd(endLine)
    } else {
      // If newline inserted, include character before it
      expandedStart = if (hasNewline) maxOf(0, start - 2) else maxOf(0, start - 1)
      expandedEnd = minOf(textStr.length, end + 1)
    }
    
    // Remove existing spans in the affected lines
    val existingSpans = editable.getSpans(expandedStart, expandedEnd, RoundedBackgroundSpan::class.java)
    for (span in existingSpans) {
      editable.removeSpan(span)
    }
    
    // Apply spans with correct line boundary flags immediately
    val radius = if (highlightBorderRadius > 0) highlightBorderRadius else cornerRadius
    
    for (i in expandedStart until expandedEnd) {
      if (i >= textStr.length) break
      
      val char = textStr[i]
      val shouldHighlight = when {
        char == '\n' || char == '\t' -> false
        char == ' ' -> {
          val hasSpaceBefore = i > 0 && textStr[i - 1] == ' '
          val hasSpaceAfter = i < textStr.length - 1 && textStr[i + 1] == ' '
          !hasSpaceBefore && !hasSpaceAfter
        }
        else -> true
      }
      
      if (shouldHighlight) {
        // ALWAYS check newlines first (for manual line breaks)
        val hasNewlineBefore = i > 0 && textStr[i - 1] == '\n'
        val hasNewlineAfter = i + 1 < textStr.length && textStr[i + 1] == '\n'
        
        var isAtLineStart = i == 0 || hasNewlineBefore
        var isAtLineEnd = i == textStr.length - 1 || hasNewlineAfter
        
        // Determine if this is the first or last line
        var isOnFirstLine = i == 0 || hasNewlineBefore
        var isOnLastLine = i == textStr.length - 1 || hasNewlineAfter
        
        // Only use layout for auto-wrapped lines (not manual newlines)
        if (!hasNewlineBefore && !hasNewlineAfter && layout != null && i < textStr.length) {
          try {
            val line = layout.getLineForOffset(i)
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)
            // Check if this is the first or last line
            isOnFirstLine = line == 0
            isOnLastLine = line == layout.lineCount - 1
            // Only override if this is an auto-wrapped boundary
            if (i == lineStart && textStr.getOrNull(i - 1) != '\n') {
              isAtLineStart = true
            }
            if ((i + 1) == lineEnd && textStr.getOrNull(i + 1) != '\n') {
              isAtLineEnd = true
            }
          } catch (e: Exception) {
            // Layout might not be ready, keep newline-based detection
          }
        }
        
        val span = RoundedBackgroundSpan(
          characterBackgroundColor,
          textColorValue,
          charPaddingLeft,
          charPaddingRight,
          charPaddingTop,
          charPaddingBottom,
          backgroundInsetTop,
          backgroundInsetBottom,
          backgroundInsetLeft,
          backgroundInsetRight,
          radius,
          isAtLineStart,
          isAtLineEnd,
          isOnFirstLine,
          isOnLastLine
        )
        editable.setSpan(span, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
    
    isUpdatingText = false
    
    // JERK FIX: Skip the post-update during fast typing to prevent layout thrashing
    // Only update boundaries when user stops typing (reduces update frequency)
    removeCallbacks(boundaryUpdateCheck)
    postDelayed(boundaryUpdateCheck, 200)
  }
  
  // Runnable for delayed boundary check
  private val boundaryUpdateCheck = Runnable {
    if (!isUpdatingText) {
      updateAutoWrappedLineBoundaries()
    }
  }
  
  /**
   * Full re-application of spans (used when props change, not during typing).
   * iOS-style: Work directly with editable, no setText() call.
   */
  private fun applyCharacterBackgrounds() {
    val editable = editableText ?: return
    val textStr = editable.toString()
    if (textStr.isEmpty()) return
    
    // Apply line height if specified, or add spacing for padding
    if (customLineHeight > 0) {
      val lineSpacingMultiplier = customLineHeight / textSize
      setLineSpacing(0f, lineSpacingMultiplier)
    } else {
      // Add line spacing to accommodate vertical padding and prevent overlap
      val extraSpacing = charPaddingTop + charPaddingBottom
      setLineSpacing(extraSpacing, 1.0f)
    }
    
    isUpdatingText = true
    
    // Remove all existing spans
    val existingSpans = editable.getSpans(0, editable.length, RoundedBackgroundSpan::class.java)
    for (span in existingSpans) {
      editable.removeSpan(span)
    }
    
    // Apply spans to all characters with correct line boundary flags
    val radius = if (highlightBorderRadius > 0) highlightBorderRadius else cornerRadius
    val layoutObj = layout
    
    for (i in textStr.indices) {
      val char = textStr[i]
      
      val shouldHighlight = when {
        char == '\n' || char == '\t' -> false
        char == ' ' -> {
          val hasSpaceBefore = i > 0 && textStr[i - 1] == ' '
          val hasSpaceAfter = i < textStr.length - 1 && textStr[i + 1] == ' '
          !hasSpaceBefore && !hasSpaceAfter
        }
        else -> true
      }
      
      if (shouldHighlight) {
        // ALWAYS check newlines first (for manual line breaks)
        val hasNewlineBefore = i > 0 && textStr[i - 1] == '\n'
        val hasNewlineAfter = i + 1 < textStr.length && textStr[i + 1] == '\n'
        
        var isAtLineStart = i == 0 || hasNewlineBefore
        var isAtLineEnd = i == textStr.length - 1 || hasNewlineAfter
        
        // Determine if this is the first or last line
        var isOnFirstLine = i == 0 || hasNewlineBefore
        var isOnLastLine = i == textStr.length - 1 || hasNewlineAfter
        
        // Only use layout for auto-wrapped lines (not manual newlines)
        if (!hasNewlineBefore && !hasNewlineAfter && layoutObj != null && i < textStr.length) {
          try {
            val line = layoutObj.getLineForOffset(i)
            val lineStart = layoutObj.getLineStart(line)
            val lineEnd = layoutObj.getLineEnd(line)
            // Check if this is the first or last line
            isOnFirstLine = line == 0
            isOnLastLine = line == layoutObj.lineCount - 1
            // Only override if this is an auto-wrapped boundary
            if (i == lineStart && textStr.getOrNull(i - 1) != '\n') {
              isAtLineStart = true
            }
            if ((i + 1) == lineEnd && textStr.getOrNull(i + 1) != '\n') {
              isAtLineEnd = true
            }
          } catch (e: Exception) {
            // Layout might not be ready, keep newline-based detection
          }
        }
        
        val span = RoundedBackgroundSpan(
          characterBackgroundColor,
          textColorValue,
          charPaddingLeft,
          charPaddingRight,
          charPaddingTop,
          charPaddingBottom,
          backgroundInsetTop,
          backgroundInsetBottom,
          backgroundInsetLeft,
          backgroundInsetRight,
          radius,
          isAtLineStart,
          isAtLineEnd,
          isOnFirstLine,
          isOnLastLine
        )
        editable.setSpan(span, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
    
    isUpdatingText = false
    
    // Schedule a post-layout update to ensure corner rounding is correct
    // This is needed because layout might not be ready when this method is called
    post {
      if (!isUpdatingText) {
        updateAutoWrappedLineBoundaries()
      }
    }
  }
  
  /**
   * Update line boundary flags only for auto-wrapped lines.
   * This is called after layout completes to handle text wrapping.
   * Only updates spans that are at auto-wrapped line boundaries.
   * Optimized to skip updates when layout hasn't changed.
   */
  private fun updateAutoWrappedLineBoundaries() {
    if (isUpdatingText) return
    
    val layout = layout ?: return
    val editable = editableText ?: return
    val textStr = editable.toString()
    if (textStr.isEmpty()) return
    
    // Validate that layout is ready and has valid dimensions
    if (width <= 0 || layout.lineCount == 0) return
    
    val spans = editable.getSpans(0, editable.length, RoundedBackgroundSpan::class.java)
    if (spans.isEmpty()) return
    
    isUpdatingText = true
    var hasChanges = false
    
    for (span in spans) {
      val spanStart = editable.getSpanStart(span)
      val spanEnd = editable.getSpanEnd(span)
      
      if (spanStart < 0 || spanStart >= textStr.length) continue
      
      try {
        val line = layout.getLineForOffset(spanStart)
        val lineStart = layout.getLineStart(line)
        val lineEnd = layout.getLineEnd(line)
        
        // Determine actual line boundaries (includes auto-wrap)
        val isAtLineStart = spanStart == lineStart
        val isAtLineEnd = spanEnd == lineEnd
        
        // Check if this is the first or last line
        val isOnFirstLine = line == 0
        val isOnLastLine = line == layout.lineCount - 1
        
        // Only update if this is an auto-wrapped line boundary (not a newline boundary)
        val isNewlineBoundary = (spanStart > 0 && textStr[spanStart - 1] == '\n') || 
                                (spanEnd < textStr.length && textStr[spanEnd] == '\n')
        
        // Only recreate span if it's at an auto-wrapped boundary and flags are wrong
        if (!isNewlineBoundary && (isAtLineStart != span.isLineStart || isAtLineEnd != span.isLineEnd || 
            isOnFirstLine != span.isFirstLine || isOnLastLine != span.isLastLine)) {
          val newSpan = RoundedBackgroundSpan(
            span.backgroundColor,
            span.textColor,
            span.paddingLeft,
            span.paddingRight,
            span.paddingTop,
            span.paddingBottom,
            span.backgroundInsetTop,
            span.backgroundInsetBottom,
            span.backgroundInsetLeft,
            span.backgroundInsetRight,
            span.cornerRadius,
            isAtLineStart,
            isAtLineEnd,
            isOnFirstLine,
            isOnLastLine
          )
          editable.removeSpan(span)
          editable.setSpan(newSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          hasChanges = true
        }
      } catch (e: Exception) {
        // Layout state is invalid, skip this update
        continue
      }
    }
    
    isUpdatingText = false
    
    // Only invalidate if we actually made changes
    if (hasChanges) {
      invalidate()
    }
  }
}
