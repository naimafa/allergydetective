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

    // User 28.11.2026: a misread letter is only suggested, never taken.
    @Test
    fun aMisreadLetterIsSuggestedAndNotTaken() {
        val match = matcher.match("Dinkelmeh1, Zucker, B1attsalat, Vani11e")

        assertEquals(listOf(4L), match.ingredientIds)
        assertEquals(
            listOf(PackageSuggestion("Dinkelmeh1", 1L), PackageSuggestion("B1attsalat", 3L), PackageSuggestion("Vani11e", 5L)),
            match.suggestions,
        )
    }

    @Test
    fun aSingleWrongLetterIsSuggestedButShortOrFarWordsAreNot() {
        val match = matcher.match("Dinkelmehll, Zuker, Sa1z, Dinkelkuchen")

        // "Dinkelmehll" and "Zuker" are one letter off; "Sa1z" is too short; "Dinkelkuchen" is far from any name.
        assertEquals(listOf(PackageSuggestion("Dinkelmehll", 1L), PackageSuggestion("Zuker", 4L)), match.suggestions)
        assertEquals(emptyList(), match.ingredientIds)
    }

    @Test
    fun twoEquallyCloseIngredientsGiveNoSuggestion() {
        val twins = PackageWordMatcher(mapOf(1L to "Haselnuss", 2L to "Hasalnuss"))

        assertEquals(emptyList(), twins.match("Hasolnuss").suggestions)
    }

    @Test
    fun aLostUmlautDotIsSuggested() {
        // Phone test 28.11.2026: "Stärke" was read as "Strke".
        val withStarch = PackageWordMatcher(mapOf(1L to "Stärke", 2L to "Zucker"))

        assertEquals(listOf(PackageSuggestion("Strke", 1L)), withStarch.match("Strke, Zucker").suggestions)
    }
}
