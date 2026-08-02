package com.alviy.moner.core.designsystem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.alviy.moner.core.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bounds = RectF()

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

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
            strokeWidth = getDimension(R.styleable.CircularProgressView_cpv_strokeWidth, dpToPx(6f))
            trackColor = getColor(R.styleable.CircularProgressView_cpv_trackColor, 0x33000000)
            progressColor = getColor(R.styleable.CircularProgressView_cpv_progressColor, 0xFFD6FF3F.toInt())
        }
    }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = strokeWidth / 2f
        bounds.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(bounds, 0f, 360f, false, trackPaint)
        canvas.drawArc(bounds, -90f, 360f * progress, false, progressPaint)
    }
}
