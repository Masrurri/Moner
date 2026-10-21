package com.alviy.moner.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/** A fetched email receipt. Enums are stored by name; see ReceiptMapper. */
@Entity(tableName = "receipt")
data class ReceiptEntity(
    @PrimaryKey
    val id: String,
    val channel: String,
    val activity: String,
    val destination: String,
    val reference: String?,
    val amount: BigDecimal,
    val transactionAt: Long,
    val receivedAt: Long,
    val method: String?,
    val note: String?,
    val isRecorded: Boolean,
    val isDeleted: Boolean,
)
