package com.nfar.allergydetective.domain.export

import com.nfar.allergydetective.domain.analysis.CandidateKeys
import com.nfar.allergydetective.domain.analysis.model.AnalysisInput
import com.nfar.allergydetective.domain.analysis.model.AnalysisResult
import com.nfar.allergydetective.domain.analysis.model.Candidate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/*
 * Export of the diary and the analysis numbers for the self-test (ROADMAP Phase 4):
 * with it the thresholds (episodes 3h, grouping 80 %, lift limits) can be calibrated.
 * Only stable keys, no display names and no free-text notes.
 */

@Serializable
data class ExportData(
    val version: Int = 1,
    val generatedAt: Long,
    val days: List<ExportDay>,
    val meals: List<ExportMeal>,
    val symptoms: List<ExportSymptom>,
    val ranking: List<ExportCandidate>,
    val groups: List<List<String>>,
    val safeIngredients: List<String>,
)

@Serializable
data class ExportDay(
    val epochDay: Long,
    val confirmed: Boolean,
    val sleepHours: Float? = null,
    val sleepQuality: Int? = null,
    val stress: Int? = null,
    val factors: List<String> = emptyList(),
)

@Serializable
data class ExportMeal(val eatenAt: Long, val tzOffsetMin: Int, val items: List<ExportItem>)

@Serializable
data class ExportItem(val ingredient: String, val portion: String)

@Serializable
data class ExportSymptom(val symptom: String, val startedAt: Long, val tzOffsetMin: Int, val intensity: Int)

@Serializable
data class ExportCandidate(
    val candidate: String,
    val level: String,
    val lift: Double,
    val n1: Double,
    val k1: Double,
    val n0: Double,
    val k0: Double,
    val exposures: Int,
    val hits: Int,
    val unexposed: Int,
    val unexposedHits: Int,
)

/** Builds the export from the analysis input and result. Pure, no database access. */
fun buildExport(input: AnalysisInput, result: AnalysisResult, keys: CandidateKeys): ExportData {
    fun ingredient(id: Long) = keys.ingredients[id]?.let { it.key ?: "custom:${it.customName}" } ?: "ingredient:$id"
    fun candidate(c: Candidate): String = when (c) {
        is Candidate.Ingredient -> "ingredient:" + ingredient(c.id)
        is Candidate.Property -> "property:" + (keys.properties[c.id] ?: c.id.toString())
        is Candidate.DayFactor -> "factor:" + (keys.factors[c.id] ?: c.id.toString())
        Candidate.BadSleep -> "lifestyle:bad_sleep"
        Candidate.HighStress -> "lifestyle:high_stress"
    }
    val factorsByDay = input.diary.dayFactors.groupBy({ it.epochDay }, { keys.factors[it.factorId] ?: it.factorId.toString() })
    return ExportData(
        generatedAt = input.now,
        days = input.diary.dayChecks.sortedBy { it.epochDay }.map {
            ExportDay(it.epochDay, it.confirmed, it.sleepHours, it.sleepQuality, it.stress, factorsByDay[it.epochDay].orEmpty())
        },
        meals = input.diary.meals.sortedBy { it.eatenAt }.map { meal ->
            ExportMeal(meal.eatenAt, meal.tzOffsetMin, meal.items.map { ExportItem(ingredient(it.ingredientId), it.portion.name) })
        },
        symptoms = input.diary.symptomEntries.sortedBy { it.startedAt }.map {
            ExportSymptom(keys.symptoms[it.symptomId] ?: it.symptomId.toString(), it.startedAt, it.tzOffsetMin, it.intensity)
        },
        ranking = result.ranking.map { r ->
            ExportCandidate(
                candidate = candidate(r.candidate),
                level = r.level.name,
                lift = r.lift,
                n1 = r.stats.n1,
                k1 = r.stats.k1,
                n0 = r.stats.n0,
                k0 = r.stats.k0,
                exposures = r.stats.exposureCount,
                hits = r.stats.hitCount,
                unexposed = r.stats.unexposedCount,
                unexposedHits = r.stats.unexposedHitCount,
            )
        },
        groups = result.groups.map { group -> group.members.map(::candidate) },
        safeIngredients = result.safeIngredientIds.map(::ingredient),
    )
}

private val exportJson = Json { prettyPrint = true; encodeDefaults = true }

fun ExportData.toJson(): String = exportJson.encodeToString(ExportData.serializer(), this)
