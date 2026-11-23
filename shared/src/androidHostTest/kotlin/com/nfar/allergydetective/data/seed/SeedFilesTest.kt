package com.nfar.allergydetective.data.seed

import com.nfar.allergydetective.data.db.Migrations
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
        assertEquals(15, entities.categories.size)
        assertEquals(26, entities.properties.size)
        assertEquals(150, entities.ingredients.size)
        assertEquals(24, entities.symptoms.size)
        assertEquals(4, entities.lifestyleFactors.size)
    }

    /** Existing installs get the oils by migration, new ones by JSON: both must be the same. */
    @Test
    fun oilsMigrationMatchesSeedFile() {
        val propertyKeys = entities.properties.associate { it.id to it.key }
        Migrations.OILS_FATS_ROWS.forEach { row ->
            val seed = entities.ingredients.single { it.id == row.id }
            assertEquals(row.key, seed.key)
            assertEquals(row.categoryId, seed.categoryId)
            assertEquals(
                row.properties.toSet(),
                entities.ingredientProperties.filter { it.ingredientId == row.id }.map { propertyKeys.getValue(it.propertyId) }.toSet(),
            )
            assertEquals(row.synonymsDe, entities.ingredientSynonyms.filter { it.ingredientId == row.id }.map { it.synonym })
        }
        assertEquals(13L, entities.categories.single { it.key == "oils_fats" }.id)
    }

    /** Spices (user note 21.11.2026): migration 9 -> 10 for existing installs, JSON for new ones; both the same. */
    @Test
    fun spicesMigrationMatchesSeedFile() {
        val propertyKeys = entities.properties.associate { it.id to it.key }
        Migrations.SPICES_ROWS.filter { it.id != Migrations.DRIED_HERBS }.forEach { row ->  // dried herbs moved in 11 -> 12
            val seed = entities.ingredients.single { it.id == row.id }
            assertEquals(row.key, seed.key)
            assertEquals(row.categoryId, seed.categoryId)
            assertEquals(
                row.properties.toSet(),
                entities.ingredientProperties.filter { it.ingredientId == row.id }.map { propertyKeys.getValue(it.propertyId) }.toSet(),
            )
            assertEquals(row.synonymsDe, entities.ingredientSynonyms.filter { it.ingredientId == row.id }.map { it.synonym })
        }
        assertEquals(14L, entities.categories.single { it.key == "spices_herbs" }.id)
        assertEquals(14, entities.ingredients.count { it.categoryId == 14L })
    }


    /** Herbs (user 23.11.2026): migration 11 -> 12 for existing installs, JSON for new ones; both the same. */
    @Test
    fun herbsMigrationMatchesSeedFile() {
        val propertyKeys = entities.properties.associate { it.id to it.key }
        Migrations.HERBS_ROWS.forEach { row ->
            val seed = entities.ingredients.single { it.id == row.id }
            assertEquals(row.key, seed.key)
            assertEquals(row.categoryId, seed.categoryId)
            assertEquals(
                row.properties.toSet(),
                entities.ingredientProperties.filter { it.ingredientId == row.id }.map { propertyKeys.getValue(it.propertyId) }.toSet(),
            )
            assertEquals(row.synonymsDe, entities.ingredientSynonyms.filter { it.ingredientId == row.id }.map { it.synonym })
        }
        assertEquals(15L, entities.categories.single { it.key == "herbs" }.id)
        assertEquals(15L, entities.ingredients.single { it.id == Migrations.DRIED_HERBS }.categoryId)
        assertEquals(
            listOf("Kräuter der Provence", "italienische Kräuter"),
            entities.ingredientSynonyms.filter { it.ingredientId == Migrations.DRIED_HERBS }.map { it.synonym },
        )
        assertEquals(14, entities.ingredients.count { it.categoryId == 15L })
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
