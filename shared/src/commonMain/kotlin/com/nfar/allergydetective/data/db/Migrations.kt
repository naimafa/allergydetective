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

    /** User note 21.11.2026: dishes in the user's own order. Existing dishes start alphabetically, as before. */
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE favorites ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            connection.execSQL(
                "UPDATE favorites SET sortOrder = (SELECT COUNT(*) FROM favorites f2 " +
                    "WHERE f2.name COLLATE NOCASE < favorites.name COLLATE NOCASE " +
                    "OR (f2.name COLLATE NOCASE = favorites.name COLLATE NOCASE AND f2.id < favorites.id))",
            )
        }
    }

    /**
     * User 23.11.2026: spices and herbs as two categories, with thyme, rosemary, sage, oregano, basil and more.
     * "Getrocknete Kräuter" moves to the herbs; oregano and thyme are no longer its synonyms but ingredients of
     * their own. Same rows as the seed JSON.
     */
    val MIGRATION_11_12 = object : Migration(11, 12) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("INSERT OR IGNORE INTO categories (id, `key`) VALUES ($HERBS_CATEGORY, 'herbs')")
            connection.execSQL("UPDATE ingredients SET categoryId = $HERBS_CATEGORY WHERE id = $DRIED_HERBS")
            connection.execSQL("DELETE FROM ingredient_synonyms WHERE ingredientId = $DRIED_HERBS AND synonym IN ('Oregano', 'Thymian')")
            HERBS_ROWS.forEach { connection.insertSeedIngredient(it) }
        }
    }

    /**
     * User 24.11.2026: a meal keeps what was logged - its dishes as they were then (name, portion) and every occurrence
     * of an ingredient with its origin. Existing meals get their ingredients as single occurrences; nothing is lost,
     * they only open without dish cards.
     */
    val MIGRATION_12_13 = object : Migration(12, 13) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `meal_dishes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mealId` INTEGER NOT NULL, " +
                    "`favoriteId` INTEGER, `name` TEXT NOT NULL, `portion` TEXT NOT NULL, `position` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`mealId`) REFERENCES `meals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                    "FOREIGN KEY(`favoriteId`) REFERENCES `favorites`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_dishes_mealId` ON `meal_dishes` (`mealId`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_dishes_favoriteId` ON `meal_dishes` (`favoriteId`)")
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `meal_item_occurrences` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mealId` INTEGER NOT NULL, " +
                    "`mealDishId` INTEGER, `ingredientId` INTEGER NOT NULL, `portion` TEXT NOT NULL, `preparation` TEXT, " +
                    "FOREIGN KEY(`mealId`) REFERENCES `meals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                    "FOREIGN KEY(`mealDishId`) REFERENCES `meal_dishes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                    "FOREIGN KEY(`ingredientId`) REFERENCES `ingredients`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )",
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_item_occurrences_mealId` ON `meal_item_occurrences` (`mealId`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_item_occurrences_mealDishId` ON `meal_item_occurrences` (`mealDishId`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_item_occurrences_ingredientId` ON `meal_item_occurrences` (`ingredientId`)")
            connection.execSQL(
                "INSERT INTO meal_item_occurrences (mealId, mealDishId, ingredientId, portion, preparation) " +
                    "SELECT mealId, NULL, ingredientId, portion, preparation FROM meal_ingredients ORDER BY mealId, ingredientId",
            )
        }
    }

    /**
     * User 25.11.2026: "Kekse" were missing. 89 everyday ingredients (baked goods, sweets, processed food, snacks,
     * gluten-free grains and more, docs/KATALOG_LUECKEN.md). No schema change; same rows as the seed JSON.
     */
    val MIGRATION_13_14 = object : Migration(13, 14) {
        override suspend fun migrate(connection: SQLiteConnection) {
            CATALOG_GAPS_ROWS.forEach { connection.insertSeedIngredient(it) }
        }
    }

    /** User 25.11.2026: the newer sweeteners (xylitol, erythritol, stevia …) and figs. No schema change. */
    val MIGRATION_14_15 = object : Migration(14, 15) {
        override suspend fun migrate(connection: SQLiteConnection) {
            SWEETENER_ROWS.forEach { connection.insertSeedIngredient(it) }
        }
    }

    /**
     * User 25.11.2026: "Getreide & Backwaren" split into "Getreide & Mehl", "Nudeln & Teigwaren" (pasta often has egg)
     * and "Brot & Backwaren"; flours, baking agents, egg noodles and single dried fruits (dates, raisins …) added.
     * "Rosinen" and "Datteln" move from the dried fruit synonyms to their own ingredients. Same as the seed JSON.
     */
    val MIGRATION_15_16 = object : Migration(15, 16) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("UPDATE categories SET `key` = 'grains_flour' WHERE id = $GRAINS_CATEGORY")
            connection.execSQL("INSERT OR IGNORE INTO categories (id, `key`) VALUES ($PASTA_CATEGORY, 'pasta')")
            connection.execSQL("INSERT OR IGNORE INTO categories (id, `key`) VALUES ($BREAD_CATEGORY, 'bread_bakery')")
            CATEGORY_MOVES_16.forEach { (categoryId, ids) ->
                connection.execSQL("UPDATE ingredients SET categoryId = $categoryId WHERE id IN (${ids.joinToString(", ")}) AND isCustom = 0")
            }
            connection.execSQL("DELETE FROM ingredient_synonyms WHERE ingredientId = $DRIED_FRUIT AND synonym IN ('Rosinen', 'Datteln')")
            GRAINS_ROWS.forEach { connection.insertSeedIngredient(it) }
        }
    }

    /**
     * User 26.11.2026: complaints of the joints and muscles (pain, swelling / inflammation). Their own group; gluten and
     * histamine are linked to it, as joint pain is described for both.
     */
    val MIGRATION_16_17 = object : Migration(16, 17) {
        override suspend fun migrate(connection: SQLiteConnection) {
            JOINT_SYMPTOMS_17.forEach { (id, key) ->
                connection.execSQL("INSERT OR IGNORE INTO symptoms (id, `key`, groupKey, isRedFlag) VALUES ($id, '$key', 'joints', 0)")
            }
            listOf(101L, 102L).forEach { propertyId ->
                connection.execSQL("INSERT OR IGNORE INTO property_symptom_groups (propertyId, symptomGroupKey) VALUES ($propertyId, 'joints')")
            }
        }
    }

    /**
     * User 27.11.2026: package photos. "Magermilchpulver" was missing (milk powder, with the triggers of milk). Salt, sugar,
     * starch and cow's milk got their package names ("Meersalz", "Rohrzucker", "Maisstärke" ...) in their display names;
     * that is text only. No schema change; same row as the seed JSON.
     */
    val MIGRATION_17_18 = object : Migration(17, 18) {
        override suspend fun migrate(connection: SQLiteConnection) {
            MILK_POWDER_ROWS.forEach { connection.insertSeedIngredient(it) }
        }
    }

    internal val MILK_POWDER_ROWS = listOf(
        SeedRow(217, "milk_powder", 2L, listOf("milk_protein", "lactose"), listOf("Trockenmilch")),
    )

    /** The new symptoms of version 17; same ids and keys as symptoms.json. */
    val JOINT_SYMPTOMS_17 = listOf(5001L to "joint_pain", 5002L to "joint_swelling", 5003L to "muscle_pain")

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18)

    private const val HERBS_CATEGORY = 15L
    internal const val DRIED_HERBS = 1415L

    internal val HERBS_ROWS = listOf(
        SeedRow(1501, "thyme", HERBS_CATEGORY, emptyList(), emptyList()),
        SeedRow(1502, "rosemary", HERBS_CATEGORY, emptyList(), emptyList()),
        SeedRow(1503, "sage", HERBS_CATEGORY, emptyList(), emptyList()),
        SeedRow(1504, "oregano", HERBS_CATEGORY, emptyList(), emptyList()),
        SeedRow(1505, "basil", HERBS_CATEGORY, emptyList(), emptyList()),
        SeedRow(1506, "parsley", HERBS_CATEGORY, emptyList(), listOf("Petersilie glatt", "Petersilie kraus")),
        SeedRow(1507, "dill", HERBS_CATEGORY, emptyList(), emptyList()),
        SeedRow(1508, "chives", HERBS_CATEGORY, listOf("fructans"), emptyList()),
        SeedRow(1509, "mint", HERBS_CATEGORY, emptyList(), listOf("Pfefferminze", "Minze frisch")),
        SeedRow(1510, "coriander_leaves", HERBS_CATEGORY, emptyList(), listOf("Koriandergrün", "Koriander frisch")),
        SeedRow(1511, "bay_leaf", HERBS_CATEGORY, emptyList(), listOf("Lorbeer")),
        SeedRow(1512, "marjoram", HERBS_CATEGORY, emptyList(), emptyList()),
        SeedRow(1513, "tarragon", HERBS_CATEGORY, emptyList(), emptyList()),
    )

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

    /** Catalog gaps (user 25.11.2026), migration 13 -> 14. Category ids as in categories.json. */
    internal val CATALOG_GAPS_ROWS = listOf(
        SeedRow(115, "biscuits", 1L, listOf("gluten", "fructans", "milk_protein", "lactose", "egg", "high_fat"), listOf("Plätzchen", "Butterkekse", "Cookies", "Gebäck", "Biskuit")),
        SeedRow(116, "waffles", 1L, listOf("gluten", "fructans", "egg", "milk_protein", "lactose", "high_fat"), listOf("Waffel", "Eiswaffel")),
        SeedRow(117, "rusk", 1L, listOf("gluten", "fructans"), emptyList()),
        SeedRow(118, "crispbread", 1L, listOf("gluten", "fructans"), emptyList()),
        SeedRow(119, "crackers_pretzel_sticks", 1L, listOf("gluten", "fructans"), listOf("Salzbrezeln", "Kräcker", "Salzstangen")),
        SeedRow(120, "lye_pastry", 1L, listOf("gluten", "fructans"), listOf("Brezel", "Laugenbrötchen", "Laugenstange")),
        SeedRow(121, "flatbread", 1L, listOf("gluten", "fructans"), listOf("Pide", "Naan", "Pita")),
        SeedRow(122, "tortilla_wrap", 1L, listOf("gluten", "fructans"), listOf("Wrap", "Burrito", "Weizentortilla")),
        SeedRow(123, "sweet_yeast_pastry", 1L, listOf("gluten", "fructans", "egg", "milk_protein", "lactose"), listOf("Hefezopf", "Rosinenbrötchen", "Brioche", "Rosinenschnecke")),
        SeedRow(124, "doughnut", 1L, listOf("gluten", "egg", "milk_protein", "high_fat"), listOf("Krapfen", "Donut", "Kreppel")),
        SeedRow(125, "cream_cake", 1L, listOf("gluten", "egg", "milk_protein", "lactose", "high_fat"), listOf("Torte", "Cremetorte")),
        SeedRow(126, "pancakes_crepes", 1L, listOf("gluten", "egg", "milk_protein", "lactose"), listOf("Eierkuchen", "Pancakes", "Crêpes", "Palatschinken")),
        SeedRow(127, "breadcrumbs_coating", 1L, listOf("gluten", "fructans", "egg"), listOf("Semmelbrösel", "Paniermehl", "paniert")),
        SeedRow(128, "gnocchi", 1L, listOf("gluten"), emptyList()),
        SeedRow(129, "muesli", 1L, listOf("gluten", "fructans", "tree_nuts"), listOf("Granola", "Knuspermüsli", "Müslimischung")),
        SeedRow(130, "cornflakes", 1L, listOf("gluten"), listOf("Frühstücksflocken", "Cerealien")),
        SeedRow(131, "rice_cakes", 1L, emptyList(), listOf("Reiscracker")),
        SeedRow(132, "quinoa", 1L, emptyList(), emptyList()),
        SeedRow(133, "millet", 1L, emptyList(), emptyList()),
        SeedRow(134, "buckwheat", 1L, emptyList(), emptyList()),
        SeedRow(135, "gluten_free_pasta", 1L, emptyList(), listOf("Reisnudeln", "Linsennudeln", "Maisnudeln")),
        SeedRow(211, "pudding", 2L, listOf("milk_protein", "lactose"), listOf("Vanillepudding", "Schokopudding")),
        SeedRow(212, "rice_pudding", 2L, listOf("milk_protein", "lactose"), listOf("Grießbrei")),
        SeedRow(213, "sour_cream", 2L, listOf("milk_protein", "lactose", "high_fat"), listOf("Schmand", "Crème fraîche", "Sauerrahm")),
        SeedRow(214, "buttermilk", 2L, listOf("milk_protein", "lactose"), emptyList()),
        SeedRow(215, "soy_yogurt", 2L, listOf("soy"), listOf("Soja-Dessert")),
        SeedRow(216, "ghee", 2L, listOf("milk_protein", "high_fat"), listOf("Butterschmalz")),
        SeedRow(308, "blue_cheese", 3L, listOf("milk_protein", "histamine"), listOf("Gorgonzola", "Roquefort")),
        SeedRow(309, "processed_cheese", 3L, listOf("milk_protein", "lactose"), listOf("Scheiblettenkäse", "Burgerkäse")),
        SeedRow(310, "goat_cheese", 3L, listOf("milk_protein"), emptyList()),
        SeedRow(311, "halloumi", 3L, listOf("milk_protein"), listOf("Grillkäse")),
        SeedRow(408, "cold_cuts", 4L, listOf("high_fat"), listOf("Lyoner", "Mortadella", "Fleischwurst", "Geflügelwurst", "Aufschnitt")),
        SeedRow(409, "frankfurters", 4L, listOf("high_fat"), listOf("Wiener", "Frankfurter", "Hot Dog")),
        SeedRow(410, "liver_sausage", 4L, listOf("high_fat"), listOf("Pastete", "Leberpastete")),
        SeedRow(411, "breaded_meat", 4L, listOf("gluten", "fructans", "egg", "high_fat"), listOf("Chicken Nuggets", "Schnitzel paniert", "Cordon bleu")),
        SeedRow(507, "fish_fingers", 5L, listOf("fish", "gluten", "fructans"), listOf("panierter Fisch")),
        SeedRow(508, "pickled_herring", 5L, listOf("fish", "histamine"), listOf("Matjes", "Bismarckhering", "Rollmops")),
        SeedRow(509, "surimi", 5L, listOf("fish", "egg"), listOf("Krebsfleischimitat")),
        SeedRow(719, "broccoli", 7L, listOf("fructans"), emptyList()),
        SeedRow(720, "cabbage", 7L, listOf("fructans", "polyols"), listOf("Weißkohl", "Rotkohl", "Wirsing", "Kraut")),
        SeedRow(721, "brussels_sprouts", 7L, listOf("fructans", "galactans"), emptyList()),
        SeedRow(722, "asparagus", 7L, listOf("fructose", "fructans"), emptyList()),
        SeedRow(723, "beetroot", 7L, listOf("fructans", "galactans"), listOf("Rote Rübe")),
        SeedRow(724, "sweet_potato", 7L, listOf("polyols"), emptyList()),
        SeedRow(725, "pumpkin", 7L, emptyList(), listOf("Hokkaido", "Butternut")),
        SeedRow(726, "fennel", 7L, emptyList(), emptyList()),
        SeedRow(727, "rocket_radish", 7L, emptyList(), listOf("Rauke", "Radieschen")),
        SeedRow(728, "pickled_gherkins", 7L, listOf("histamine"), listOf("Gewürzgurken", "Cornichons", "saure Gurken")),
        SeedRow(729, "fries", 7L, listOf("high_fat"), listOf("Pommes", "Kroketten")),
        SeedRow(730, "potato_chips", 7L, listOf("high_fat"), listOf("Chips")),
        SeedRow(731, "tomato_sauce", 7L, listOf("histamine", "histamine_liberator"), listOf("passierte Tomaten", "Pizzasoße", "Tomatenmark")),
        SeedRow(815, "grapes", 8L, listOf("fructose"), listOf("Trauben")),
        SeedRow(816, "apricot", 8L, listOf("polyols", "birch_pollen_cross"), listOf("Marille")),
        SeedRow(817, "melon", 8L, emptyList(), listOf("Honigmelone", "Cantaloupe")),
        SeedRow(818, "papaya", 8L, listOf("histamine_liberator"), emptyList()),
        SeedRow(819, "currants_gooseberries", 8L, emptyList(), listOf("Johannisbeeren", "Stachelbeeren")),
        SeedRow(820, "pomegranate", 8L, emptyList(), emptyList()),
        SeedRow(821, "coconut", 8L, listOf("high_fat"), listOf("Kokosmilch", "Kokosraspeln", "Kokosnuss")),
        SeedRow(908, "brazil_nut", 9L, listOf("tree_nuts"), emptyList()),
        SeedRow(909, "macadamia", 9L, listOf("tree_nuts"), emptyList()),
        SeedRow(910, "pecan", 9L, listOf("tree_nuts"), emptyList()),
        SeedRow(911, "sunflower_pumpkin_seeds", 9L, emptyList(), listOf("Sonnenblumenkerne", "Kürbiskerne", "Kerne")),
        SeedRow(912, "linseed_chia", 9L, emptyList(), listOf("Leinsamen", "Chiasamen")),
        SeedRow(913, "poppy_seed", 9L, emptyList(), emptyList()),
        SeedRow(1008, "peas", 10L, listOf("galactans", "fructans"), listOf("grüne Erbsen", "Zuckerschoten")),
        SeedRow(1009, "edamame", 10L, listOf("soy", "galactans"), listOf("Sojabohnen")),
        SeedRow(1010, "tempeh", 10L, listOf("soy"), emptyList()),
        SeedRow(1111, "hot_chocolate", 11L, listOf("milk_protein", "lactose", "histamine_liberator"), listOf("Kakaogetränk", "Schokomilch", "heiße Schokolade")),
        SeedRow(1112, "milk_coffee", 11L, listOf("caffeine", "milk_protein", "lactose"), listOf("Cappuccino", "Latte macchiato", "Milchkaffee")),
        SeedRow(1113, "soft_drink", 11L, listOf("fructose"), listOf("Limo", "Fanta", "Sprite", "Eistee", "Limonade")),
        SeedRow(1114, "smoothie", 11L, listOf("fructose", "polyols"), emptyList()),
        SeedRow(1115, "alcohol_free_beer", 11L, listOf("gluten", "histamine"), listOf("alkoholfreies Bier")),
        SeedRow(1116, "kombucha", 11L, listOf("histamine", "caffeine"), emptyList()),
        SeedRow(1212, "jam", 12L, listOf("fructose"), listOf("Konfitüre", "Gelee", "Fruchtaufstrich")),
        SeedRow(1213, "nut_nougat_spread", 12L, listOf("tree_nuts", "milk_protein", "lactose", "high_fat"), listOf("Nutella", "Schokocreme", "Nougatcreme")),
        SeedRow(1214, "marzipan", 12L, listOf("tree_nuts"), listOf("Persipan")),
        SeedRow(1215, "fruit_gums", 12L, emptyList(), listOf("Gummibärchen", "Weingummi")),
        SeedRow(1216, "cereal_bar", 12L, listOf("gluten", "tree_nuts"), listOf("Müsliriegel", "Schokoriegel", "Proteinriegel")),
        SeedRow(1217, "popcorn", 12L, emptyList(), emptyList()),
        SeedRow(1218, "sorbet", 12L, listOf("fructose"), listOf("Wassereis")),
        SeedRow(1219, "maple_syrup", 12L, emptyList(), emptyList()),
        SeedRow(1220, "stock_cube", 12L, listOf("celery", "histamine_liberator"), listOf("Brühwürfel", "Bouillon", "Suppenwürze", "Gemüsebrühe")),
        SeedRow(1221, "remoulade_sauce", 12L, listOf("egg", "mustard"), listOf("Grillsoße", "Burgersoße", "Cocktailsoße")),
        SeedRow(1222, "instant_noodles", 12L, listOf("gluten", "high_fat", "celery"), listOf("Tütensuppe", "Ramen", "Fertigsuppe", "Instantnudeln")),
        SeedRow(1416, "salt", 14L, emptyList(), listOf("Kräutersalz", "Meersalz", "Speisesalz")),
        SeedRow(1417, "vanilla", 14L, emptyList(), listOf("Vanillezucker", "Vanilleschote")),
        SeedRow(1418, "cloves", 14L, emptyList(), listOf("Gewürznelken")),
        SeedRow(1419, "cardamom", 14L, emptyList(), emptyList()),
        SeedRow(1420, "anise_fennel_seed", 14L, emptyList(), listOf("Sternanis", "Fenchelsamen", "Anis")),
    )

    /** Sweeteners and figs (user 25.11.2026), migration 14 -> 15. */
    internal val SWEETENER_ROWS = listOf(
        SeedRow(822, "fig", 8L, listOf("fructose", "fructans"), listOf("Feigen", "getrocknete Feigen")),
        SeedRow(1223, "xylitol", 12L, listOf("polyols"), listOf("Xylit", "Xylitol", "Birkenzucker")),
        SeedRow(1224, "erythritol", 12L, listOf("polyols"), listOf("Erythritol", "Sukrin")),
        SeedRow(1225, "sorbitol", 12L, listOf("polyols"), listOf("Sorbitol", "Zuckeraustauschstoff")),
        SeedRow(1226, "maltitol", 12L, listOf("polyols"), listOf("Maltit", "Maltitol", "Isomalt")),
        SeedRow(1227, "stevia", 12L, emptyList(), listOf("Steviolglycoside", "Stevia-Tabs", "Steviapulver")),
        SeedRow(1228, "sweetener", 12L, emptyList(), listOf("Sucralose", "Aspartam", "Saccharin", "Cyclamat", "Süßstofftabletten", "Flüssigsüße")),
        SeedRow(1229, "monk_fruit", 12L, emptyList(), listOf("Mönchsfrucht", "Luo Han Guo", "Monk Fruit")),
        SeedRow(1230, "coconut_blossom_sugar", 12L, emptyList(), listOf("Kokoszucker")),
        SeedRow(1231, "date_syrup", 12L, listOf("fructose", "fructans"), listOf("Dattelsüße", "Dattelpaste")),
        SeedRow(1232, "rice_syrup", 12L, emptyList(), listOf("Reissüße")),
    )

    internal const val GRAINS_CATEGORY = 1L
    internal const val PASTA_CATEGORY = 16L
    internal const val BREAD_CATEGORY = 17L
    internal const val DRIED_FRUIT = 813L

    /** Category of existing ingredients after 15 -> 16: pasta and bread / baked goods leave "grains". */
    internal val CATEGORY_MOVES_16: Map<Long, List<Long>> = mapOf(
        PASTA_CATEGORY to listOf(105L, 106L, 128L, 135L, 1222L),
        BREAD_CATEGORY to listOf(101L, 102L, 103L, 104L, 109L, 110L, 113L, 114L, 115L, 116L, 117L, 118L, 119L, 120L, 121L, 122L, 123L, 124L, 125L, 126L, 131L),
    )

    /** Flours, baking agents, egg noodles and single dried fruits (user 25.11.2026), migration 15 -> 16. */
    internal val GRAINS_ROWS = listOf(
        SeedRow(136, "wheat_flour", 1L, listOf("gluten", "fructans"), listOf("Mehl", "Weißmehl", "Type 405", "Type 550")),
        SeedRow(137, "wholemeal_wheat_flour", 1L, listOf("gluten", "fructans"), listOf("Vollkornmehl", "Type 1050")),
        SeedRow(138, "spelt_flour", 1L, listOf("gluten", "fructans"), listOf("Dinkel", "Type 630")),
        SeedRow(139, "wholemeal_spelt_flour", 1L, listOf("gluten", "fructans"), listOf("Dinkelvollkorn")),
        SeedRow(140, "rye_flour", 1L, listOf("gluten", "fructans"), listOf("Roggen", "Roggenvollkornmehl")),
        SeedRow(141, "semolina", 1L, listOf("gluten", "fructans"), listOf("Hartweizengrieß", "Weichweizengrieß")),
        SeedRow(142, "oat_flour", 1L, listOf("gluten"), listOf("Haferkleie")),
        SeedRow(143, "rice_flour", 1L, emptyList(), listOf("Reisstärke")),
        SeedRow(144, "starch", 1L, emptyList(), listOf("Maisstärke", "Kartoffelstärke", "Stärke", "Mondamin")),
        SeedRow(145, "gluten_free_flour_mix", 1L, emptyList(), listOf("glutenfreies Mehl", "Mehlmischung glutenfrei")),
        SeedRow(146, "almond_flour", 1L, listOf("tree_nuts"), listOf("gemahlene Mandeln")),
        SeedRow(147, "chickpea_flour", 1L, listOf("galactans"), listOf("Besan")),
        SeedRow(148, "baking_powder", 1L, emptyList(), listOf("Natron", "Weinsteinbackpulver", "Backsoda")),
        SeedRow(149, "yeast", 1L, emptyList(), listOf("Trockenhefe", "Frischhefe", "Germ")),
        SeedRow(1601, "egg_noodles", 16L, listOf("gluten", "fructans", "egg"), listOf("Spätzle", "Bandnudeln", "Tagliatelle", "Eierspätzle", "Maultaschen", "Eiernudeln")),
        SeedRow(823, "dates", 8L, listOf("fructose", "fructans"), listOf("Dattel", "Medjool")),
        SeedRow(824, "raisins", 8L, listOf("fructose"), listOf("Rosine", "Sultaninen", "Korinthen")),
        SeedRow(825, "dried_apricots", 8L, listOf("sulfites", "polyols"), listOf("Trockenaprikosen", "getrocknete Marillen")),
        SeedRow(826, "prunes", 8L, listOf("polyols"), listOf("Trockenpflaumen", "Dörrpflaumen", "getrocknete Pflaumen")),
        SeedRow(827, "dried_cranberries", 8L, listOf("fructose"), listOf("Cranberries getrocknet", "Moosbeeren")),
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
