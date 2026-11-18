package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import kotlin.test.Test
import kotlin.test.assertEquals

class VoiceMatcherTest {

    private companion object {
        const val SPINACH = 1L
        const val PIZZA = 2L
        const val COFFEE = 3L
        const val ROLL = 4L
        const val BUTTER = 5L
        const val BEETROOT = 6L
        const val SPELT_BREAD = 7L
        const val WHOLEMEAL_WHEAT = 8L
        const val SPELT_WHOLEMEAL = 9L
    }

    private val search = IngredientSearch(
        listOf(
            SearchEntry(SPINACH, "Spinat", emptyList()),
            SearchEntry(PIZZA, "Pizza", emptyList()),
            SearchEntry(COFFEE, "Kaffee", emptyList()),
            SearchEntry(ROLL, "Weißbrot", listOf("Semmel", "Brötchen")),
            SearchEntry(BUTTER, "Butter", emptyList()),
            SearchEntry(BEETROOT, "Rote Bete", emptyList()),
            SearchEntry(SPELT_BREAD, "Dinkelbrot", emptyList()),
            SearchEntry(WHOLEMEAL_WHEAT, "Vollkornbrot (Weizen)", listOf("Vollkornbrot")),
            SearchEntry(SPELT_WHOLEMEAL, "Dinkelvollkornbrot", emptyList()),
        ),
    )
    private val muesli = FavoriteInfo(10, "Müsli mit Milch", listOf(MealItem(BUTTER, Portion.MEDIUM)))
    private val matcher = VoiceMatcher(search, listOf(muesli))

    @Test
    fun sentenceWithFillerWordsIsSplitIntoIngredients() {
        assertEquals(VoiceMatch(listOf(SPINACH, PIZZA, COFFEE), emptyList(), emptyList()), matcher.match("Spinat Pizza und Kaffee"))
    }

    @Test
    fun compoundWordIsSplit() {
        assertEquals(listOf(SPINACH, PIZZA), matcher.match("Spinatpizza").ingredientIds)
    }

    @Test
    fun synonymsAndSmallTyposAreFound() {
        assertEquals(listOf(ROLL, BUTTER), matcher.match("Semmel mit Buter").ingredientIds)
    }

    @Test
    fun splitCompoundIsOneIngredient() {
        // Self-test 17.11.: "Dinkel Vollkornbrot" became spelt bread + wheat wholemeal bread.
        assertEquals(VoiceMatch(listOf(SPELT_WHOLEMEAL), emptyList(), emptyList()), matcher.match("Dinkel Vollkornbrot"))
    }

    @Test
    fun multiWordIngredientIsOneMatch() {
        assertEquals(listOf(BEETROOT), matcher.match("rote Bete").ingredientIds)
    }

    @Test
    fun favoriteNameWinsOverSingleWords() {
        val result = matcher.match("Müsli mit Milch und Kaffee")

        assertEquals(listOf(10L), result.favoriteIds)
        assertEquals(listOf(COFFEE), result.ingredientIds)
    }

    @Test
    fun unknownWordsAreReported() {
        assertEquals(listOf("xylophon"), matcher.match("Kaffee Xylophon").unmatched)
    }
}
