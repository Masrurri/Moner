package com.alviy.moner.core.designsystem.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.alviy.moner.core.R

/** Ring progress (0f..1f) starting at 12 o'clock. Attributes: cpv_progress, cpv_strokeWidth, cpv_trackColor, cpv_progressColor. */
class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bounds = RectF()
    private val trackPaint = strokePaint()
    private val progressPaint = strokePaint()

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var strokeWidth: Float = 0f
        set(value) {
            field = value
            trackPaint.strokeWidth = value
            progressPaint.strokeWidth = value
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
        context.withStyledAttributes(attrs, R.styleable.CircularProgressView) {
            progress = getFloat(R.styleable.CircularProgressView_cpv_progress, 0f)
            strokeWidth = getDimension(
                R.styleable.CircularProgressView_cpv_strokeWidth,
                resources.getDimension(R.dimen.progress_stroke_default)
            )
            trackColor = getColor(
                R.styleable.CircularProgressView_cpv_trackColor,
                context.getColor(R.color.color_progress_track_default)
            )
            progressColor = getColor(
                R.styleable.CircularProgressView_cpv_progressColor,
                context.getColor(R.color.color_accent_neon)
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = strokeWidth / 2f
        bounds.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(bounds, 0f, FULL_SWEEP, false, trackPaint)
        canvas.drawArc(bounds, START_ANGLE, FULL_SWEEP * progress, false, progressPaint)
    }

    private fun strokePaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private companion object {
        const val FULL_SWEEP = 360f
        const val START_ANGLE = -90f
    }
}
