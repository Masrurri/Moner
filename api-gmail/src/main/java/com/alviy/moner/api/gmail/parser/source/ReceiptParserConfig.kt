package com.alviy.moner.api.gmail.parser.source

import com.alviy.moner.api.gmail.BuildConfig

/** Knobs for the receipt search and parser. */
object ReceiptParserConfig {

    /** Gmail "newer_than:" window for the search query. */
    const val NEWER_THAN = "60d"

    /** null = normal mode (query built from the sources). Set temporarily to discover senders. */
    val DISCOVERY_QUERY: String? = null

    /** Logs From, Subject and parsed rows. Rows contain personal data, so this is never on in release builds. */
    val DEBUG_LOG: Boolean = BuildConfig.DEBUG
}
