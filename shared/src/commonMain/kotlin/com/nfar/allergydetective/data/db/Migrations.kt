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
            connection.execSQL(
                "INSERT OR IGNORE INTO ingredients (id, `key`, customName, categoryId, isCustom, baseIngredientId) " +
                    "VALUES (114, 'spelt_wholemeal_bread', NULL, 1, 0, NULL)",
            )
            connection.execSQL(
                "INSERT OR IGNORE INTO ingredient_properties (ingredientId, propertyId) " +
                    "SELECT 114, id FROM properties WHERE `key` IN ('gluten', 'fructans')",
            )
            listOf("Dinkel-Vollkornbrot", "Dinkel Vollkornbrot").forEach { synonym ->
                connection.execSQL(
                    "INSERT INTO ingredient_synonyms (ingredientId, locale, synonym) VALUES (114, 'de', '$synonym')",
                )
            }
        }
    }

    val ALL = arrayOf(MIGRATION_1_2)
}
