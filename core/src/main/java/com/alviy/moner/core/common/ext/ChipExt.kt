package com.alviy.moner.core.common.ext

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.alviy.moner.core.designsystem.SpendCategory
import com.google.android.material.chip.Chip

private val CHECKED = intArrayOf(android.R.attr.state_checked)
private val DEFAULT = intArrayOf()

/**
 * Styles a choice [Chip] as a [SpendCategory] pill: light container background,
 * bare icon + label in the content color, and a content-colored outline when selected.
 * The category is stored in [Chip.getTag] so a ChipGroup listener can read it back.
 */
fun Chip.bindCategory(category: SpendCategory) {
    val container = ContextCompat.getColor(context, category.containerColor)
    val content = ContextCompat.getColor(context, category.contentColor)

    tag = category
    text = category.label
    setChipIconResource(category.icon)
    chipIconTint = null // icons already carry their own color

    chipBackgroundColor = ColorStateList.valueOf(container)
    chipStrokeColor = ColorStateList(arrayOf(CHECKED, DEFAULT), intArrayOf(content, Color.TRANSPARENT))
    setTextColor(content)
    rippleColor = ColorStateList.valueOf(ColorUtils.setAlphaComponent(content, 0x33))
}