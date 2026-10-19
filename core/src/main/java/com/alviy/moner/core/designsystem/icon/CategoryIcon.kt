package com.alviy.moner.core.designsystem.icon

import androidx.annotation.DrawableRes
import com.alviy.moner.core.R

/**
 * Picks an icon from keywords in a free-text category name (English and Indonesian).
 * To support a new category, add a row to [RULES]; the first match wins.
 */
object CategoryIcon {

    private class Rule(val keywords: List<String>, @DrawableRes val icon: Int)

    private val RULES = listOf(
        Rule(listOf("food", "makan", "eat", "restaurant"), R.drawable.ic_food_category),
        Rule(listOf("transport", "travel", "ride", "car", "gojek", "grab"), R.drawable.ic_transport_category),
        Rule(listOf("shop", "belanja"), R.drawable.ic_shoping_category),
        Rule(listOf("bensin", "gas", "oli", "oil", "service"), R.drawable.ic_vehicle_maintain_category),
        Rule(listOf("nonton", "film", "entertaiment", "concert", "ticket"), R.drawable.ic_entertaiment_category),
        Rule(listOf("listrik", "bills", "tagihan", "internet"), R.drawable.ic_bills_category),
    )

    @DrawableRes
    private val FALLBACK = R.drawable.ic_others_category

    @DrawableRes
    fun forName(categoryName: String): Int {
        val name = categoryName.lowercase()
        return RULES.firstOrNull { rule -> rule.keywords.any(name::contains) }?.icon ?: FALLBACK
    }
}
