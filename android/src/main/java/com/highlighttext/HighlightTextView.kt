package com.highlighttext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import com.facebook.react.common.assets.ReactFontManager
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Custom EditText that mimics the iOS implementation by drawing per-character
 * rounded highlights directly in onDraw(), instead of using spans.
 *
 * This avoids layout thrashing/flicker when lines auto-wrap and keeps padding
 * logic independent from Android's line breaking.
 */
class HighlightTextView : AppCompatEditText {
  // Visual props. Lengths (radius, padding, insets) are stored in layout points
  // (dp), the same units iOS uses, and converted to pixels where they are used.
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
  private var customLineSpacing: Float = 0f

  // Font + alignment state
  private var currentFontFamily: String? = null
  private var currentFontWeight: String = "normal"
  private var currentVerticalAlign: String? = null
  // Letter spacing in layout points (same semantics as React Native's letterSpacing prop)
  private var letterSpacingPoints: Float = 0f

  // Internal flags
  private var isUpdatingText: Boolean = false

  // Drawing helpers
  private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }
  private val backgroundRect = RectF()
  private val backgroundPath = Path()
  private val radii = FloatArray(8)

  private companion object {
    // Padded character rects closer than this (px) join into one run, so
    // sub-pixel gaps never show up as seams.
    const val RUN_JOIN_TOLERANCE_PX = 0.5f
  }

  var onTextChangeListener: ((String) -> Unit)? = null
  var focusEventListener: ((Boolean) -> Unit)? = null
  var onSubmitEditingListener: ((String) -> Unit)? = null
  var onSelectionChangeListener: ((Int, Int) -> Unit)? = null

  // Input configuration (combined into inputType by updateInputType)
  private var editableState: Boolean = true
  private var autoCapitalizeValue: String = ""
  private var keyboardTypeValue: String = ""
  // returnKeyType set (and not "default"): Return fires onSubmitEditing instead of a new line
  private var submitOnReturn: Boolean = false
  // -1 = no limit; only applies to typing, not to programmatic text
  private var maxLengthValue: Int = -1

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
    gravity = Gravity.START or Gravity.CENTER_VERTICAL
    setPadding(20, 20, 20, 20)
    textColorValue = currentTextColor

    // Enable text wrapping
    maxLines = Int.MAX_VALUE
    isSingleLine = false
    setHorizontallyScrolling(false)
    includeFontPadding = false

    applyLineHeightAndSpacing()

    filters = arrayOf(maxLengthFilter)

    setOnEditorActionListener { _, actionId, event ->
      val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER
      if (submitOnReturn && (actionId != EditorInfo.IME_ACTION_UNSPECIFIED || isEnterKey)) {
        onSubmitEditingListener?.invoke(text?.toString() ?: "")
        true
      } else {
        false
      }
    }

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
    if (text.isEmpty()) return

    backgroundPaint.color = characterBackgroundColor
    val paint = paint
    val density = resources.displayMetrics.density
    val radius = (if (highlightBorderRadius > 0f) highlightBorderRadius else cornerRadius) * density
    val padLeft = charPaddingLeft * density
    val padRight = charPaddingRight * density
    val padTop = charPaddingTop * density
    val padBottom = charPaddingBottom * density
    val insetTop = backgroundInsetTop * density
    val insetBottom = backgroundInsetBottom * density
    val insetLeft = backgroundInsetLeft * density
    val insetRight = backgroundInsetRight * density
    // Like iOS, the background starts from the line box: the custom line height
    // when one is set, otherwise the font's own line height. Its bottom sits on
    // the font's descent, so extra line height goes above the text as on iOS.
    val fm = paint.fontMetrics
    val lineBoxHeight = if (customLineHeight > 0f) {
      spToPx(customLineHeight, resources.displayMetrics)
    } else {
      fm.descent - fm.ascent
    }

    val horizontalGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
    val isRightAligned = horizontalGravity == Gravity.END || horizontalGravity == Gravity.RIGHT

    // All highlight shapes go into ONE path that is filled once. Filling each
    // shape separately anti-aliases every edge on its own, which leaves faint
    // seams where neighbouring shapes meet or overlap.
    backgroundPath.reset()

    for (line in 0 until layout.lineCount) {
      val lineStart = layout.getLineStart(line)
      val lineEnd = layout.getLineEnd(line)

      // Same geometry as iOS for every character: the line box (vertical) and
      // the glyph advance (horizontal) shrink by the insets, then expand
      // outward by the padding. Spaces, tabs and newlines get no background.
      val baseline = layout.getLineBaseline(line).toFloat()
      val bottom = baseline + fm.descent - insetBottom + padBottom
      val top = baseline + fm.descent - lineBoxHeight + insetTop - padTop
      if (bottom <= top) continue

      // Characters whose padded rects touch or overlap (also across a space,
      // when the padding covers it) form one run, drawn as a single rounded
      // shape so a line reads as one continuous highlight like on iOS.
      var runLeft = 0f
      var runRight = 0f
      var hasRun = false
      var isFirstRun = true
      var tabSinceRun = false

      for (i in lineStart until lineEnd) {
        val ch = text[i]
        if (ch == '\n' || ch == ' ') continue
        if (ch == '\t') {
          tabSinceRun = true
          continue
        }

        val xStart = layout.getPrimaryHorizontal(i)
        val xEnd = if (i + 1 < lineEnd) {
          layout.getPrimaryHorizontal(i + 1)
        } else {
          xStart + paint.measureText(text, i, i + 1)
        }
        val left = minOf(xStart, xEnd) + insetLeft - padLeft
        val right = maxOf(xStart, xEnd) - insetRight + padRight
        if (right <= left) continue

        if (hasRun && !tabSinceRun && left <= runRight + RUN_JOIN_TOLERANCE_PX) {
          runLeft = minOf(runLeft, left)
          runRight = maxOf(runRight, right)
        } else {
          if (hasRun) {
            addRun(layout, text, line, runLeft, runRight, top, bottom, radius, isFirstRun, false)
            isFirstRun = false
          }
          runLeft = left
          runRight = right
          hasRun = true
        }
        tabSinceRun = false
      }

      if (hasRun) {
        // For right-aligned text, snap the line's outermost highlight to the
        // line's visual right edge so wrapped lines form a clean column.
        if (isRightAligned) {
          runRight = layout.getLineRight(line) - insetRight + padRight
        }
        addRun(layout, text, line, runLeft, runRight, top, bottom, radius, isFirstRun, true)
      }
    }

    canvas.drawPath(backgroundPath, backgroundPaint)
  }

  /**
   * Adds one highlight run to the path. Sides in the middle of a line are fully
   * rounded. The line's outer sides are rounded where the shape has an outside
   * corner: on the first/last line of a paragraph, or where this line extends
   * further than the line above/below (the side text is anchored to only rounds
   * at paragraph boundaries).
   */
  private fun addRun(
    layout: android.text.Layout,
    text: CharSequence,
    line: Int,
    left: Float,
    right: Float,
    top: Float,
    bottom: Float,
    radius: Float,
    isLineStart: Boolean,
    isLineEnd: Boolean
  ) {
    if (right <= left) return
    backgroundRect.set(left, top, right, bottom)

    val horizontalGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
    val isLeftAligned = horizontalGravity == Gravity.START || horizontalGravity == Gravity.LEFT
    val isRightAligned = horizontalGravity == Gravity.END || horizontalGravity == Gravity.RIGHT
    val isCenterAligned = horizontalGravity == Gravity.CENTER_HORIZONTAL

    // Detect paragraph boundaries (empty lines above/below)
    val isFirstLineOfParagraph = line == 0 || isLineEmpty(text, layout, line - 1)
    val isLastLineOfParagraph = line == layout.lineCount - 1 || isLineEmpty(text, layout, line + 1)
    val currentLineWidth = layout.getLineMax(line)

    // Side the text is anchored to: rounded only at paragraph boundaries
    val anchoredTop = if (isFirstLineOfParagraph) radius else 0f
    val anchoredBottom = if (isLastLineOfParagraph) radius else 0f
    // Ragged side: also rounded where this line extends past the line above/below
    val raggedTop = when {
      isFirstLineOfParagraph -> radius
      extendsBeyond(currentLineWidth, layout.getLineMax(line - 1)) -> radius
      else -> 0f
    }
    val raggedBottom = when {
      isLastLineOfParagraph -> radius
      extendsBeyond(currentLineWidth, layout.getLineMax(line + 1)) -> radius
      else -> 0f
    }

    var tl = radius
    var bl = radius
    var tr = radius
    var br = radius

    if (isLineStart) {
      when {
        isLeftAligned -> { tl = anchoredTop; bl = anchoredBottom }
        isRightAligned || isCenterAligned -> { tl = raggedTop; bl = raggedBottom }
        else -> { tl = 0f; bl = 0f }
      }
    }
    if (isLineEnd) {
      when {
        isRightAligned -> { tr = anchoredTop; br = anchoredBottom }
        isLeftAligned || isCenterAligned -> { tr = raggedTop; br = raggedBottom }
        else -> { tr = 0f; br = 0f }
      }
    }

    // Arrays: Top-Left x,y; Top-Right x,y; Bottom-Right x,y; Bottom-Left x,y
    radii[0] = tl; radii[1] = tl
    radii[2] = tr; radii[3] = tr
    radii[4] = br; radii[5] = br
    radii[6] = bl; radii[7] = bl

    backgroundPath.addRoundRect(backgroundRect, radii, Path.Direction.CW)
  }

  private fun extendsBeyond(currentLineWidth: Float, otherLineWidth: Float): Boolean =
    !lineWidthsEqual(currentLineWidth, otherLineWidth) && currentLineWidth > otherLineWidth

  private fun lineWidthsEqual(w1: Float, w2: Float): Boolean {
    // Small tolerance so lines that should visually match are treated as equal
    return abs(w1 - w2) < 0.5f
  }

  private fun isLineEmpty(text: CharSequence, layout: android.text.Layout, line: Int): Boolean {
    if (line < 0 || line >= layout.lineCount) return false
    
    val lineStart = layout.getLineStart(line)
    val lineEnd = layout.getLineEnd(line)
    
    // Check if line contains only whitespace/newlines
    for (i in lineStart until lineEnd) {
      val ch = text[i]
      if (ch != '\n' && ch != '\t' && ch != ' ') {
        return false
      }
    }
    return true
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
    requestLayout()
    post { invalidate() }
  }

  fun setCharPaddingLeft(padding: Float) {
    charPaddingLeft = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    requestLayout()
    post { invalidate() }
  }

  fun setCharPaddingRight(padding: Float) {
    charPaddingRight = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    requestLayout()
    post { invalidate() }
  }

  fun setCharPaddingTop(padding: Float) {
    charPaddingTop = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    requestLayout()
    post { invalidate() }
  }

  fun setCharPaddingBottom(padding: Float) {
    charPaddingBottom = padding
    updateViewPadding()
    applyLineHeightAndSpacing()
    requestLayout()
    post { invalidate() }
  }

  private fun updateViewPadding() {
    // Keep view padding in sync so backgrounds are not clipped at the edges
    val density = resources.displayMetrics.density
    setPadding(
      (charPaddingLeft * density).roundToInt(),
      (charPaddingTop * density).roundToInt(),
      (charPaddingRight * density).roundToInt(),
      (charPaddingBottom * density).roundToInt()
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

    // Capture currentFontFamily as local variable to avoid smart cast issues
    val fontFamily = currentFontFamily

    // Get base typeface using ReactFontManager for custom fonts
    val baseTypeface = if (fontFamily != null) {
      when (fontFamily.lowercase()) {
        "system" -> Typeface.DEFAULT
        "sans-serif" -> Typeface.SANS_SERIF
        "serif" -> Typeface.SERIF
        "monospace" -> Typeface.MONOSPACE
        else -> try {
          // Use ReactFontManager to load custom fonts from assets
          val style = if (weight >= 600) Typeface.BOLD else Typeface.NORMAL
          ReactFontManager.getInstance().getTypeface(fontFamily, style, context.assets)
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
    // Request layout to ensure proper measurement with new font before redrawing
    requestLayout()
    // Post invalidate to ensure layout is complete before drawing
    post { invalidate() }
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
    requestLayout()
    post { invalidate() }
  }

  fun setCustomLineSpacing(spacing: Float) {
    customLineSpacing = spacing
    applyLineHeightAndSpacing()
    requestLayout()
    post { invalidate() }
  }

  fun setLetterSpacingProp(points: Float) {
    letterSpacingPoints = points
    applyLetterSpacing()
    invalidate()
  }

  override fun setTextSize(unit: Int, size: Float) {
    super.setTextSize(unit, size)
    applyLineHeightAndSpacing()
    applyLetterSpacing()
    requestLayout()
    post { invalidate() }
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
        imm?.showSoftInput(this, 0)
      }, 100)
    }
  }

  // --- Input configuration -----------------------------------------------------

  private val maxLengthFilter = InputFilter { source, start, end, dest, dstart, dend ->
    val max = maxLengthValue
    if (max < 0 || isUpdatingText) {
      null // no limit, or programmatic text (text prop / setText command)
    } else {
      val keep = max - (dest.length - (dend - dstart))
      when {
        keep <= 0 -> ""
        keep >= end - start -> null
        else -> {
          var cut = start + keep
          if (Character.isHighSurrogate(source[cut - 1])) {
            cut--
            if (cut == start) return@InputFilter ""
          }
          source.subSequence(start, cut)
        }
      }
    }
  }

  fun setEditableProp(value: Boolean) {
    editableState = value
    isFocusable = value
    isFocusableInTouchMode = value
    isEnabled = value
    updateInputType()
    // Prevent keyboard from showing when not editable (and restore it when editable again)
    setShowSoftInputOnFocus(value)
  }

  fun setAutoCapitalizeProp(value: String?) {
    autoCapitalizeValue = value ?: ""
    updateInputType()
  }

  fun setKeyboardTypeProp(value: String?) {
    keyboardTypeValue = value ?: ""
    updateInputType()
  }

  fun setMaxLengthProp(value: Int) {
    maxLengthValue = value
  }

  fun setReturnKeyTypeProp(value: String?) {
    val type = value ?: ""
    submitOnReturn = type.isNotEmpty() && type != "default"
    imeOptions = when (type) {
      "done" -> EditorInfo.IME_ACTION_DONE
      "go" -> EditorInfo.IME_ACTION_GO
      "next" -> EditorInfo.IME_ACTION_NEXT
      "search" -> EditorInfo.IME_ACTION_SEARCH
      "send" -> EditorInfo.IME_ACTION_SEND
      "previous" -> EditorInfo.IME_ACTION_PREVIOUS
      "none" -> EditorInfo.IME_ACTION_NONE
      else -> EditorInfo.IME_ACTION_UNSPECIFIED
    }
    restartInputIfActive()
  }

  fun setPlaceholderProp(value: String?) {
    hint = value
  }

  fun setPlaceholderTextColorProp(color: Int?) {
    if (color != null) {
      setHintTextColor(color)
    } else {
      setHintTextColor(defaultHintTextColors)
    }
  }

  private val defaultHintTextColors = hintTextColors

  private fun updateInputType() {
    val (inputClass, variation) = when (keyboardTypeValue) {
      "email-address" -> InputType.TYPE_CLASS_TEXT to InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
      "url" -> InputType.TYPE_CLASS_TEXT to InputType.TYPE_TEXT_VARIATION_URI
      "visible-password" -> InputType.TYPE_CLASS_TEXT to InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
      "numeric" -> InputType.TYPE_CLASS_NUMBER to
        (InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL)
      "decimal-pad" -> InputType.TYPE_CLASS_NUMBER to InputType.TYPE_NUMBER_FLAG_DECIMAL
      "number-pad" -> InputType.TYPE_CLASS_NUMBER to 0
      "phone-pad" -> InputType.TYPE_CLASS_PHONE to 0
      else -> InputType.TYPE_CLASS_TEXT to 0
    }

    var type = inputClass or variation
    if (inputClass == InputType.TYPE_CLASS_TEXT) {
      // Always keep multiline flag to preserve newlines, even when not editable
      type = type or InputType.TYPE_TEXT_FLAG_MULTI_LINE
      type = type or when (autoCapitalizeValue) {
        "sentences" -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        "words" -> InputType.TYPE_TEXT_FLAG_CAP_WORDS
        "characters" -> InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        else -> 0
      }
      if (!editableState) {
        type = type or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
      }
    }

    // setInputType can switch the typeface (password variations) and single-line
    // mode (non-text classes); keep ours so the rendering does not change.
    val currentTypeface = typeface
    inputType = type
    if (inputClass != InputType.TYPE_CLASS_TEXT) {
      setSingleLine(false)
      maxLines = Int.MAX_VALUE
      setHorizontallyScrolling(false)
    }
    typeface = currentTypeface
  }

  private fun restartInputIfActive() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    if (hasFocus()) imm?.restartInput(this)
  }

  override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
    val connection = super.onCreateInputConnection(outAttrs)
    if (submitOnReturn) {
      // Multi-line editors hide the action key by default; show it so Return submits
      outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
    }
    return connection
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (submitOnReturn && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
      // Hardware Return key: submit instead of inserting a new line
      return true
    }
    return super.onKeyDown(keyCode, event)
  }

  override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
    if (submitOnReturn && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
      onSubmitEditingListener?.invoke(text?.toString() ?: "")
      return true
    }
    return super.onKeyUp(keyCode, event)
  }

  override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
    super.onFocusChanged(focused, direction, previouslyFocusedRect)
    focusEventListener?.invoke(focused)
  }

  override fun onSelectionChanged(selStart: Int, selEnd: Int) {
    super.onSelectionChanged(selStart, selEnd)
    // Called from the TextView constructor before our properties exist
    @Suppress("SENSELESS_COMPARISON")
    if (onSelectionChangeListener != null) {
      onSelectionChangeListener?.invoke(minOf(selStart, selEnd), maxOf(selStart, selEnd))
    }
  }

  // --- Commands (ref methods) --------------------------------------------------

  fun focusFromJs() {
    if (!editableState) return
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(this, 0)
  }

  fun blurFromJs() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
  }

  /** Replaces the text programmatically and reports it through onChange. */
  fun setTextFromJs(newText: String) {
    if (this.text?.toString() != newText) {
      isUpdatingText = true
      setText(newText)
      isUpdatingText = false
      text?.length?.let { setSelection(it) }
      invalidate()
    }
    onTextChangeListener?.invoke(newText)
  }

  // --- Layout helpers ----------------------------------------------------------

  private fun applyLineHeightAndSpacing() {
    val metrics = resources.displayMetrics
    // lineSpacing (Android only) is extra space between lines, in points
    val extraSpacing = if (customLineSpacing != 0f) spToPx(customLineSpacing, metrics) else 0f

    if (customLineHeight > 0f) {
      // Like iOS, lineHeight (points, scaled as sp like fontSize) is the exact
      // distance between baselines: add the difference to the font's own height.
      val desiredLineHeightPx = spToPx(customLineHeight, metrics)
      val fmi = paint.fontMetricsInt
      val fontHeightPx = (fmi.descent - fmi.ascent).toFloat()
      setLineSpacing(desiredLineHeightPx - fontHeightPx + extraSpacing, 1.0f)
    } else {
      // Default: the font's own line height like iOS. Vertical padding of
      // neighbouring lines overlaps and joins into one shape.
      setLineSpacing(extraSpacing, 1.0f)
    }
  }

  private fun spToPx(value: Float, metrics: android.util.DisplayMetrics): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, metrics)

  private fun applyLetterSpacing() {
    // React Native's letterSpacing is specified in layout points. Convert that to
    // Android's "em" units: pxSpacing / textSizePx.
    val metrics = resources.displayMetrics
    val pxSpacing = spToPx(letterSpacingPoints, metrics)
    val textPx = textSize
    if (textPx > 0f) {
      super.setLetterSpacing(pxSpacing / textPx)
    } else {
      super.setLetterSpacing(0f)
    }
  }
}
