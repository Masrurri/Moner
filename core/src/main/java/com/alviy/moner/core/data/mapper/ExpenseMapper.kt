package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.ExpenseCategoryEntity
import com.alviy.moner.core.data.local.entity.ExpenseCategoryWithTransactions
import com.alviy.moner.core.domain.model.ExpenseItem
import com.alviy.moner.core.domain.model.TransactionItem

fun ExpenseCategoryWithTransactions.toExpenseItem(): ExpenseItem {
    return ExpenseItem(
        categoryId = category.categoryId,
        categoryName = category.categoryName,
        monthYear = category.monthYear,
        totalExpense = totalExpenseCategory,
        dateCreated = category.dateCreated,
        transactions = transactions.map {
            TransactionItem(
                categoryId = it.categoryId,
                date = it.date,
                desc = it.desc,
                source = it.source,
                amount = it.amount
            )
        }
    )
}

fun ExpenseCategoryEntity.toExpenseItem(): ExpenseItem {
    return ExpenseItem(
        categoryName = this.categoryName,
        dateCreated = this.dateCreated,
        transactions = null
    )
}

fun List<ExpenseCategoryWithTransactions>.toListExpenseItem(): List<ExpenseItem> {
    return map { expenseData -> expenseData.toExpenseItem() }
}
