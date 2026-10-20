package com.alviy.moner.core.designsystem

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.graphics.drawable.RotateDrawable
import android.view.animation.LinearInterpolator
import androidx.core.view.doOnDetach
import com.alviy.moner.core.R
import com.google.android.material.button.MaterialButton

private class IconSpinner(val original: Drawable, val spinning: Drawable?, val animator: ValueAnimator)

/**
 * Spins the button's current icon around its centre while [spinning] is true (e.g. during a fetch)
 * and puts the original icon back when stopped. Calling it with the current state is a no-op, and the
 * animation stops by itself if the button is detached.
 */
fun MaterialButton.setIconSpinning(spinning: Boolean) {
    val running = getTag(R.id.tag_icon_spinner) as? IconSpinner
    if (spinning == (running != null)) return

    if (!spinning) {
        running?.animator?.cancel()
        // Only restore if nobody swapped the icon meanwhile (e.g. the label changed to "Auth Gmail").
        if (running != null && icon === running.spinning) icon = running.original
        setTag(R.id.tag_icon_spinner, null)
        return
    }

    val original = icon ?: return
    val rotating = RotateDrawable().apply {
        drawable = original
        fromDegrees = 0f
        toDegrees = 360f
    }
    icon = rotating
    val animator = ValueAnimator.ofInt(0, MAX_LEVEL).apply {
        duration = SPIN_DURATION_MS
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { rotating.level = it.animatedValue as Int }
        start()
    }
    // MaterialButton wraps the drawable it is given; keep the wrapped instance to compare against.
    setTag(R.id.tag_icon_spinner, IconSpinner(original, icon, animator))
    doOnDetach { setIconSpinning(false) }
}

private const val MAX_LEVEL = 10_000 // Drawable level range: RotateDrawable maps it to 0..360 degrees
private const val SPIN_DURATION_MS = 900L
