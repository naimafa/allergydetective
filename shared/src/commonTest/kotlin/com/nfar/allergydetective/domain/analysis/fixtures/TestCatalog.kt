package com.nfar.allergydetective.domain.analysis.fixtures

import com.nfar.allergydetective.domain.analysis.model.IngredientRef
import com.nfar.allergydetective.domain.analysis.model.ReferenceData
import com.nfar.allergydetective.domain.analysis.model.SymptomRef
import com.nfar.allergydetective.domain.analysis.model.TimeWindow
import com.nfar.allergydetective.domain.analysis.model.TriggerProperty

/**
 * Small synthetic reference catalog for engine tests.
 * IDs and windows follow the seed data (CONTENT_Zutaten.md), but only the entries the tests need.
 */
object TestCatalog {
    // Trigger properties
    const val GLUTEN = 102L
    const val LACTOSE = 103L
    const val FRUCTANS = 107L
    const val MILK_PROTEIN = 117L

    // Ingredients
    const val MILK = 1L // lactose + milk protein
    const val BREAD = 2L // gluten + fructans
    const val RICE = 3L // no trigger
    const val HARD_CHEESE = 4L // milk protein only

    // Symptoms
    const val BLOATING = 1002L
    const val DIARRHEA = 1005L
    const val HIVES = 2003L
    const val HEADACHE = 3001L

    const val DIGESTIVE = "digestive"
    const val SKIN = "skin"
    const val HEAD = "head"
    const val AIRWAYS = "airways"

    fun reference(knownAllergyPropertyIds: Set<Long> = emptySet()) = ReferenceData(
        properties = listOf(
            TriggerProperty(GLUTEN, listOf(TimeWindow(60, 2880)), setOf(DIGESTIVE, SKIN, HEAD)),
            TriggerProperty(LACTOSE, listOf(TimeWindow(15, 360)), setOf(DIGESTIVE)),
            TriggerProperty(FRUCTANS, listOf(TimeWindow(60, 720)), setOf(DIGESTIVE)),
            TriggerProperty(MILK_PROTEIN, listOf(TimeWindow(0, 120)), setOf(DIGESTIVE, SKIN, AIRWAYS)),
        ),
        ingredients = listOf(
            IngredientRef(MILK, setOf(LACTOSE, MILK_PROTEIN)),
            IngredientRef(BREAD, setOf(GLUTEN, FRUCTANS)),
            IngredientRef(RICE, emptySet()),
            IngredientRef(HARD_CHEESE, setOf(MILK_PROTEIN)),
        ),
        symptoms = listOf(
            SymptomRef(BLOATING, DIGESTIVE),
            SymptomRef(DIARRHEA, DIGESTIVE),
            SymptomRef(HIVES, SKIN),
            SymptomRef(HEADACHE, HEAD),
        ),
        knownAllergyPropertyIds = knownAllergyPropertyIds,
    )
}
