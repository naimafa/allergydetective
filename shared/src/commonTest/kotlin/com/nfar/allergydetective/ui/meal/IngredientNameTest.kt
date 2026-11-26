package com.nfar.allergydetective.ui.meal

import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientNameTest {
    @Test
    fun examplesAreSplitOff() {
        assertEquals("Schnittkäse jung" to "junger Gouda, Edamer", splitExamples("Schnittkäse jung (junger Gouda, Edamer)"))
        assertEquals("Süßstoff" to "Sucralose, Aspartam …", splitExamples("Süßstoff (Sucralose, Aspartam …)"))
    }

    @Test
    fun aBracketThatTellsIngredientsApartStays() {
        assertEquals("Nudeln (Weizen)" to null, splitExamples("Nudeln (Weizen)"))
        assertEquals("Koriander (frisch)" to null, splitExamples("Koriander (frisch)"))
        assertEquals("Quark" to null, splitExamples("Quark"))
    }
}
