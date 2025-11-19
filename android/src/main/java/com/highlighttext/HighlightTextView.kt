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
  internal val isFirstInGroup: Boolean = false,
  internal val isLastInGroup: Boolean = false,
  private val isStartOfLine: Boolean = false,
  private val isEndOfLine: Boolean = false
) : ReplacementSpan() {

  override fun getSize(
    paint: Paint,
    text: CharSequence?,
    start: Int,
    end: Int,
    fm: Paint.FontMetricsInt?
  ): Int {
    val width = paint.measureText(text, start, end)
    // Add padding for word boundaries AND line boundaries (for consistent alignment)
    val leftPad = if (isFirstInGroup || isStartOfLine) paddingLeft else 0f
    val rightPad = if (isLastInGroup || isEndOfLine) paddingRight else 0f
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
    // Draw background with padding
    val bgPaint = Paint().apply {
      color = backgroundColor
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    
    val width = paint.measureText(text, start, end)
    
    // Use font metrics for consistent height (matches iOS behavior)
    val fontMetrics = paint.fontMetrics
    val textHeight = fontMetrics.descent - fontMetrics.ascent
    val textTop = y + fontMetrics.ascent
    
    // Add padding for word AND line boundaries (consistent alignment)
    val isReallyFirst = isFirstInGroup || isStartOfLine
    val isReallyLast = isLastInGroup || isEndOfLine
    val leftPad = if (isReallyFirst) paddingLeft else 0f
    val rightPad = if (isReallyLast) paddingRight else 0f
    
    // Small overlap to eliminate gaps between characters
    val overlapExtension = 2f
    val leftOverlap = if (!isReallyFirst) overlapExtension else 0f
    val rightOverlap = if (!isReallyLast) overlapExtension else 0f
    
    // Apply background insets first (shrinks from line box)
    val insetTop = textTop + backgroundInsetTop
    val insetHeight = textHeight - (backgroundInsetTop + backgroundInsetBottom)
    
    // Calculate background rect
    val rect = RectF(
      x - leftOverlap + backgroundInsetLeft,
      insetTop - paddingTop,
      x + width + leftPad + rightPad + rightOverlap - backgroundInsetRight,
      insetTop + insetHeight + paddingBottom
    )
    
    // Draw background with selective corner rounding (matches iOS behavior)
    // iOS draws per-character backgrounds with full corner radius, so we do the same
    when {
      isReallyFirst && isReallyLast -> {
        // Single character or isolated group - round all corners (matches iOS)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
      }
      isReallyFirst -> {
        // First character (word start or line start) - round left corners only
        val path = android.graphics.Path()
        path.addRoundRect(
          rect,
          floatArrayOf(
            cornerRadius, cornerRadius, // top-left
            0f, 0f,                      // top-right (flat for connection)
            0f, 0f,                      // bottom-right (flat for connection)
            cornerRadius, cornerRadius   // bottom-left
          ),
          android.graphics.Path.Direction.CW
        )
        canvas.drawPath(path, bgPaint)
      }
      isReallyLast -> {
        // Last character (word end or line end) - round right corners only
        val path = android.graphics.Path()
        path.addRoundRect(
          rect,
          floatArrayOf(
            0f, 0f,                      // top-left (flat for connection)
            cornerRadius, cornerRadius,  // top-right
            cornerRadius, cornerRadius,  // bottom-right
            0f, 0f                       // bottom-left (flat for connection)
          ),
          android.graphics.Path.Direction.CW
        )
        canvas.drawPath(path, bgPaint)
      }
      else -> {
        // Middle character - no rounded corners for seamless connection
        canvas.drawRect(rect, bgPaint)
      }
    }
    
    // Draw text with left padding offset only if first in group
    val textPaint = Paint(paint).apply {
      color = textColor
      isAntiAlias = true
    }
    canvas.drawText(text!!, start, end, x + leftPad, y.toFloat(), textPaint)
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
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable?) {
        if (!isUpdatingText) {
          onTextChangeListener?.invoke(s?.toString() ?: "")
          applyCharacterBackgrounds()
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
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingLeft(padding: Float) {
    charPaddingLeft = padding
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingRight(padding: Float) {
    charPaddingRight = padding
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingTop(padding: Float) {
    charPaddingTop = padding
    applyCharacterBackgrounds()
  }
  
  fun setCharPaddingBottom(padding: Float) {
    charPaddingBottom = padding
    applyCharacterBackgrounds()
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
  
  private fun applyCharacterBackgrounds() {
    val text = text?.toString() ?: return
    if (text.isEmpty()) return
    
    val spannable = SpannableString(text)
    
    // Apply line height if specified
    if (customLineHeight > 0) {
      val lineSpacingMultiplier = customLineHeight / textSize
      setLineSpacing(0f, lineSpacingMultiplier)
    }
    
    // Apply character-by-character for proper line wrapping
    for (i in text.indices) {
      val char = text[i]
      
      // Check if this is a space that should be highlighted
      val shouldHighlight = when {
        char == '\n' || char == '\t' -> false // Never highlight newlines or tabs
        char == ' ' -> {
          // Highlight space only if it's a single space (not multiple consecutive)
          val hasSpaceBefore = i > 0 && text[i - 1] == ' '
          val hasSpaceAfter = i < text.length - 1 && text[i + 1] == ' '
          !hasSpaceBefore && !hasSpaceAfter
        }
        else -> true // Highlight all other characters
      }
      
      if (shouldHighlight) {
        // Determine if this is the first or last character in a word group
        val isFirst = i == 0 || !shouldHighlightChar(text, i - 1)
        val isLast = i == text.length - 1 || !shouldHighlightChar(text, i + 1)
        
        // Use highlightBorderRadius if specified, otherwise use cornerRadius (matches iOS)
        val radius = if (highlightBorderRadius > 0) highlightBorderRadius else cornerRadius
        
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
          isFirst,
          isLast
        )
        spannable.setSpan(span, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
    
    // Save current selection to prevent cursor jumping (smooth editing)
    val currentSelection = selectionStart
    
    isUpdatingText = true
    setText(spannable)
    
    // Restore cursor position if valid (prevents jerking during editing)
    if (currentSelection >= 0 && currentSelection <= text.length) {
      setSelection(currentSelection)
    }
    isUpdatingText = false
    
    // Detect line wraps after layout is ready
    post { detectLineWraps() }
  }
  
  private fun detectLineWraps() {
    val layout = layout ?: return
    val text = text as? Spannable ?: return
    val textStr = text.toString()
    val spans = text.getSpans(0, text.length, RoundedBackgroundSpan::class.java)
    
    for (span in spans) {
      val spanStart = text.getSpanStart(span)
      val spanEnd = text.getSpanEnd(span)
      
      if (spanStart >= 0 && spanStart < text.length) {
        val line = layout.getLineForOffset(spanStart)
        val lineStart = layout.getLineStart(line)
        val lineEnd = layout.getLineEnd(line)
        
        // Check for manual line break (\n) before this character
        val hasNewlineBefore = spanStart > 0 && textStr[spanStart - 1] == '\n'
        // Check for manual line break (\n) after this character  
        val hasNewlineAfter = spanEnd < textStr.length && textStr[spanEnd] == '\n'
        
        // Check if this is the last line of text
        val isLastLine = line == layout.lineCount - 1
        
        // Check if this char is at start of visual line (wrapped OR after \n)
        val isAtLineStart = (spanStart == lineStart && !span.isFirstInGroup) || hasNewlineBefore
        
        // Check if this char is at end of visual line (wrapped OR before \n OR end of last line)
        // CRITICAL: Ensure last character of entire text gets rounded corners
        val isAtLineEnd = (spanEnd == lineEnd && !span.isLastInGroup) || hasNewlineAfter || 
                          (isLastLine && spanEnd == lineEnd)
        
        if (isAtLineStart || isAtLineEnd) {
          // Create new span with line boundary flags
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
            span.isFirstInGroup,
            span.isLastInGroup,
            isAtLineStart,
            isAtLineEnd
          )
          text.removeSpan(span)
          text.setSpan(newSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      }
    }
    invalidate()
  }
  
  private fun shouldHighlightChar(text: String, index: Int): Boolean {
    if (index < 0 || index >= text.length) return false
    val char = text[index]
    
    return when {
      char == '\n' || char == '\t' -> false
      char == ' ' -> {
        val hasSpaceBefore = index > 0 && text[index - 1] == ' '
        val hasSpaceAfter = index < text.length - 1 && text[index + 1] == ' '
        !hasSpaceBefore && !hasSpaceAfter
      }
      else -> true
    }
  }
}
