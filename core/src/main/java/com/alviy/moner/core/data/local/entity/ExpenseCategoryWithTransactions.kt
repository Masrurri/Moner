package com.alviy.moner.core.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import java.math.BigDecimal

data class ExpenseCategoryWithTransactions(

    @Embedded
    val category: ExpenseCategoryEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val transactions: List<TransactionEntity>
){
    val totalExpenseCategory: BigDecimal
        get() = transactions.fold(BigDecimal.ZERO) { total, trx ->
            total + trx.amount
        }
}