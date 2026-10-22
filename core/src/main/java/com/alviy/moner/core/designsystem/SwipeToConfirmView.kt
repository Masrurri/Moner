package com.alviy.moner.core.designsystem

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.updateLayoutParams
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.alviy.moner.core.R
import com.alviy.moner.core.databinding.ViewSwipeToConfirmBinding
import com.alviy.moner.core.designsystem.SwipeToConfirmView.Companion.AUTO
import com.alviy.moner.core.designsystem.SwipeToConfirmView.Companion.PILL
import kotlin.math.roundToInt

/**
 * Pill-shaped "slide to confirm" control: drag the thumb to the end to fire [onConfirmed].
 * Released early, it springs back. TalkBack users confirm with a double-tap on the thumb.
 */
/**
 * Pill-shaped "slide to confirm" control: drag the thumb to the end to fire [onConfirmed].
 * Released early, it springs back. TalkBack users confirm with a double-tap on the thumb.
 *
 * Styleable from XML (all optional):
 *   app:stc_text, app:stc_textAppearance, app:stc_textColor, app:stc_textSize,
 *   app:stc_labelPaddingHorizontal, app:stc_trackColor, app:stc_cornerRadius,
 *   app:stc_contentPadding, app:stc_thumbSize, app:stc_thumbColor, app:stc_thumbIcon,
 *   app:stc_thumbIconTint, app:stc_confirmThreshold
 */
