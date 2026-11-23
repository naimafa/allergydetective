package com.nfar.allergydetective.domain.diary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IngredientSearchTest {

    private companion object {
        const val WHITE_BREAD = 101L
        const val WHOLEMEAL_BREAD = 102L
        const val PASTA = 105L
        const val COW_MILK = 201L
        const val OAT_MILK = 203L
        const val CAFE = 1101L
    }

    private val search = IngredientSearch(
        listOf(
            SearchEntry(WHITE_BREAD, "Weißbrot", listOf("Brötchen", "Semmel", "Toast")),
            SearchEntry(WHOLEMEAL_BREAD, "Vollkornbrot", emptyList()),
            SearchEntry(PASTA, "Nudeln (Weizen)", listOf("Pasta", "Spaghetti")),
            SearchEntry(COW_MILK, "Kuhmilch", emptyList()),
            SearchEntry(OAT_MILK, "Hafermilch", emptyList()),
            SearchEntry(CAFE, "Café", emptyList()),
        ),
    )

    @Test
    fun blankQueryFindsNothing() {
        assertTrue(search.search("  ").isEmpty())
    }

    @Test
    fun synonymFindsTheIngredient() {
        assertEquals(listOf(WHITE_BREAD), search.search("semmel"))
    }

    @Test
    fun compoundWordsAreFoundByTheirParts() {
        assertEquals(setOf(WHITE_BREAD, WHOLEMEAL_BREAD), search.search("Brot").toSet())
        assertEquals(setOf(COW_MILK, OAT_MILK), search.search("milch").toSet())
    }

    @Test
    fun startOfNameRanksBeforeMatchInsideTheName() {
        val entries = listOf(SearchEntry(1, "Hafermilch", emptyList()), SearchEntry(2, "Milchreis", emptyList()))

        assertEquals(listOf(2L, 1L), IngredientSearch(entries).search("milch"))
    }

    @Test
    fun umlautsAndAccentsAreNormalised() {
        assertEquals(listOf(WHITE_BREAD), search.search("weissbrot"))
        assertEquals(listOf(WHITE_BREAD), search.search("Brotchen"))
        assertEquals(listOf(CAFE), search.search("cafe"))
    }

    @Test
    fun smallTyposAreTolerated() {
        assertEquals(listOf(PASTA), search.search("Spagetti"))
        assertEquals(listOf(COW_MILK), search.search("Kuhmilsh"))
    }

    @Test
    fun shortQueriesAreNotFuzzy() {
        assertTrue(search.search("xyz").isEmpty())
    }

    @Test
    fun wordInsideTheNameMatches() {
        assertEquals(listOf(PASTA), search.search("weizen"))
    }

    @Test
    fun synonymIsReportedOnlyWhenTheNameDoesNotMatch() {
        assertEquals("Semmel", search.matchedSynonym(WHITE_BREAD, "sem"))
        assertEquals(null, search.matchedSynonym(WHITE_BREAD, "weiss"))
        assertEquals(null, search.matchedSynonym(WHOLEMEAL_BREAD, "sem"))
    }
}
