package com.nfar.allergydetective.data.seed

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks the real seed files that ship with the app (JVM only, reads them from the source tree).
 * Counts follow docs/CONTENT_Zutaten.md v0.1 and docs/CONTENT_Symptome.md v1.1.
 */
class SeedFilesTest {

    private val resourcesDir: File = listOf("src/commonMain/composeResources", "shared/src/commonMain/composeResources")
        .map(::File)
        .first { it.isDirectory }

    private fun seedFile(name: String) = File(resourcesDir, "files/seed/$name").readText()

    private val entities = SeedParser.parse(
        RawSeed(
            categories = seedFile("categories.json"),
            properties = seedFile("properties.json"),
            ingredients = seedFile("ingredients.json"),
            symptoms = seedFile("symptoms.json"),
            lifestyleFactors = seedFile("lifestyle_factors.json"),
        ),
    ).toEntities()

    @Test
    fun hasExpectedCounts() {
        assertEquals(12, entities.categories.size)
        assertEquals(26, entities.properties.size)
        assertEquals(113, entities.ingredients.size)
        assertEquals(24, entities.symptoms.size)
        assertEquals(3, entities.lifestyleFactors.size)
    }

    @Test
    fun onlySwellingAndShortnessOfBreathAreRedFlags() {
        assertEquals(setOf("swelling", "shortness_of_breath"), entities.symptoms.filter { it.isRedFlag }.map { it.key }.toSet())
    }

    @Test
    fun spinachIsHistamine() {
        val spinach = entities.ingredients.single { it.key == "spinach" }
        val histamine = entities.properties.single { it.key == "histamine" }

        assertTrue(entities.ingredientProperties.any { it.ingredientId == spinach.id && it.propertyId == histamine.id })
    }

    @Test
    fun everyKeyHasDisplayNameInEnglishAndGerman() {
        val expected = entities.categories.map { "category_${it.key}" } +
            entities.properties.map { "property_${it.key}" } +
            entities.symptoms.map { "symptom_group_${it.groupKey}" }.distinct() +
            entities.symptoms.map { "symptom_${it.key}" } +
            entities.ingredients.map { "ingredient_${it.key}" } +
            entities.lifestyleFactors.map { "factor_${it.key}" }

        listOf("values", "values-de").forEach { folder ->
            val xml = File(resourcesDir, "$folder/seed_strings.xml").readText()
            val names = Regex("""<string name="([a-z0-9_]+)">""").findAll(xml).map { it.groupValues[1] }.toSet()
            val missing = expected.filterNot { it in names }
            assertTrue(missing.isEmpty(), "Missing in $folder: $missing")
        }
    }
}
