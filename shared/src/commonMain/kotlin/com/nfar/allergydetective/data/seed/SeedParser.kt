package com.nfar.allergydetective.data.seed

import kotlinx.serialization.json.Json

/** Raw content of the seed files, one string per file. */
data class RawSeed(
    val categories: String,
    val properties: String,
    val ingredients: String,
    val symptoms: String,
    val lifestyleFactors: String,
)

object SeedParser {
    // Strict on purpose: a typo in a field name should fail the tests, not be ignored.
    private val json = Json { ignoreUnknownKeys = false }

    fun parse(raw: RawSeed): SeedData = SeedData(
        categories = json.decodeFromString(raw.categories),
        properties = json.decodeFromString(raw.properties),
        ingredients = json.decodeFromString(raw.ingredients),
        symptoms = json.decodeFromString(raw.symptoms),
        lifestyleFactors = json.decodeFromString(raw.lifestyleFactors),
    )
}
