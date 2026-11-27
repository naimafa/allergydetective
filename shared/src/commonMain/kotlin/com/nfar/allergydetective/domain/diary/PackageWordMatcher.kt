package com.nfar.allergydetective.domain.diary

/** What the list of a package turned into: ingredients of the catalog, and entries the catalog does not know. */
data class PackageMatch(
    val ingredientIds: List<Long>,
    /** Entries that matched nothing as written, offered as new ingredients (user 27.11.2026). */
    val unknown: List<String>,
)

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
            } else {
                found += ids
            }
        }
        return PackageMatch(found.distinct(), unknown.distinct())
    }

    /** "MAGERMILCHPULVER" -> "Magermilchpulver"; a name written normally stays as it is. */
    private fun display(entry: String): String =
        if (entry.any { it.isLowerCase() }) entry else entry.lowercase().replaceFirstChar { it.uppercase() }

    private companion object {
        const val MAX_PHRASE_WORDS = 4
        const val MIN_NEW_NAME_LENGTH = 3
    }
}
