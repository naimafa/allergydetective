package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DishSearchTest {

    private val names = mapOf(1L to "Linsen", 2L to "Möhre", 3L to "Olivenöl")
    private val soup = FavoriteInfo(7, "Linsensuppe", listOf(MealItem(1, Portion.MEDIUM), MealItem(2, Portion.SMALL)))

    @Test
    fun dishIsFoundByNameOrIngredient() {
        assertTrue(soup.matches("suppe", names))
        assertTrue(soup.matches("MOEHRE", names))
        assertTrue(soup.matches("möh", names))
        assertFalse(soup.matches("olivenöl", names))
    }

    @Test
    fun blankQueryMatchesEveryDish() {
        assertTrue(soup.matches("  ", names))
    }
}
