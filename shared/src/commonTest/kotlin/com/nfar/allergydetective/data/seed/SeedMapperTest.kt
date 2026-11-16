package com.nfar.allergydetective.data.seed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeedMapperTest {

    private val raw = RawSeed(
        categories = """[{"id": 1, "key": "vegetables"}]""",
        properties = """[{"id": 101, "key": "histamine", "group": "intolerance",
            "windows": [{"startMin": 0, "endMin": 240}], "symptomGroups": ["skin"]}]""",
        ingredients = """[
            {"id": 701, "key": "spinach", "category": "vegetables", "properties": ["histamine"],
             "synonyms": {"de": ["Blattspinat"]}},
            {"id": 702, "key": "cucumber", "category": "vegetables"}]""",
        symptoms = """[{"id": 2003, "key": "hives", "group": "skin"},
            {"id": 2004, "key": "swelling", "group": "skin", "isRedFlag": true}]""",
        lifestyleFactors = """[{"id": 1, "key": "sport"}]""",
    )

    @Test
    fun resolvesKeysToIds() {
        val entities = SeedParser.parse(raw).toEntities()

        val spinach = entities.ingredients.first { it.key == "spinach" }
        assertEquals(1L, spinach.categoryId)
        assertEquals(false, spinach.isCustom)
        assertNull(spinach.customName)
        assertEquals(701L to 101L, entities.ingredientProperties.single().let { it.ingredientId to it.propertyId })
        assertEquals("Blattspinat", entities.ingredientSynonyms.single { it.locale == "de" }.synonym)
    }

    @Test
    fun ingredientWithoutTriggersHasNoPropertyRows() {
        val entities = SeedParser.parse(raw).toEntities()

        assertTrue(entities.ingredientProperties.none { it.ingredientId == 702L })
    }

    @Test
    fun mapsWindowsSymptomGroupsAndRedFlags() {
        val entities = SeedParser.parse(raw).toEntities()

        val window = entities.propertyWindows.single()
        assertEquals(listOf(101L, 0, 240), listOf(window.propertyId, window.startMin, window.endMin))
        assertEquals("skin", entities.propertySymptomGroups.single().symptomGroupKey)
        assertEquals(setOf("swelling"), entities.symptoms.filter { it.isRedFlag }.map { it.key }.toSet())
    }

    @Test
    fun failsOnUnknownProperty() {
        val broken = raw.copy(ingredients = """[{"id": 1, "key": "x", "category": "vegetables", "properties": ["typo"]}]""")

        assertFailsWith<IllegalArgumentException> { SeedParser.parse(broken).toEntities() }
    }

    @Test
    fun failsOnUnknownCategory() {
        val broken = raw.copy(ingredients = """[{"id": 1, "key": "x", "category": "typo"}]""")

        assertFailsWith<IllegalArgumentException> { SeedParser.parse(broken).toEntities() }
    }

    @Test
    fun failsOnDuplicateKey() {
        val broken = raw.copy(lifestyleFactors = """[{"id": 1, "key": "sport"}, {"id": 2, "key": "sport"}]""")

        assertFailsWith<IllegalArgumentException> { SeedParser.parse(broken).toEntities() }
    }

    @Test
    fun failsOnInvalidWindow() {
        val broken = raw.copy(
            properties = """[{"id": 101, "key": "histamine", "group": "intolerance",
                "windows": [{"startMin": 240, "endMin": 0}], "symptomGroups": ["skin"]}]""",
        )

        assertFailsWith<IllegalArgumentException> { SeedParser.parse(broken).toEntities() }
    }

    @Test
    fun failsOnUnknownSymptomGroup() {
        val broken = raw.copy(
            properties = """[{"id": 101, "key": "histamine", "group": "intolerance",
                "windows": [{"startMin": 0, "endMin": 240}], "symptomGroups": ["typo"]}]""",
        )

        assertFailsWith<IllegalArgumentException> { SeedParser.parse(broken).toEntities() }
    }
}
