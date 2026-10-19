package com.alviy.moner.core.designsystem

import androidx.annotation.DrawableRes
import com.alviy.moner.core.R

/** Fixed set of spend categories, one per category icon. [label] is what gets stored on a transaction. */
enum class SpendCategory(val label: String, @DrawableRes val icon: Int) {
    FOOD("Food", R.drawable.ic_food_category),
    TRANSPORT("Transport", R.drawable.ic_transport_category),
    SHOPPING("Shopping", R.drawable.ic_shoping_category),
    VEHICLE("Vehicle", R.drawable.ic_vehicle_maintain_category),
    ENTERTAINMENT("Entertainment", R.drawable.ic_entertaiment_category),
    BILLS("Bills", R.drawable.ic_bills_category),
    OTHERS("Others", R.drawable.ic_others_category);

    companion object {
        val labels: List<String> = entries.map { it.label }

        /** Unknown or legacy names fall back to [OTHERS]. */
        fun fromLabel(label: String): SpendCategory =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHERS
    }
}

@DrawableRes
fun iconForCategory(categoryName: String): Int = SpendCategory.fromLabel(categoryName).icon
