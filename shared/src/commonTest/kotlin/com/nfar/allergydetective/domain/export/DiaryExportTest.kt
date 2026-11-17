package com.nfar.allergydetective.domain.export

import com.nfar.allergydetective.domain.analysis.AnalysisEngine
import com.nfar.allergydetective.domain.analysis.CandidateKeys
import com.nfar.allergydetective.domain.analysis.fixtures.DiaryBuilder
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BLOATING
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BREAD
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.LACTOSE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK_PROTEIN
import com.nfar.allergydetective.domain.diary.IngredientInfo
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiaryExportTest {

    private val keys = CandidateKeys(
        ingredients = mapOf(MILK to IngredientInfo(MILK, "cow_milk", null), BREAD to IngredientInfo(BREAD, null, "Omas Brot")),
        properties = mapOf(LACTOSE to "lactose", MILK_PROTEIN to "milk_protein"),
        factors = emptyMap(),
        symptoms = mapOf(BLOATING to "bloating"),
    )

    private val input = DiaryBuilder().apply {
        days(21) { i ->
            meal(8, BREAD)
            if (i % 3 == 0) {
                meal(15, MILK)
                symptom(16, 30, BLOATING, intensity = 4)
            }
        }
    }.build(reference = TestCatalog.reference())

    private val export = buildExport(input, AnalysisEngine().analyze(input), keys)

    @Test
    fun diaryIsExportedWithStableKeys() {
        assertEquals(21, export.days.size)
        assertEquals(28, export.meals.size)
        assertEquals("custom:Omas Brot", export.meals.first().items.single().ingredient)
        assertEquals(listOf("bloating"), export.symptoms.map { it.symptom }.distinct())
    }

    @Test
    fun analysisNumbersAreIncluded() {
        val lactose = export.ranking.single { it.candidate == "property:lactose" }

        // 21 days: suspicion; "strong" needs the suspicion to be stable for 14 days (TRD 2.8)
        assertEquals("SUSPICION", lactose.level)
        assertEquals(7, lactose.exposures)
        assertEquals(7, lactose.hits)
        assertTrue(lactose.lift >= 2.0)
        assertTrue(export.groups.single().contains("ingredient:cow_milk"))
    }

    @Test
    fun jsonCanBeReadBack() {
        val parsed = Json.decodeFromString(ExportData.serializer(), export.toJson())

        assertEquals(export, parsed)
    }
}
