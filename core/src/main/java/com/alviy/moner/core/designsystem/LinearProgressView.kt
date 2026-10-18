
package com.alviy.moner.core.designsystem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.alviy.moner.core.R

class LinearProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackBounds = RectF()
    private val progressBounds = RectF()

    private val trackPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = false
        setShadowLayer(0f, 0f, 0f, 0)
    }

    private val progressPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = false
        setShadowLayer(0f, 0f, 0f, 0)
    }

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
        context.withStyledAttributes(
            attrs,
            R.styleable.LinearProgressView
        ) {
            progress = getFloat(
                R.styleable.LinearProgressView_lpv_progress,
                0f
            )

            strokeWidth = getDimension(
                R.styleable.LinearProgressView_lpv_strokeWidth,
                dpToPx(6f)
            )

            trackColor = getColor(
                R.styleable.LinearProgressView_lpv_trackColor,
                0x33000000
            )

            progressColor = getColor(
                R.styleable.LinearProgressView_lpv_progressColor,
                0xFFD6FF3F.toInt()
            )

            elevation = 0f
            stateListAnimator = null
            background = null
        }
    }

    private fun dpToPx(dp: Float): Float =
        dp * resources.displayMetrics.density

    private fun updateBounds() {
        val height = height.toFloat()
        val width = width.toFloat()
        val verticalInset = (height - strokeWidth) / 2f

        trackBounds.set(
            0f,
            verticalInset,
            width,
            verticalInset + strokeWidth
        )

        progressBounds.set(
            0f,
            verticalInset,
            width * progress,
            verticalInset + strokeWidth
        )
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBounds()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        updateBounds()

        val radius = strokeWidth / 2f

        canvas.drawRoundRect(
            trackBounds,
            radius,
            radius,
            trackPaint
        )

        if (progress > 0f) {
            canvas.drawRoundRect(
                progressBounds,
                radius,
                radius,
                progressPaint
            )
        }
    }
}
