package com.nfar.allergydetective.domain.diary

/** One searchable ingredient: its display name in the current language plus synonyms. */
data class SearchEntry(
    val ingredientId: Long,
    val name: String,
    val synonyms: List<String>,
)

/**
 * Local ingredient search (TRD 3.2): normalised text, prefix before substring,
 * and small typos (Levenshtein distance) for longer queries.
 */
class IngredientSearch(entries: List<SearchEntry>) {

    private class Indexed(val id: Long, val name: String, val terms: List<String>, val synonyms: List<String>)

    private val index = entries.map { entry ->
        Indexed(entry.ingredientId, entry.name, (listOf(entry.name) + entry.synonyms).map(::normalizeForSearch), entry.synonyms)
    }

    private val byId = index.associateBy { it.id }

    /**
     * The synonym through which [id] matched [query], or null when its name matches too (user feedback
     * 21.11.2026: "Quark" appeared for "to" because of its other name "Topfen", without a visible reason).
     */
    fun matchedSynonym(id: Long, query: String): String? {
        val q = normalizeForSearch(query)
        val entry = byId[id] ?: return null
        if (q.isEmpty() || score(q, entry.terms.first()) != null) return null
        return entry.synonyms.firstOrNull { score(q, normalizeForSearch(it)) != null }
    }

    /** Ingredient ids, best match first. */
    fun search(query: String, limit: Int = MAX_RESULTS): List<Long> {
        val q = normalizeForSearch(query)
        if (q.isEmpty()) return emptyList()
        return index
            .mapNotNull { entry -> entry.terms.mapNotNull { score(q, it) }.minOrNull()?.let { entry to it } }
            .sortedWith(compareBy<Pair<Indexed, Int>> { it.second }.thenBy { it.first.name })
            .take(limit)
            .map { it.first.id }
    }

    /** Best ingredient for [query] and its score (0 = exact … 4 = small typo), or null. */
    fun bestMatch(query: String): Pair<Long, Int>? {
        val q = normalizeForSearch(query)
        if (q.isEmpty()) return null
        return index
            .mapNotNull { entry -> entry.terms.mapNotNull { score(q, it) }.minOrNull()?.let { entry to it } }
            .minWithOrNull(compareBy<Pair<Indexed, Int>> { it.second }.thenBy { it.first.name.length })
            ?.let { it.first.id to it.second }
    }

    /** Lower is better, null means no match. */
    private fun score(query: String, term: String): Int? {
        val words = term.split(' ')
        return when {
            term == query -> 0
            term.startsWith(query) -> 1
            words.any { it.startsWith(query) } -> 2
            term.contains(query) -> 3
            query.length >= MIN_FUZZY_LENGTH && words.any { isClose(query, it) } -> 4
            else -> null
        }
    }

    private fun isClose(query: String, word: String): Boolean {
        val maxDistance = if (query.length <= 5) 1 else 2
        if (kotlin.math.abs(query.length - word.length) > maxDistance) return false
        return levenshtein(query, word) <= maxDistance
    }

    private companion object {
        const val MAX_RESULTS = 20
        const val MIN_FUZZY_LENGTH = 4
    }
}

/** Lower case, umlauts spelled out, accents removed, everything else becomes a single space. */
fun normalizeForSearch(text: String): String {
    val builder = StringBuilder(text.length)
    for (char in text.lowercase()) {
        when (char) {
            'ä' -> builder.append("ae")
            'ö' -> builder.append("oe")
            'ü' -> builder.append("ue")
            'ß' -> builder.append("ss")
            in "àáâãå" -> builder.append('a')
            in "èéêë" -> builder.append('e')
            in "ìíîï" -> builder.append('i')
            in "òóôõø" -> builder.append('o')
            in "ùúû" -> builder.append('u')
            'ç' -> builder.append('c')
            'ñ' -> builder.append('n')
            else -> builder.append(if (char.isLetterOrDigit()) char else ' ')
        }
    }
    return builder.toString().split(' ').filter { it.isNotEmpty() }.joinToString(" ")
}

internal fun levenshtein(a: String, b: String): Int {
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
    return previous[b.length]
}
