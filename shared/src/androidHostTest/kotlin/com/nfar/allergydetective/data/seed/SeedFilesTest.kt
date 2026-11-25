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
        assertEquals(250, entities.ingredients.size)
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
        assertEquals(19, entities.ingredients.count { it.categoryId == 14L })  // + 5 in 13 -> 14
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

    /** Catalog gaps (user 25.11.2026): migration 13 -> 14 for existing installs, JSON for new ones; both the same. */
    @Test
    fun catalogGapsMigrationMatchesSeedFile() {
        val propertyKeys = entities.properties.associate { it.id to it.key }
        val categoryKeys = entities.categories.associate { it.id to it.key }
        assertEquals(89, Migrations.CATALOG_GAPS_ROWS.size)
        Migrations.CATALOG_GAPS_ROWS.forEach { row ->
            val seed = entities.ingredients.single { it.id == row.id }
            assertEquals(row.key, seed.key)
            assertEquals(row.categoryId, seed.categoryId, "category of ${row.key} (${categoryKeys[seed.categoryId]})")
            assertEquals(
                row.properties.toSet(),
                entities.ingredientProperties.filter { it.ingredientId == row.id }.map { propertyKeys.getValue(it.propertyId) }.toSet(),
            )
            assertEquals(row.synonymsDe, entities.ingredientSynonyms.filter { it.ingredientId == row.id }.map { it.synonym })
            // Ids follow category x 100 + n, below the custom ingredients.
            assertEquals(row.categoryId, row.id / 100)
        }
    }


    /** Sweeteners and figs (user 25.11.2026): migration 14 -> 15 for existing installs, JSON for new ones; both the same. */
    @Test
    fun sweetenersMigrationMatchesSeedFile() {
        val propertyKeys = entities.properties.associate { it.id to it.key }
        assertEquals(11, Migrations.SWEETENER_ROWS.size)
        Migrations.SWEETENER_ROWS.forEach { row ->
            val seed = entities.ingredients.single { it.id == row.id }
            assertEquals(row.key, seed.key)
            assertEquals(row.categoryId, seed.categoryId)
            assertEquals(
                row.properties.toSet(),
                entities.ingredientProperties.filter { it.ingredientId == row.id }.map { propertyKeys.getValue(it.propertyId) }.toSet(),
            )
            assertEquals(row.synonymsDe, entities.ingredientSynonyms.filter { it.ingredientId == row.id }.map { it.synonym })
            assertEquals(row.categoryId, row.id / 100)
        }
        // Sugar alcohols upset the gut in larger amounts; stevia and sweeteners like sucralose count as neutral here.
        val polyols = entities.properties.single { it.key == "polyols" }.id
        val withPolyols = entities.ingredientProperties.filter { it.propertyId == polyols }.map { it.ingredientId }.toSet()
        assertTrue(listOf(1223L, 1224L, 1225L, 1226L).all { it in withPolyols })
        assertTrue(1227L !in withPolyols)
    }
    @Test
    fun biscuitsAreInTheCatalog() {
        val biscuits = entities.ingredients.single { it.key == "biscuits" }
        val keys = entities.properties.associate { it.id to it.key }
        assertEquals(
            setOf("gluten", "fructans", "milk_protein", "lactose", "egg", "high_fat"),
            entities.ingredientProperties.filter { it.ingredientId == biscuits.id }.map { keys.getValue(it.propertyId) }.toSet(),
        )
        assertTrue(entities.ingredientSynonyms.any { it.ingredientId == biscuits.id && it.synonym == "Plätzchen" })
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
