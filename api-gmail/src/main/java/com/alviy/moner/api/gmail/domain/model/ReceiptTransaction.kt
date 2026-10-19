package com.alviy.moner.api.gmail.domain.model

import java.math.BigDecimal

/** The essentials of one payment, parsed from a Gmail receipt. Amounts are IDR. */
data class ReceiptTransaction(
    val id: String,                 // Gmail message id: list key + de-duplication, not shown
    val channel: String,            // who sent it: Mandiri, BCA, Grab, OVO, Shopee
    val title: String,              // payment subject: "Pembayaran QRIS", "Transfer", "Top-up"
    val destination: String,        // who/where it went: merchant, recipient, provider
    val amount: BigDecimal,         // exact: 26378, not 26378.0
    val date: String,               // "dd/MM/yyyy", always in Jakarta time (WIB)
    val time: String?,              // "HH:mm" in WIB, null when the email has no time
    val method: PaymentMethod?,     // QRIS, debit, transfer... null if the email doesn't say
    val note: String?,              // category or note, if the email has one
)
