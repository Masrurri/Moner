package com.alviy.moner.api.gmail.model

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

enum class PaymentMethod(val label: String) {
    QRIS("QRIS"), DEBIT("Debit"), TRANSFER("Transfer"), EWALLET("E-wallet"), CREDIT("Credit card");

    companion object {
        fun fromText(text: String?): PaymentMethod? {
            val s = text?.lowercase() ?: return null
            return when {
                "qris" in s -> QRIS
                "debit" in s -> DEBIT
                "kredit" in s || "credit" in s || "visa" in s || "mastercard" in s -> CREDIT
                "top-up" in s || "top up" in s || "transfer" in s -> TRANSFER
                "ovo" in s || "gopay" in s || "dana" in s || "shopeepay" in s || "linkaja" in s || "wallet" in s -> EWALLET
                else -> null
            }
        }
    }
}