@SuppressLint("ClickableViewAccessibility")
class SwipeToConfirmView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewSwipeToConfirmBinding.inflate(LayoutInflater.from(context), this)

    private val trackDrawable = GradientDrawable()
    private val thumbDrawable = GradientDrawable()

    var onConfirmed: (() -> Unit)? = null

    // ---------------------------------------------------------------- styleable properties

    var text: CharSequence?
        get() = binding.tvLabel.text
        set(value) {
            binding.tvLabel.text = value
            binding.ivThumb.contentDescription = value
        }

    @ColorInt
    var trackColor: Int = DEFAULT_TRACK_COLOR
        set(value) {
            field = value
            trackDrawable.setColor(value)
        }

    @ColorInt
    var thumbColor: Int = 0
        set(value) {
            field = value
            thumbDrawable.setColor(value)
        }

    var thumbIcon: Drawable?
        get() = binding.ivThumb.drawable
        set(value) = binding.ivThumb.setImageDrawable(value)

    var thumbIconTint: ColorStateList?
        get() = ImageViewCompat.getImageTintList(binding.ivThumb)
        set(value) = ImageViewCompat.setImageTintList(binding.ivThumb, value)

    /** Track corner radius in px; [PILL] (negative) = half the height, for a full pill. */
    var cornerRadius: Float = PILL
        set(value) {
            field = value
            applyShapes()
        }

    /** Gap between the track edge and the thumb, in px. */
    var contentPadding: Int = 0
        set(value) {
            field = value
            applyThumbLayout()
        }

    var thumbSize: Int = 0
        set(value) {
            field = value
            applyThumbLayout()
        }

    /** Horizontal padding of the label, in px; [AUTO] (negative) = clear of the thumb on both sides. */
    var labelPaddingHorizontal: Int = AUTO
        set(value) {
            field = value
            applyThumbLayout()
        }

    /** Fraction of the travel (0–1) the thumb must pass for a release to confirm. */
    var confirmThreshold: Float = DEFAULT_CONFIRM_THRESHOLD
        set(value) {
            field = value.coerceIn(0.1f, 1f)
        }

    fun setTextColor(colors: ColorStateList) = binding.tvLabel.setTextColor(colors)

    fun setTextSize(unit: Int, size: Float) = binding.tvLabel.setTextSize(unit, size)

    // ---------------------------------------------------------------- drag state

    private var downRawX = 0f
    private var startOffset = 0f
    private var thumbOffset = 0f          // 0..maxTravel, measured from the start edge
    private var isConfirmed = false

    private val maxTravel: Float
        get() = (width - binding.ivThumb.width - contentPadding * 2).coerceAtLeast(0).toFloat()

    /** +1 left-to-right, -1 right-to-left, so the thumb always travels from start to end. */
    private val direction: Float
        get() = if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutDirection == View.LAYOUT_DIRECTION_RTL
            } else {
                TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
            }
        ) -1f else 1f

    init {
        background = trackDrawable
        binding.ivThumb.background = thumbDrawable

        val defaultContent = ContextCompat.getColor(context, R.color.white_grey)

        context.withStyledAttributes(attrs, R.styleable.SwipeToConfirmView) {
            // Label: appearance first, then explicit color/size override it.
            val appearance = getResourceId(R.styleable.SwipeToConfirmView_stc_textAppearance, 0)
            TextViewCompat.setTextAppearance(
                binding.tvLabel,
                if (appearance != 0) appearance else R.style.TextAppearance_Moner_Bold
            )
            binding.tvLabel.setTextColor(
                getColorStateList(R.styleable.SwipeToConfirmView_stc_textColor)
                    ?: ColorStateList.valueOf(defaultContent)
            )
            when {
                hasValue(R.styleable.SwipeToConfirmView_stc_textSize) -> binding.tvLabel.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getDimension(R.styleable.SwipeToConfirmView_stc_textSize, 0f)
                )
                appearance == 0 -> binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP)
            }
            text = getText(R.styleable.SwipeToConfirmView_stc_text)

            // Track
            trackColor = getColor(R.styleable.SwipeToConfirmView_stc_trackColor, DEFAULT_TRACK_COLOR)
            cornerRadius = getDimension(R.styleable.SwipeToConfirmView_stc_cornerRadius, PILL)

            // Thumb
            thumbColor = getColor(R.styleable.SwipeToConfirmView_stc_thumbColor, defaultContent)
            thumbIcon = getDrawable(R.styleable.SwipeToConfirmView_stc_thumbIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.ic_arrow_right_circle)
            thumbIconTint = getColorStateList(R.styleable.SwipeToConfirmView_stc_thumbIconTint)
                ?: ColorStateList.valueOf(trackColor)

            // Spacing
            contentPadding = getDimensionPixelSize(R.styleable.SwipeToConfirmView_stc_contentPadding, dp(4))
            thumbSize = getDimensionPixelSize(R.styleable.SwipeToConfirmView_stc_thumbSize, dp(48))
            labelPaddingHorizontal =
                getDimensionPixelSize(R.styleable.SwipeToConfirmView_stc_labelPaddingHorizontal, AUTO)

            confirmThreshold =
                getFloat(R.styleable.SwipeToConfirmView_stc_confirmThreshold, DEFAULT_CONFIRM_THRESHOLD)
        }

        binding.ivThumb.setOnTouchListener(::onThumbTouch)
        binding.ivThumb.setOnClickListener { confirm() }   // only reached via accessibility; touches are consumed above
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyShapes()
        if (isConfirmed) binding.ivThumb.translationX = maxTravel * direction   // keep it at the end after a resize
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else DISABLED_ALPHA
    }

    // ---------------------------------------------------------------- layout helpers

    private fun applyThumbLayout() {
        binding.ivThumb.updateLayoutParams<LayoutParams> {
            width = thumbSize
            height = thumbSize
            marginStart = contentPadding
            marginEnd = contentPadding
        }
        val labelPad = if (labelPaddingHorizontal >= 0) labelPaddingHorizontal else contentPadding + thumbSize
        binding.tvLabel.setPaddingRelative(labelPad, 0, labelPad, 0)
        minimumHeight = thumbSize + contentPadding * 2
        applyShapes()
    }

    /** Track: pill or fixed radius. Thumb: circle for a pill, otherwise a concentric rounded square. */
    private fun applyShapes() {
        val radius = if (cornerRadius < 0f) height / 2f else cornerRadius
        trackDrawable.cornerRadius = radius
        if (cornerRadius < 0f) {
            thumbDrawable.shape = GradientDrawable.OVAL
        } else {
            thumbDrawable.shape = GradientDrawable.RECTANGLE
            thumbDrawable.cornerRadius = (radius - contentPadding).coerceAtLeast(0f)
        }
    }

    // ---------------------------------------------------------------- drag handling

    private fun onThumbTouch(v: View, event: MotionEvent): Boolean {
        if (isConfirmed || !isEnabled) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                binding.ivThumb.animate().cancel()
                downRawX = event.rawX
                startOffset = thumbOffset
                parent?.requestDisallowInterceptTouchEvent(true)   // keep the sheet/scroll from stealing the drag
            }
            MotionEvent.ACTION_MOVE -> {
                val dragged = (event.rawX - downRawX) * direction
                moveThumb((startOffset + dragged).coerceIn(0f, maxTravel))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (thumbOffset >= maxTravel * confirmThreshold) confirm() else reset()
            }
        }
        return true
    }

    private fun moveThumb(offset: Float) {
        thumbOffset = offset
        binding.ivThumb.translationX = offset * direction
        binding.tvLabel.alpha = if (maxTravel > 0f) 1f - offset / maxTravel else 1f
    }

    private fun confirm() {
        if (isConfirmed) return
        isConfirmed = true
        thumbOffset = maxTravel
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        binding.tvLabel.animate().alpha(0f).setDuration(ANIM_MS).start()
        binding.ivThumb.animate()
            .translationX(maxTravel * direction)
            .setDuration(ANIM_MS)
            .withEndAction { onConfirmed?.invoke() }
            .start()
    }

    /** Puts the thumb back at the start, e.g. if the action failed and the user should retry. */
    fun reset() {
        isConfirmed = false
        thumbOffset = 0f
        binding.ivThumb.animate().translationX(0f).setDuration(ANIM_MS).start()
        binding.tvLabel.animate().alpha(1f).setDuration(ANIM_MS).start()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()

    companion object {
        /** Use as [cornerRadius] for a full pill (radius = half the height). */
        const val PILL = -1f

        /** Use as [labelPaddingHorizontal] to keep the label clear of the thumb automatically. */
        const val AUTO = -1

        private const val DEFAULT_TRACK_COLOR = 0xFF333333.toInt()
        private const val DEFAULT_TEXT_SIZE_SP = 14f
        private const val DEFAULT_CONFIRM_THRESHOLD = 0.85f
        private const val DISABLED_ALPHA = 0.5f
        private const val ANIM_MS = 180L
    }
}