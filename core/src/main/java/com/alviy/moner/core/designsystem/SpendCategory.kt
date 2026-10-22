package com.alviy.moner.core.designsystem

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.alviy.moner.core.R

/**
 * Fixed set of spend categories, one per category icon. [label] is what gets stored on a transaction.
 * [containerColor] is the light badge/pill background, [contentColor] the matching icon + label color.
 */
enum class SpendCategory(
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val badge: Int,
    @ColorRes val containerColor: Int,
    @ColorRes val contentColor: Int
) {
    FOOD("Food", R.drawable.ic_category_food, R.drawable.ic_category_food_badge,
        R.color.category_food_container, R.color.category_food_content),
    TRANSPORT("Transport", R.drawable.ic_category_transport, R.drawable.ic_category_transport_badge,
        R.color.category_transport_container, R.color.category_transport_content),
    SHOPPING("Shopping", R.drawable.ic_category_shopping, R.drawable.ic_category_shopping_badge,
        R.color.category_shopping_container, R.color.category_shopping_content),
    VEHICLE("Vehicle", R.drawable.ic_category_vehicle, R.drawable.ic_category_vehicle_badge,
        R.color.category_vehicle_container, R.color.category_vehicle_content),
    ENTERTAINMENT("Entertainment", R.drawable.ic_category_entertainment, R.drawable.ic_category_entertainment_badge,
        R.color.category_entertainment_container, R.color.category_entertainment_content),
    BILLS("Bills", R.drawable.ic_category_bills, R.drawable.ic_category_bills_badge,
        R.color.category_bills_container, R.color.category_bills_content),
    OTHERS("Others", R.drawable.ic_category_others, R.drawable.ic_category_others_badge,
        R.color.category_others_container, R.color.category_others_content);

    /** Badge = icon on its colored rounded tile; otherwise the bare icon. */
    @DrawableRes
    fun drawable(withBadge: Boolean = true): Int = if (withBadge) badge else icon

    companion object {
        val labels: List<String> = entries.map { it.label }

        /** Unknown or legacy names fall back to [OTHERS]. */
        fun fromLabel(label: String): SpendCategory =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHERS
    }
}

@DrawableRes
fun iconForCategory(categoryName: String, withBadge: Boolean = true): Int =
    SpendCategory.fromLabel(categoryName).drawable(withBadge)

