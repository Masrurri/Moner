package com.alviy.moner.core.designsystem

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.alviy.moner.core.R

/**
 * Draws a moving highlight band over its children while [startShimmer] is active.
 * With `shimmer_maskToContent` (default true) the band only covers the children's painted pixels,
 * so skeleton placeholders shimmer and the gaps between them stay untouched; set it to false to
 * sweep the whole area (e.g. inside a card that already clips to its shape).
 */
class ShimmerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var maskToContent = true
    private val highlightColor = context.getColor(R.color.color_shimmer_highlight)
    private val bandMatrix = Matrix()
    private val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var bandWidth = 0f
    private var progress = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = SHIMMER_DURATION_MS
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }

    val isShimmering: Boolean get() = animator.isStarted

    init {
        setWillNotDraw(false)
        context.withStyledAttributes(attrs, R.styleable.ShimmerLayout) {
            maskToContent = getBoolean(R.styleable.ShimmerLayout_shimmer_maskToContent, true)
        }
        if (maskToContent) bandPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    }

    fun startShimmer() {
        if (!animator.isStarted) animator.start()
    }

    fun stopShimmer() {
        animator.cancel()
        invalidate()
    }

    /** Convenience for state rendering: start or stop in one call. */
    fun setShimmering(shimmering: Boolean) = if (shimmering) startShimmer() else stopShimmer()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bandWidth = w * BAND_WIDTH_RATIO
        bandPaint.shader = LinearGradient(
            0f, 0f, bandWidth, 0f,
            intArrayOf(android.graphics.Color.TRANSPARENT, highlightColor, android.graphics.Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (!isShimmering || width == 0 || height == 0) {
            super.dispatchDraw(canvas)
            return
        }
        val layer = if (maskToContent) canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null) else null
        super.dispatchDraw(canvas)
        // Sweep from fully left of the view to fully right of it.
        bandMatrix.setTranslate(-bandWidth + progress * (width + bandWidth), 0f)
        bandPaint.shader?.setLocalMatrix(bandMatrix)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bandPaint)
        layer?.let(canvas::restoreToCount)
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val SHIMMER_DURATION_MS = 1200L
        const val BAND_WIDTH_RATIO = 0.6f
    }
}
