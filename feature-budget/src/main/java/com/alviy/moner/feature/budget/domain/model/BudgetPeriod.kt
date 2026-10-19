package com.alviy.moner.feature.budget.domain.model

enum class BudgetPeriod {
    MONTHLY,
    WEEKLY;

    companion object {
        /** Stored as the enum name; unknown or legacy values fall back to [MONTHLY]. */
        fun fromStored(value: String): BudgetPeriod = entries.firstOrNull { it.name == value } ?: MONTHLY
    }
}
