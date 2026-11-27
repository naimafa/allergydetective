package com.nfar.allergydetective.domain.diary

import kotlin.test.Test
import kotlin.test.assertEquals

class PackageWordMatcherTest {

    private val matcher = PackageWordMatcher(
        mapOf(1L to "Dinkelmehl", 2L to "Backpulver / Natron", 3L to "Blattsalat", 4L to "Zucker", 5L to "Vanille", 6L to "Rote Bete"),
    )

    @Test
    fun onlyNamesAsWrittenMatchNoPartsOfWordsAndNoTypos() {
        // "Salat" is part of "Blattsalat", "Dinkelmehll" a typo: neither counts.
        val match = matcher.match("Dinkelmehl, Salat, Dinkelmehll, Zucker")

        assertEquals(listOf(1L, 4L), match.ingredientIds)
        assertEquals(listOf("Salat", "Dinkelmehll"), match.unknown)
    }

    @Test
    fun partOfANameWithSlashAndPhrasesAndHyphensMatch() {
        val match = matcher.match("Backpulver, Bourbon-Vanille, rote Bete")

        assertEquals(listOf(2L, 5L, 6L), match.ingredientIds)
        assertEquals(emptyList(), match.unknown)
    }

    @Test
    fun unknownNamesAreOfferedAsNewIngredients() {
        val match = matcher.match("MAGERMILCHPULVER, Meersalz, Ei")

        assertEquals(emptyList(), match.ingredientIds)
        // "Ei" is too short to be offered.
        assertEquals(listOf("Magermilchpulver", "Meersalz"), match.unknown)
    }
}
