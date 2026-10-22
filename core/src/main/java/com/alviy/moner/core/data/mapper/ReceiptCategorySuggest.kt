package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.designsystem.SpendCategory
import com.alviy.moner.core.domain.model.ReceiptActivity
import java.util.Locale

/**
 * Best-guess [SpendCategory] for a parsed receipt. The user can still pick another one in the sheet.
 * Grab activities map directly; payments are matched on the destination name; anything unmatched is [SpendCategory.OTHERS].
 */
fun suggestSpendCategory(activity: ReceiptActivity, destination: String): SpendCategory {
    when (activity) {
        ReceiptActivity.GRAB_BIKE, ReceiptActivity.GRAB_CAR -> return SpendCategory.TRANSPORT
        ReceiptActivity.GRAB_FOOD -> return SpendCategory.FOOD
        ReceiptActivity.TRANSFER, ReceiptActivity.TOP_UP -> return SpendCategory.OTHERS
        ReceiptActivity.PAYMENT, ReceiptActivity.OTHER -> Unit
    }
    val text = destination.lowercase(Locale.ROOT)
    return CATEGORY_KEYWORDS.firstOrNull { (_, pattern) -> pattern.containsMatchIn(text) }?.first
        ?: SpendCategory.OTHERS
}

/**
 * Checked in this order, so more specific groups win (a "tiket bioskop" is Entertainment, not Transport;
 * an "app store" charge is Entertainment, not Shopping). Whole-word matches only, so "tol" won't hit "bristol".
 */
private val CATEGORY_KEYWORDS: List<Pair<SpendCategory, Regex>> = listOf(
    SpendCategory.BILLS to keywords(
        "pln", "listrik", "token listrik", "pdam", "telkom", "indihome", "biznet", "first media", "myrepublic",
        "pulsa", "paket data", "telkomsel", "xl", "axis", "indosat", "im3", "smartfren", "bpjs", "asuransi",
        "cicilan", "angsuran", "kartu kredit", "tagihan", "internet", "wifi", "pajak", "pbb"
    ),
    SpendCategory.ENTERTAINMENT to keywords(
        "netflix", "spotify", "youtube", "disney", "vidio", "viu", "wetv", "cgv", "xxi", "cinepolis", "bioskop",
        "steam", "playstation", "nintendo", "google play", "app store", "apple", "game", "karaoke", "konser"
    ),
    SpendCategory.VEHICLE to keywords(
        "spbu", "pertamina", "shell", "bensin", "bbm", "pertalite", "pertamax", "solar", "bengkel", "servis",
        "ahass", "oli", "ban", "cuci mobil", "cuci motor", "samsat", "sparepart", "suku cadang"
    ),
    SpendCategory.TRANSPORT to keywords(
        "gojek", "goride", "gocar", "grab", "maxim", "indrive", "bluebird", "taksi", "taxi", "krl", "commuter",
        "mrt", "lrt", "transjakarta", "kai", "kereta", "whoosh", "tiket", "pesawat", "garuda", "lion air",
        "citilink", "airasia", "traveloka", "tiket.com", "parkir", "parking", "tol", "toll", "e-toll", "damri"
    ),
    SpendCategory.FOOD to keywords(
        "kopi", "coffee", "cafe", "kafe", "resto", "restoran", "restaurant", "warung", "warteg", "rumah makan",
        "bakso", "mie", "ayam", "sate", "nasi", "padang", "seafood", "bakery", "roti", "kfc", "mcd", "mcdonald",
        "starbucks", "janji jiwa", "kenangan", "fore", "chatime", "mixue", "hokben", "pizza", "burger", "solaria",
        "gofood", "shopeefood", "makan", "food"
    ),
    SpendCategory.SHOPPING to keywords(
        "indomaret", "alfamart", "alfamidi", "superindo", "hypermart", "transmart", "lotte", "hero", "ranch market",
        "tokopedia", "shopee", "lazada", "blibli", "tiktok shop", "uniqlo", "zara", "ace hardware", "informa",
        "ikea", "mall", "store", "toko", "mart", "minimarket", "supermarket", "apotek", "guardian", "watsons"
    ),
)

private fun keywords(vararg words: String): Regex =
    Regex("\\b(" + words.joinToString("|") { Regex.escape(it) } + ")\\b")