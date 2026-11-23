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

    /**
     * Self-test 18.11.: raw or cooked for ingredients with a heat-labile trigger (birch pollen).
     * Existing entries of those ingredients were analysed as raw, so they stay raw.
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE meal_ingredients ADD COLUMN preparation TEXT")
            connection.execSQL("ALTER TABLE favorite_ingredients ADD COLUMN preparation TEXT")
            val heatLabile = "SELECT ip.ingredientId FROM ingredient_properties ip " +
                "JOIN properties p ON p.id = ip.propertyId WHERE p.`key` = 'birch_pollen_cross'"
            connection.execSQL("UPDATE meal_ingredients SET preparation = 'RAW' WHERE ingredientId IN ($heatLabile)")
            connection.execSQL("UPDATE favorite_ingredients SET preparation = 'RAW' WHERE ingredientId IN ($heatLabile)")
        }
    }

    /** Self-test 18.11.: an illness over several days is entered once as a period. */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `sick_periods` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`startDay` INTEGER NOT NULL, `endDay` INTEGER)",
            )
        }
    }

    /** Self-test 19.11.: on a sick day the user says whether a complaint belongs to the illness. */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE symptom_entries ADD COLUMN illnessRelation TEXT")
        }
    }

    /**
     * Feedback 20.11.: the kind of a meal (incl. the new "drink") is stored instead of being
     * derived from the time. Existing rows get it from their local time once:
     * 8:00 breakfast, 12:30 lunch, 19:00 dinner, any other time a snack.
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE meals ADD COLUMN kind TEXT")
            connection.execSQL(
                "UPDATE meals SET kind = CASE ((((eatenAt / 60000) + tzOffsetMin) % 1440) + 1440) % 1440 " +
                    "WHEN 480 THEN 'BREAKFAST' WHEN 750 THEN 'LUNCH' WHEN 1140 THEN 'DINNER' ELSE 'SNACK' END",
            )
        }
    }

    /** Feedback 20.11.: optional duration of a complaint. */
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE symptom_entries ADD COLUMN duration TEXT")
        }
    }

    /** User note 21.11.2026: spices were missing. New category with 15 spices, same rows as the seed JSON. */
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("INSERT OR IGNORE INTO categories (id, `key`) VALUES ($SPICES_CATEGORY, 'spices_herbs')")
            SPICES_ROWS.forEach { connection.insertSeedIngredient(it) }
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)

    private const val OILS_CATEGORY = 13L
    private const val SPICES_CATEGORY = 14L

    internal val SPICES_ROWS = listOf(
        SeedRow(1401, "black_pepper", SPICES_CATEGORY, listOf("spicy"), listOf("Pfeffer")),
        SeedRow(1402, "paprika_powder", SPICES_CATEGORY, emptyList(), listOf("Paprika edelsüß", "Paprikagewürz")),
        SeedRow(1403, "chili_powder", SPICES_CATEGORY, listOf("spicy"), listOf("Cayennepfeffer", "Cayenne", "Chiliflocken")),
        SeedRow(1404, "curry_powder", SPICES_CATEGORY, listOf("spicy", "mustard"), listOf("Curry")),
        SeedRow(1405, "cinnamon", SPICES_CATEGORY, listOf("histamine_liberator"), emptyList()),
        SeedRow(1406, "nutmeg", SPICES_CATEGORY, emptyList(), listOf("Muskat")),
        SeedRow(1407, "cumin", SPICES_CATEGORY, emptyList(), listOf("Kumin", "Cumin")),
        SeedRow(1408, "coriander_ground", SPICES_CATEGORY, emptyList(), listOf("Koriander")),
        SeedRow(1409, "turmeric", SPICES_CATEGORY, emptyList(), listOf("Gelbwurz")),
        SeedRow(1410, "ginger_ground", SPICES_CATEGORY, listOf("spicy"), listOf("Ingwerpulver")),
        SeedRow(1411, "celery_seed_salt", SPICES_CATEGORY, listOf("celery"), listOf("Selleriesalz", "Selleriesamen")),
        SeedRow(1412, "mustard_seed", SPICES_CATEGORY, listOf("mustard"), listOf("Senfkörner")),
        SeedRow(1413, "garlic_powder", SPICES_CATEGORY, listOf("fructans"), listOf("Knoblauchgranulat")),
        SeedRow(1414, "onion_powder", SPICES_CATEGORY, listOf("fructans"), listOf("Zwiebelgranulat")),
        SeedRow(1415, "dried_herbs", SPICES_CATEGORY, emptyList(), listOf("Oregano", "Thymian", "Kräuter der Provence", "italienische Kräuter")),
    )

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
