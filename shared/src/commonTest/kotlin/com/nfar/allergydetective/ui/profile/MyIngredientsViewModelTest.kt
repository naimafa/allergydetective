package com.nfar.allergydetective.ui.profile

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.diary.CategoryInfo
import com.nfar.allergydetective.domain.diary.CustomIngredient
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.MealSnapshot
import com.nfar.allergydetective.domain.diary.PropertyInfo
import com.nfar.allergydetective.domain.time.MealSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MyIngredientsViewModelTest {

    private val biscuits = IngredientInfo(115, "biscuits", null, 1)
    private val oats = IngredientInfo(108, "oat_flakes", null, 1)

    /** "Kekse" made by hand (used in 3 meals), "Hafer-Mix" never used. */
    private class FakeRepository : CustomIngredientRepository {
        var items = mutableListOf(
            CustomIngredient(100_000, "Kekse", 12, setOf(102L), mealCount = 3, dishCount = 1),
            CustomIngredient(100_001, "Hafer-Mix", 1, setOf(102L), mealCount = 0, dishCount = 0),
        )
        val replaced = mutableListOf<Pair<Long, Long>>()
        val replacedByDish = mutableListOf<Triple<Long, Long, String>>()
        override suspend fun categories() = listOf(CategoryInfo(1, "grains_flour"), CategoryInfo(12, "condiments_sweets"))
        override suspend fun properties() = listOf(PropertyInfo(102, "gluten"), PropertyInfo(117, "milk_protein"), PropertyInfo(118, "egg"))
        override suspend fun categoryOf(ingredientId: Long) = 1L
        override suspend fun propertiesOf(ingredientId: Long) = if (ingredientId == 115L) setOf(102L, 117L, 118L) else setOf(102L)
        override suspend fun create(name: String, categoryId: Long, baseIngredientId: Long?, propertyIds: Set<Long>) = IngredientInfo(1, null, name)
        override suspend fun customIngredients() = items.toList()
        override suspend fun update(id: Long, name: String, categoryId: Long, propertyIds: Set<Long>) {
            items = items.map { if (it.id == id) it.copy(name = name, categoryId = categoryId, propertyIds = propertyIds) else it }.toMutableList()
        }
        override suspend fun deleteIfUnused(id: Long): Boolean {
            val item = items.single { it.id == id }
            if (item.used) return false
            items.remove(item)
            return true
        }
        override suspend fun replace(from: Long, to: Long) {
            replaced += from to to
            items.removeAll { it.id == from }
        }
        override suspend fun replaceWithDish(from: Long, favoriteId: Long, dishName: String) {
            replacedByDish += Triple(from, favoriteId, dishName)
            items.removeAll { it.id == from }
        }
    }

    private inner class FakeMeals : MealRepository {
        override suspend fun ingredients() = listOf(biscuits, oats) + repository.items.map { it.info }
        override suspend fun synonyms(language: String) = mapOf(115L to listOf("Plätzchen"))
        override suspend fun recentIngredientIds(limit: Int) = emptyList<Long>()
        override suspend fun ingredientIdsWithKnownAllergy() = emptySet<Long>()
        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?, slot: MealSlot?, snapshot: MealSnapshot?) = 0L
        override suspend fun deleteMeal(mealId: Long) = Unit
        override suspend fun favorites() = listOf(FavoriteInfo(7, "Feigen-Datteln-Marmelade", listOf(MealItem(oats.id, com.nfar.allergydetective.domain.analysis.model.Portion.SMALL))))
        override suspend fun saveFavorite(name: String, items: List<MealItem>) = 0L
        override suspend fun deleteFavorite(favoriteId: Long) = Unit
    }

    private val repository = FakeRepository()

    private fun viewModel() = MyIngredientsViewModel(
        repository = repository,
        meals = FakeMeals(),
        names = { _, key -> if (key == "biscuits") "Kekse / Plätzchen" else "Haferflocken" },
        language = { "de" },
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun listsOwnIngredients() {
        assertEquals(listOf("Kekse", "Hafer-Mix"), viewModel().state.value.items.map { it.name })
    }

    @Test
    fun editChangesNameKindAndContents() {
        val vm = viewModel()
        vm.onEdit(repository.items[0])
        vm.onEditName("Butterkekse")
        vm.onEditCategory(1)
        vm.onEditQuery("Plätzchen")
        vm.onAddContents(vm.state.value.editing!!.results.single())
        vm.onRemoveProperty(118)
        vm.onSaveEdit()

        val saved = repository.items.single { it.id == 100_000L }
        assertEquals("Butterkekse", saved.name)
        assertEquals(1L, saved.categoryId)
        assertEquals(setOf(102L, 117L), saved.propertyIds)
        assertNull(vm.state.value.editing)
    }

    @Test
    fun aNameThatExistsBlocksSaving() {
        val vm = viewModel()
        vm.onEdit(repository.items[1])
        vm.onEditName("Haferflocken")
        assertFalse(vm.state.value.editing!!.canSave)

        vm.onEditName("Hafer-Mix")  // its own name is fine
        assertTrue(vm.state.value.editing!!.canSave)
    }

    @Test
    fun unusedIngredientIsDeletedAfterOneQuestion() {
        val vm = viewModel()
        vm.onDelete(repository.items.single { it.name == "Hafer-Mix" })
        assertNotNull(vm.state.value.deleting)

        vm.onConfirmDelete()
        assertEquals(listOf("Kekse"), vm.state.value.items.map { it.name })
    }

    @Test
    fun usedIngredientIsReplacedNotDeleted() {
        val vm = viewModel()
        vm.onDelete(repository.items.single { it.name == "Kekse" })
        assertNull(vm.state.value.deleting)
        assertNotNull(vm.state.value.replacing)

        vm.onReplaceQuery("Plätzchen")
        val results = vm.state.value.replacing!!.results
        assertFalse(results.any { it.id == 100_000L })  // never itself
        vm.onReplaceTarget(results.first { it.id == 115L })
        vm.onConfirmReplace()

        assertEquals(listOf(100_000L to 115L), repository.replaced)
        assertEquals(listOf("Hafer-Mix"), vm.state.value.items.map { it.name })
    }

    @Test
    fun anIngredientThatWasReallyADishIsReplacedByTheDish() {
        // User 25.11.2026: "Feigen-Datteln-Marmelade" was created as an ingredient and as a dish.
        val vm = viewModel()
        vm.onDelete(repository.items.single { it.name == "Kekse" })
        vm.onReplaceQuery("feigen")
        val dish = vm.state.value.replacing!!.dishResults.single()
        vm.onReplaceTargetDish(dish)
        assertTrue(vm.state.value.replacing!!.canReplace)
        vm.onConfirmReplace()

        assertEquals(listOf(Triple(100_000L, 7L, "Feigen-Datteln-Marmelade")), repository.replacedByDish)
        assertTrue(repository.replaced.isEmpty())
    }
}
