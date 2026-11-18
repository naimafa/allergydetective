package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.IngredientRef
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.analysis.model.ReferenceData
import com.nfar.allergydetective.domain.analysis.model.TimeWindow
import com.nfar.allergydetective.domain.analysis.model.TriggerProperty
import kotlin.test.Test
import kotlin.test.assertEquals

/** Self-test 18.11.: cooking destroys the birch pollen cross-reaction, other triggers stay. */
class PreparationTest {

    private val birch = TriggerProperty(118, listOf(TimeWindow(0, 60)), setOf("skin"), heatLabile = true)
    private val celeryAllergen = TriggerProperty(114, listOf(TimeWindow(0, 120)), setOf("skin"))
    private val catalog = FoodCatalog(
        ReferenceData(
            properties = listOf(birch, celeryAllergen),
            ingredients = listOf(IngredientRef(711, setOf(118)), IngredientRef(712, setOf(114, 118))),
            symptoms = emptyList(),
        ),
    )

    @Test
    fun cookedCarrotLosesTheBirchTrigger() {
        assertEquals(setOf(118L), catalog.propertiesOf(MealItem(711, Portion.MEDIUM, Preparation.RAW)))
        assertEquals(emptySet(), catalog.propertiesOf(MealItem(711, Portion.MEDIUM, Preparation.COOKED)))
    }

    @Test
    fun cookedCeleryKeepsItsHeatStableAllergen() {
        assertEquals(setOf(114L), catalog.propertiesOf(MealItem(712, Portion.MEDIUM, Preparation.COOKED)))
    }

    @Test
    fun withoutPreparationEverythingCounts() {
        assertEquals(setOf(114L, 118L), catalog.propertiesOf(MealItem(712, Portion.MEDIUM)))
    }
}
