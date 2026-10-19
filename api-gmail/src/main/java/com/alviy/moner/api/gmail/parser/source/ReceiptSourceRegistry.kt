package com.alviy.moner.api.gmail.parser.source

import javax.inject.Inject
import javax.inject.Singleton

/** Builds the Gmail search query from the sources and routes each email to its source. */
@Singleton
class ReceiptSourceRegistry @Inject constructor(
    private val sources: List<@JvmSuppressWildcards ReceiptSource>,
) {

    /** The discovery query if set, otherwise one built from the sources. */
    fun searchQuery(): String = ReceiptParserConfig.DISCOVERY_QUERY ?: buildQuery()

    /** (from:(a OR b) subject:("x" OR "y")) OR (...) newer_than:60d */
    fun buildQuery(newerThan: String = ReceiptParserConfig.NEWER_THAN): String {
        val groups = sources.map { s ->
            listOfNotNull(
                s.from.takeIf { it.isNotEmpty() }?.joinToString(" OR ", "from:(", ")"),
                s.subjects.takeIf { it.isNotEmpty() }
                    ?.joinToString(" OR ", "subject:(", ")") { "\"${it.replace("\"", "")}\"" },
            ).joinToString(" ", "(", ")")
        }
        return "(${groups.joinToString(" OR ")}) newer_than:$newerThan"
    }

    /** First source whose sender and subject both match; null = not a supported receipt. */
    fun match(from: String, subject: String): ReceiptSource? = sources.firstOrNull { s ->
        (s.from.isEmpty() || s.from.any { from.contains(it, ignoreCase = true) }) &&
            (s.subjects.isEmpty() || s.subjects.any { subject.contains(it, ignoreCase = true) })
    }
}
