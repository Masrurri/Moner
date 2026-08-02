package com.alviy.moner.core.designsystem

import androidx.annotation.DrawableRes
import com.alviy.moner.core.R

@DrawableRes
fun iconForCategory(categoryName: String): Int {
    val name = categoryName.lowercase()
    return when {
        listOf("food", "makan", "eat", "restaurant").any { name.contains(it) } -> R.drawable.ic_food
        listOf("transport", "travel", "ride", "car", "gojek", "grab").any { name.contains(it) } -> R.drawable.ic_transport
        listOf("shop", "belanja").any { name.contains(it) } -> R.drawable.ic_shopping
        else -> R.drawable.ic_expense
    }
}
