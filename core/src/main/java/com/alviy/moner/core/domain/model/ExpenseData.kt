package com.alviy.moner.core.domain.model

data class ExpenseData(
    val profile: Profile = Profile(),
    val journalExpense: List<ExpenseItem> = emptyList()
)