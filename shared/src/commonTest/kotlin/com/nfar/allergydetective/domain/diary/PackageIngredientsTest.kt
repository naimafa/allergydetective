package com.nfar.allergydetective.domain.diary

import kotlin.test.Test
import kotlin.test.assertEquals

class PackageIngredientsTest {

    @Test
    fun takesOnlyTheIngredientListAndDropsPercentagesAndTheNutritionTable() {
        val text = "Müsli 500 g\nZutaten: Hafer-\nflocken (62 %), Rosinen 12,5%, Mandeln; Salz.\nKann Spuren von Erdnüssen enthalten.\nNährwerte pro 100 g"

        assertEquals("Haferflocken, Rosinen, Mandeln, Salz", PackageIngredients.clean(text))
    }

    @Test
    fun worksWithoutAMarkerAndInEnglish() {
        assertEquals("milk, sugar, cocoa", PackageIngredients.clean("Ingredients: milk, sugar (20%), cocoa. May contain nuts."))
        assertEquals("Milch, Zucker", PackageIngredients.clean("Milch, Zucker"))
    }
}
