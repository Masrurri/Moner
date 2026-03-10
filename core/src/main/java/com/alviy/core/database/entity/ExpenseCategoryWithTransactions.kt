package com.alviy.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseCategoryWithTransactions(

    @Embedded
    val category: ExpenseCategoryEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val transactions: List<TransactionEntity>
)