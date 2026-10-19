package com.alviy.moner.api.gmail.domain.model

enum class PaymentMethod(val label: String) {
    QRIS("QRIS"),
    DEBIT("Debit"),
    TRANSFER("Transfer"),
    EWALLET("E-wallet"),
    CREDIT("Credit card");

    companion object {
        /** Keywords checked in order; the first group with a hit wins. */
        private val KEYWORDS: List<Pair<PaymentMethod, List<String>>> = listOf(
            QRIS to listOf("qris"),
            DEBIT to listOf("debit"),
            CREDIT to listOf("kredit", "credit", "visa", "mastercard"),
            TRANSFER to listOf("top-up", "top up", "transfer"),
            EWALLET to listOf("ovo", "gopay", "dana", "shopeepay", "linkaja", "wallet"),
        )

        /** Guesses the method from free text such as "Pembayaran QRIS" or "Paid by OVO". */
        fun fromText(text: String?): PaymentMethod? {
            val s = text?.lowercase() ?: return null
            return KEYWORDS.firstOrNull { (_, words) -> words.any { it in s } }?.first
        }
    }
}
