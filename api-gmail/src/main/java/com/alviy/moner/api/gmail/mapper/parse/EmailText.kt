package com.alviy.moner.api.gmail.mapper.parse

/**
 * An email body as rows of cells, so label/value tables survive:
 *   <tr><td>Total Bayar</td><td>: IDR 4,000.00</td></tr>  ->  ["Total Bayar", ": IDR 4,000.00"]
 * Pure Kotlin (no Android classes) so it can be unit-tested on the JVM.
 */
class EmailText(val rows: List<List<String>>) {

    /** Whole body as plain text, one row per line. Used only for keyword checks, never stored. */
    val flat: String by lazy { rows.joinToString("\n") { it.joinToString(" ") } }

    /**
     * Value of the first row whose label matches; labels are tried in the order given.
     * Understands three layouts:
     *   cells in one row:      ["Total Bayar", ": IDR 4,000.00"]
     *   "label: value" cell:   ["Total Bayar : IDR 4,000.00"]
     *   one cell per row:      ["Status"], [":"], ["Berhasil"]      (BCA transfer emails)
     */
    fun value(vararg labels: String): String? {
        for (label in labels) {
            val want = norm(label)
            for ((i, row) in rows.withIndex()) {
                val pair = split(row)
                if (pair != null) {
                    if (norm(pair.first) == want && pair.second.isNotBlank()) return pair.second
                } else if (row.size == 1 && norm(row[0]) == want) {
                    valueOnFollowingRows(i)?.let { return it }
                }
            }
        }
        return null
    }

    /** Row [i] is a bare label: the value is ": text" on the next row, or ":" then the value on the row after. */
    private fun valueOnFollowingRows(i: Int): String? {
        val next = rows.getOrNull(i + 1)?.joinToString(" ")?.trim() ?: return null
        if (!next.startsWith(":")) return null
        next.trimStart(':', ' ').trim().takeIf { it.isNotEmpty() }?.let { return it }
        val value = rows.getOrNull(i + 2)?.joinToString(" ")?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        // An empty field: the "value" row is really the next label (it is followed by its own ":").
        val after = rows.getOrNull(i + 3)?.joinToString(" ")?.trim()
        return if (after != null && after.startsWith(":")) null else value
    }

    /**
     * Text of the row right below the first row whose FIRST cell is [label]; for layouts with no ":"
     * separator (Grab: "Total Paid" then "Rp16.300"). Labels are tried in the order given.
     */
    fun below(vararg labels: String): String? {
        for (label in labels) {
            val want = norm(label)
            val i = rows.indexOfFirst { norm(it.first()) == want }
            if (i >= 0) rows.getOrNull(i + 1)?.joinToString(" ")?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return null
    }

    /**
     * Rows following a label-only row (card layouts like "Penerima" then name / number / bank on separate lines).
     * [stopAtPair] stops at the first label+value row so a card doesn't swallow the next table.
     */
    fun rowsAfter(label: String, max: Int, stopAtPair: Boolean = true): List<List<String>> {
        val want = norm(label)
        val i = rows.indexOfFirst { norm(it.first()) == want }
        if (i < 0) return emptyList()
        val after = rows.drop(i + 1)
        return (if (stopAtPair) after.takeWhile { it.size == 1 } else after).take(max)
    }

    fun lines(label: String, max: Int): List<String> =
        rowsAfter(label, max).map { it.joinToString(" ") }

    private fun split(row: List<String>): Pair<String, String>? {
        val cells = row.map { it.trimStart(':', ' ').trim() }.filter { it.isNotEmpty() }
        if (cells.size >= 2) return cells[0] to cells.drop(1).joinToString(" ")
        val single = cells.firstOrNull() ?: return null
        val idx = single.indexOf(':')                       // "Total Bayar : IDR 4,000.00" in one cell
        return if (idx in 1..40) single.substring(0, idx).trim() to single.substring(idx + 1).trim() else null
    }

    private fun norm(s: String) = s.lowercase().replace(Regex("[:\\s]+"), " ").trim()

    companion object {
        private const val CELL = "\u0001"

        fun fromHtml(html: String): EmailText {
            val marked = html
                .replace(Regex("(?is)<(style|script|head)\\b.*?</\\1>"), " ")
                .replace(Regex("(?i)</t[dh]\\s*>"), CELL)
                .replace(Regex("(?i)<br\\s*/?>|</(tr|p|div|li|h[1-6]|table)\\s*>"), "\n")
                .replace(Regex("<[^>]+>"), " ")
            return EmailText(toRows(decodeEntities(marked)))
        }

        fun fromPlain(text: String): EmailText =
            EmailText(text.lines()
                .map { line -> line.split(Regex("\t| {2,}")).map(::clean).filter { it.isNotEmpty() } }
                .filter { it.isNotEmpty() })

        private fun toRows(text: String): List<List<String>> =
            text.split('\n')
                .map { line -> line.split(CELL).map(::clean).filter { it.isNotEmpty() } }
                .filter { it.isNotEmpty() }

        private fun clean(s: String) = s.replace(' ', ' ').replace(Regex("\\s+"), " ").trim()

        private val ENTITY = Regex("&(#[xX][0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);")
        private val NAMED = mapOf(
            "nbsp" to " ", "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'",
            "rsquo" to "'", "lsquo" to "'", "ndash" to "-", "mdash" to "-",
        )

        private fun decodeEntities(s: String): String = ENTITY.replace(s) { m ->
            val e = m.groupValues[1]
            when {
                e.startsWith("#x") || e.startsWith("#X") -> e.substring(2).toIntOrNull(16)?.let { String(Character.toChars(it)) }
                e.startsWith("#") -> e.substring(1).toIntOrNull()?.let { String(Character.toChars(it)) }
                else -> NAMED[e.lowercase()]
            } ?: m.value
        }
    }
}