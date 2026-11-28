package com.nfar.allergydetective.domain.diary

/** What the list of a package turned into: ingredients of the catalog, and entries the catalog does not know. */
data class PackageMatch(
    val ingredientIds: List<Long>,
    /** Entries that matched nothing as written, offered as new ingredients (user 27.11.2026). */
    val unknown: List<String>,
    /** Entries that are almost a catalog name (a misread letter), only suggested, never taken (user 28.11.2026). */
    val suggestions: List<PackageSuggestion> = emptyList(),
)

/** [written] is the entry as read ("Weizenmeh1"), [ingredientId] the catalog ingredient it probably means. */
data class PackageSuggestion(val written: String, val ingredientId: Long)

/**
 * Matches the ingredient list of a package (cleaned by [PackageIngredients]) only as written: the name of an
 * ingredient, no synonyms, no typos, no parts of words. A guess put wrong ingredients into the meal (user 27.11.2026:
 * "so erkennen wie sie sind"); what the catalog does not know is offered as a new ingredient instead.
 */
class PackageWordMatcher(names: Map<Long, String>) {

    private val byName: Map<String, Long> = buildMap {
        for ((id, name) in names) {
            for (variant in variants(name)) putIfAbsent(normalizeForSearch(variant), id)
        }
    }

    /** "Backpulver / Natron" is also "Backpulver"; "Vollkornbrot (Weizen)" is also "Vollkornbrot". */
    private fun variants(name: String): List<String> {
        val withoutExamples = name.substringBefore(" (").trim()
        return (listOf(name, withoutExamples) + withoutExamples.split(" / ")).filter { it.isNotBlank() }.distinct()
    }

    fun match(cleaned: String): PackageMatch {
        val found = mutableListOf<Long>()
        val unknown = mutableListOf<String>()
        val suggestions = mutableListOf<PackageSuggestion>()
        for (entry in cleaned.split(',').map { it.trim() }.filter { it.isNotEmpty() }) {
            val words = normalizeForSearch(entry).split(' ').filter { it.isNotEmpty() }
            val ids = mutableListOf<Long>()
            var i = 0
            while (i < words.size) {
                // Longest phrase first: "rote bete" is one ingredient.
                val phrase = (minOf(MAX_PHRASE_WORDS, words.size - i) downTo 1).firstNotNullOfOrNull { length ->
                    byName[words.subList(i, i + length).joinToString(" ")]?.let { length to it }
                }
                if (phrase != null) {
                    ids += phrase.second
                    i += phrase.first
                } else {
                    i++
                }
            }
            if (ids.isEmpty()) {
                if (entry.length >= MIN_NEW_NAME_LENGTH) unknown += display(entry)
                suggest(entry)?.let { suggestions += PackageSuggestion(display(entry), it) }
            } else {
                found += ids
            }
        }
        return PackageMatch(found.distinct(), unknown.distinct(), suggestions.distinctBy { it.ingredientId }.filter { it.ingredientId !in found })
    }

    /**
     * An entry that is almost an ingredient name once the usual misreadings are undone (a digit for a letter, "rn" for
     * "m") and with at most one letter wrong: a suggestion only, the user decides. Not for short words and not when two
     * ingredients are equally close.
     */
    private fun suggest(entry: String): Long? {
        val written = unconfuse(normalizeForSearch(entry))
        if (written.length < MIN_SUGGEST_LENGTH) return null
        var best: Long? = null
        var bestDistance = 2
        var tie = false
        for ((name, id) in byName) {
            if (kotlin.math.abs(name.length - written.length) > 1) continue
            val distance = editDistance(written, unconfuse(name), limit = 1)
            if (distance > 1) continue
            if (distance < bestDistance) {
                best = id
                bestDistance = distance
                tie = false
            } else if (distance == bestDistance && id != best) {
                tie = true
            }
        }
        return best.takeIf { !tie }
    }

    /** Letters that a camera confuses with digits and letter pairs, put on one side: "weizenmeh1" -> "weizenmehl". */
    private fun unconfuse(text: String): String =
        text.replace("rn", "m").replace("vv", "w")
            .map { char -> when (char) { '0' -> 'o'; '1' -> 'l'; '5' -> 's'; '8' -> 'b'; '6' -> 'g'; else -> char } }
            .joinToString("")

    /** Number of single-letter changes between [a] and [b]; anything above [limit] comes back as limit + 1. */
    private fun editDistance(a: String, b: String, limit: Int): Int {
        if (kotlin.math.abs(a.length - b.length) > limit) return limit + 1
        var previous = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
            }
            previous = current
        }
        return previous[b.length].coerceAtMost(limit + 1)
    }

    /** "MAGERMILCHPULVER" -> "Magermilchpulver"; a name written normally stays as it is. */
    private fun display(entry: String): String =
        if (entry.any { it.isLowerCase() }) entry else entry.lowercase().replaceFirstChar { it.uppercase() }

    private companion object {
        const val MAX_PHRASE_WORDS = 4
        const val MIN_NEW_NAME_LENGTH = 3
        const val MIN_SUGGEST_LENGTH = 6
    }
}
