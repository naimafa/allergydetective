package com.nfar.allergydetective.data.db

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Seed changes for existing installs (CODING_RICHTLINIEN: every change as a migration).
 * New installs get the same rows from the seed JSON instead.
 */
object Migrations {

    /** Self-test 17.11.: "Dinkel Vollkornbrot" became spelt bread + wheat wholemeal bread. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.insertSeedIngredient(
                SeedRow(114, "spelt_wholemeal_bread", 1, listOf("gluten", "fructans"), listOf("Dinkel-Vollkornbrot", "Dinkel Vollkornbrot")),
            )
        }
    }

    /** Self-test 18.11.: oils in general and olives had no category. Same rows as the seed JSON. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("INSERT OR IGNORE INTO categories (id, `key`) VALUES ($OILS_CATEGORY, 'oils_fats')")
            OILS_FATS_ROWS.forEach { connection.insertSeedIngredient(it) }
        }
    }

    /** Self-test 18.11.: days of an acute illness can be marked and are left out of the analysis. */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("INSERT OR IGNORE INTO lifestyle_factors (id, `key`) VALUES (4, 'sick')")
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

    private const val OILS_CATEGORY = 13L

    internal val OILS_FATS_ROWS = listOf(
        SeedRow(1301, "cooking_oil", OILS_CATEGORY, listOf("high_fat"), listOf("Öl", "Speiseöl", "Pflanzenöl")),
        SeedRow(1302, "olive_oil", OILS_CATEGORY, listOf("high_fat"), emptyList()),
        SeedRow(1303, "sunflower_oil", OILS_CATEGORY, listOf("high_fat"), emptyList()),
        SeedRow(1304, "rapeseed_oil", OILS_CATEGORY, listOf("high_fat"), listOf("Rapsöl")),
        SeedRow(1305, "coconut_oil", OILS_CATEGORY, listOf("high_fat"), listOf("Kokosfett")),
        SeedRow(1306, "sesame_oil", OILS_CATEGORY, listOf("sesame", "high_fat"), emptyList()),
        SeedRow(1307, "peanut_oil", OILS_CATEGORY, listOf("peanut", "high_fat"), emptyList()),
        SeedRow(1308, "margarine", OILS_CATEGORY, listOf("high_fat"), emptyList()),
        SeedRow(1309, "olives", OILS_CATEGORY, listOf("histamine"), listOf("Olive", "grüne Oliven", "schwarze Oliven")),
    )

    internal class SeedRow(
        val id: Long,
        val key: String,
        val categoryId: Long,
        val properties: List<String>,
        val synonymsDe: List<String>,
    )

    private fun SQLiteConnection.insertSeedIngredient(row: SeedRow) {
        execSQL(
            "INSERT OR IGNORE INTO ingredients (id, `key`, customName, categoryId, isCustom, baseIngredientId) " +
                "VALUES (${row.id}, '${row.key}', NULL, ${row.categoryId}, 0, NULL)",
        )
        if (row.properties.isNotEmpty()) {
            val keys = row.properties.joinToString(", ") { "'$it'" }
            execSQL(
                "INSERT OR IGNORE INTO ingredient_properties (ingredientId, propertyId) " +
                    "SELECT ${row.id}, id FROM properties WHERE `key` IN ($keys)",
            )
        }
        row.synonymsDe.forEach { synonym ->
            execSQL("INSERT INTO ingredient_synonyms (ingredientId, locale, synonym) VALUES (${row.id}, 'de', '$synonym')")
        }
    }
}
