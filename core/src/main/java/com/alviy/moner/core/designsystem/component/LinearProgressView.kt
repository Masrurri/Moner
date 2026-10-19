package com.alviy.moner.core.designsystem.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.alviy.moner.core.R

/** Flat rounded bar progress (0f..1f). Attributes: lpv_progress, lpv_strokeWidth, lpv_trackColor, lpv_progressColor. */
class LinearProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackBounds = RectF()
    private val progressBounds = RectF()
    private val trackPaint = fillPaint()
    private val progressPaint = fillPaint()

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var strokeWidth: Float = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
            requestLayout()
            updateBounds()
            invalidate()
        }

    var trackColor: Int = 0
        set(value) {
            field = value
            trackPaint.color = value
            invalidate()
        }

    var progressColor: Int = 0
        set(value) {
            field = value
            progressPaint.color = value
            invalidate()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.LinearProgressView) {
            progress = getFloat(R.styleable.LinearProgressView_lpv_progress, 0f)
            strokeWidth = getDimension(
                R.styleable.LinearProgressView_lpv_strokeWidth,
                resources.getDimension(R.dimen.progress_stroke_default)
            )
            trackColor = getColor(
                R.styleable.LinearProgressView_lpv_trackColor,
                context.getColor(R.color.color_progress_track_default)
            )
            progressColor = getColor(
                R.styleable.LinearProgressView_lpv_progressColor,
                context.getColor(R.color.color_accent_neon)
            )
        }
        elevation = 0f
        stateListAnimator = null
        background = null
    }

    private fun updateBounds() {
        val verticalInset = (height - strokeWidth) / 2f
        trackBounds.set(0f, verticalInset, width.toFloat(), verticalInset + strokeWidth)
        progressBounds.set(0f, verticalInset, width * progress, verticalInset + strokeWidth)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBounds()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateBounds()
        val radius = strokeWidth / 2f
        canvas.drawRoundRect(trackBounds, radius, radius, trackPaint)
        if (progress > 0f) canvas.drawRoundRect(progressBounds, radius, radius, progressPaint)
    }

    private fun fillPaint() = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = false
    }
}
