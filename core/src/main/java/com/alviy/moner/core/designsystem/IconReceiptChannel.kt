package com.alviy.moner.core.designsystem

import androidx.annotation.DrawableRes
import com.alviy.moner.core.R

/** Fixed set of spend categories, one per category icon. [label] is what gets stored on a transaction. */
enum class IconReceiptChannel(val label: String, @DrawableRes val icon: Int) {
    MANDIRI("Mandiri", R.drawable.ic_bmri),
    BCA("BCA", R.drawable.ic_bca),
    GOPAY("GoPay", R.drawable.ic_gopay),
    BRI("BRI", R.drawable.ic_bri),
    SHOPEE("Shopee", R.drawable.ic_shopee),
    GOJEK("Gojek", R.drawable.ic_gojek),
    OVO("Ovo", R.drawable.ic_ovo),
    GRAB("Grab", R.drawable.ic_grab),
    OTHERS("Others", R.drawable.ic_global_search_square);

    companion object {
        val labels: List<String> = entries.map { it.label }

        /** Unknown or legacy names fall back to [OTHERS]. */
        fun fromLabel(label: String): IconReceiptChannel =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHERS
    }
}

@DrawableRes
fun iconForCahannel(channel: String): Int = IconReceiptChannel.fromLabel(channel).icon