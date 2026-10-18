package com.alviy.moner.core.designsystem

import androidx.annotation.DrawableRes
import com.alviy.moner.core.R

@DrawableRes
fun iconForCategory(categoryName: String): Int {
    val name = categoryName.lowercase()
    return when {
        listOf("food", "makan", "eat", "restaurant").any { name.contains(it) } -> R.drawable.ic_food_category
        listOf("transport", "travel", "ride", "car", "gojek", "grab").any { name.contains(it) } -> R.drawable.ic_transport_category
        listOf("shop", "belanja").any { name.contains(it) } -> R.drawable.ic_shoping_category
        listOf("bensin", "gas", "oli", "oil", "service").any { name.contains(it) } -> R.drawable.ic_vehicle_maintain_category
        listOf("nonton", "film", "entertaiment", "concert", "ticket").any { name.contains(it) } -> R.drawable.ic_entertaiment_category
        listOf("listrik", "bills", "tagihan", "internet").any { name.contains(it) } -> R.drawable.ic_bills_category
        else -> R.drawable.ic_others_category
    }
}
