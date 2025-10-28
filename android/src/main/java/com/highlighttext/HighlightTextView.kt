package com.highlighttext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
  private val backgroundColor: Int,
  private val textColor: Int,
  private val paddingLeft: Float,
  private val paddingRight: Float,
  private val paddingTop: Float,
  private val paddingBottom: Float,
  private val backgroundInsetTop: Float,
  private val backgroundInsetBottom: Float,
  private val backgroundInsetLeft: Float,
  private val backgroundInsetRight: Float,
  private val cornerRadius: Float,
  private val isFirstInGroup: Boolean = false,
  private val isLastInGroup: Boolean = false
) : ReplacementSpan() {

  override fun getSize(
    paint: Paint,
    text: CharSequence?,
    start: Int,
    end: Int,
    fm: Paint.FontMetricsInt?
  ): Int {
    val width = paint.measureText(text, start, end)
    // Only add padding for first and last characters in a group
    val leftPad = if (isFirstInGroup) paddingLeft else 0f
    val rightPad = if (isLastInGroup) paddingRight else 0f
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
    
    // Only add padding for first and last characters in a group
    val leftPad = if (isFirstInGroup) paddingLeft else 0f
    val rightPad = if (isLastInGroup) paddingRight else 0f
    
    // Extend background to overlap and eliminate gaps between characters
    val overlapExtension = 2f
    val leftExtension = if (!isFirstInGroup) overlapExtension else 0f
    val rightExtension = if (!isLastInGroup) overlapExtension else 0f
    
    // Apply background insets first (shrinks from line box)
    val insetTop = textTop + backgroundInsetTop
    val insetHeight = textHeight - (backgroundInsetTop + backgroundInsetBottom)
    
    // Calculate proper bounds with insets then padding
    val rect = RectF(
      x - leftExtension + backgroundInsetLeft,
      insetTop - paddingTop,
      x + width + leftPad + rightPad + rightExtension - backgroundInsetRight,
      insetTop + insetHeight + paddingBottom
    )
    
    // Draw background with selective corner rounding
    when {
      isFirstInGroup && isLastInGroup -> {
        // Single character or isolated group - round all corners
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
      }
      isFirstInGroup -> {
        // First character - round left corners only
        val path = android.graphics.Path()
        path.addRoundRect(
          rect,
          floatArrayOf(
            cornerRadius, cornerRadius, // top-left
            0f, 0f,                      // top-right
            0f, 0f,                      // bottom-right
            cornerRadius, cornerRadius   // bottom-left
          ),
          android.graphics.Path.Direction.CW
        )
        canvas.drawPath(path, bgPaint)
      }
      isLastInGroup -> {
        // Last character - round right corners only
        val path = android.graphics.Path()
        path.addRoundRect(
          rect,
          floatArrayOf(
            0f, 0f,                      // top-left
            cornerRadius, cornerRadius,  // top-right
            cornerRadius, cornerRadius,  // bottom-right
            0f, 0f                       // bottom-left
          ),
          android.graphics.Path.Direction.CW
        )
        canvas.drawPath(path, bgPaint)
      }
      else -> {
        // Middle character - no rounded corners, just rectangle
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
  private var charPaddingLeft: Float = 8f
  private var charPaddingRight: Float = 8f
  private var charPaddingTop: Float = 4f
  private var charPaddingBottom: Float = 4f
  private var backgroundInsetTop: Float = 0f
  private var backgroundInsetBottom: Float = 0f
  private var backgroundInsetLeft: Float = 0f
  private var backgroundInsetRight: Float = 0f
  private var customLineHeight: Float = 0f
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
      isUpdatingText = false
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
          cornerRadius,
          isFirst,
          isLast
        )
        spannable.setSpan(span, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
    
    isUpdatingText = true
    setText(spannable)
    setSelection(text.length) // Keep cursor at end
    isUpdatingText = false
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
