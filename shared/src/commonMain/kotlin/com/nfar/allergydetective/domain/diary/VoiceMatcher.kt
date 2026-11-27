package com.nfar.allergydetective.domain.diary

/** What a spoken sentence was turned into (UX 3.2). */
data class VoiceMatch(
    val ingredientIds: List<Long>,
    val favoriteIds: List<Long>,
    /** Words that matched nothing, offered as "create new …". */
    val unmatched: List<String>,
)

/**
 * Local word matching for voice input (TRD 3.2): favorites → synonyms → ingredients,
 * tolerant to small typos and compound words ("Spinatpizza" → spinach + pizza).
 */
class VoiceMatcher(
    private val search: IngredientSearch,
    favorites: List<FavoriteInfo>,
    private val strictTypos: Boolean = false,
) {
    // Filler words are removed on both sides, so "Müsli mit Milch" matches the spoken "Müsli mit Milch".
    private val favoritesByName = favorites.associateBy { words(it.name).joinToString(" ") }

    private fun words(text: String): List<String> =
        normalizeForSearch(text).split(' ').filter { it.isNotEmpty() && it !in FILLER_WORDS }

    fun match(text: String): VoiceMatch {
        val words = words(text)
        val ingredients = mutableListOf<Long>()
        val favorites = mutableListOf<Long>()
        val unmatched = mutableListOf<String>()
        var i = 0
        while (i < words.size) {
            // Longest phrase first: "milchreis mit zimt" can be one favorite, "rote bete" one ingredient.
            val phrase = (minOf(MAX_PHRASE_WORDS, words.size - i) downTo 1).firstNotNullOfOrNull { length ->
                val phrase = words.subList(i, i + length).joinToString(" ")
                favoritesByName[phrase]?.let { PhraseMatch(length, favoriteId = it.id) }
                    ?: exactIngredient(phrase)?.let { PhraseMatch(length, ingredientId = it) }
                    // Speech recognition often splits compounds: "dinkel vollkornbrot" → "dinkelvollkornbrot".
                    ?: phrase.takeIf { length > 1 }?.let { exactIngredient(it.replace(" ", "")) }
                        ?.let { PhraseMatch(length, ingredientId = it) }
            }
            if (phrase != null) {
                phrase.favoriteId?.let(favorites::add)
                phrase.ingredientId?.let(ingredients::add)
                i += phrase.words
                continue
            }
            val word = words[i]
            val single = closeIngredient(word)
            val compound = if (single == null) splitCompound(word) else null
            when {
                single != null -> ingredients += single
                compound != null -> ingredients += compound
                else -> unmatched += word
            }
            i++
        }
        return VoiceMatch(ingredients.distinct(), favorites.distinct(), unmatched)
    }

    private class PhraseMatch(val words: Int, val favoriteId: Long? = null, val ingredientId: Long? = null)

    private fun exactIngredient(phrase: String): Long? = search.bestMatch(phrase)?.takeIf { it.second == 0 }?.first

    private fun closeIngredient(word: String): Long? =
        if (word.length < MIN_WORD_LENGTH) null else search.bestMatch(word)
            // A package is full of words that are one typo away from an ingredient ("Teige" -> "Feige"): a typo only counts in longer words.
            ?.takeIf { !strictTypos || reliable(it.second, word) }?.first

    /** A package has many short words: "aus" sits inside "Zuckeraustauschstoff" (Sorbit). Only prefixes count for them, a typo or a part only in longer words. */
    private fun reliable(score: Int, word: String): Boolean = when {
        score < SUBSTRING_SCORE -> true
        score == SUBSTRING_SCORE -> word.length >= MIN_SUBSTRING_WORD_LENGTH
        else -> word.length >= MIN_TYPO_WORD_LENGTH
    }

    /** Tries "spinatpizza" as "spinat" + "pizza", both parts must match well. */
    private fun splitCompound(word: String): List<Long>? {
        for (cut in MIN_WORD_LENGTH..word.length - MIN_WORD_LENGTH) {
            val first = search.bestMatch(word.substring(0, cut))?.takeIf { it.second <= GOOD_SCORE }
            val second = search.bestMatch(word.substring(cut))?.takeIf { it.second <= GOOD_SCORE }
            if (first != null && second != null) return listOf(first.first, second.first)
        }
        return null
    }

    private companion object {
        const val MAX_PHRASE_WORDS = 4
        const val MIN_WORD_LENGTH = 3
        const val SUBSTRING_SCORE = 3
        const val MIN_SUBSTRING_WORD_LENGTH = 6
        const val MIN_TYPO_WORD_LENGTH = 7

        /** Exact, prefix or word prefix; no substring or typo for compound parts. */
        const val GOOD_SCORE = 2

        /** Small words that join ingredients in a sentence (DE, EN, FR). */
        val FILLER_WORDS = setOf(
            "und", "mit", "ohne", "ein", "eine", "einen", "etwas", "dazu", "noch",
            "and", "with", "a", "an", "some", "of",
            "et", "avec", "du", "de", "la", "le", "des", "un", "une",
        )
    }
}
