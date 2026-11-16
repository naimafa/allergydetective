package com.nfar.allergydetective.data.seed

import kotlinx.serialization.Serializable

/*
 * JSON shape of the seed files in composeResources/files/seed (TRD 1.1, CONTENT_Zutaten.md, CONTENT_Symptome.md).
 * References between files use stable keys, not ids, so the files stay readable.
 */

@Serializable
data class CategorySeed(
    val id: Long,
    val key: String,
)

@Serializable
data class WindowSeed(
    val startMin: Int,
    val endMin: Int,
)

@Serializable
data class PropertySeed(
    val id: Long,
    val key: String,
    val group: String,
    val windows: List<WindowSeed>,
    val symptomGroups: List<String>,
)

/** [synonyms] maps a locale ("de") to search and voice synonyms. */
@Serializable
data class IngredientSeed(
    val id: Long,
    val key: String,
    val category: String,
    val properties: List<String> = emptyList(),
    val synonyms: Map<String, List<String>> = emptyMap(),
)

@Serializable
data class SymptomSeed(
    val id: Long,
    val key: String,
    val group: String,
    val isRedFlag: Boolean = false,
)

@Serializable
data class LifestyleFactorSeed(
    val id: Long,
    val key: String,
)

data class SeedData(
    val categories: List<CategorySeed>,
    val properties: List<PropertySeed>,
    val ingredients: List<IngredientSeed>,
    val symptoms: List<SymptomSeed>,
    val lifestyleFactors: List<LifestyleFactorSeed>,
)
