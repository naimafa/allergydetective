package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.diary.CategoryInfo
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.PropertyInfo
import com.nfar.allergydetective.domain.diary.nextCustomIngredientId
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CustomIngredientViewModelTest {

    private val hardCheese = IngredientInfo(301, "hard_cheese", null)
    private val oats = IngredientInfo(110, "oats", null)

    private data class Created(val name: String, val categoryId: Long, val baseId: Long?, val propertyIds: Set<Long>)

    private class FakeCustomRepository : CustomIngredientRepository {
        val created = mutableListOf<Created>()
        override suspend fun categories() = listOf(CategoryInfo(2, "dairy"), CategoryInfo(3, "cheese"))
        override suspend fun properties() = listOf(PropertyInfo(101, "histamine"), PropertyInfo(117, "milk_protein"))
        override suspend fun categoryOf(ingredientId: Long) = if (ingredientId == 110L) 1L else 3L
        override suspend fun propertiesOf(ingredientId: Long) = if (ingredientId == 110L) setOf(102L) else setOf(101L, 117L)
        override suspend fun create(name: String, categoryId: Long, baseIngredientId: Long?, propertyIds: Set<Long>): IngredientInfo {
            created += Created(name, categoryId, baseIngredientId, propertyIds)
            return IngredientInfo(100_000, null, name)
        }
    }

    private inner class FakeMeals : MealRepository {
        override suspend fun ingredients() = listOf(hardCheese, oats)
        override suspend fun synonyms(language: String) = emptyMap<Long, List<String>>()
        override suspend fun recentIngredientIds(limit: Int) = emptyList<Long>()
        override suspend fun ingredientIdsWithKnownAllergy() = emptySet<Long>()
        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?, slot: MealSlot?, snapshot: com.nfar.allergydetective.domain.diary.MealSnapshot?) = 0L
        override suspend fun deleteMeal(mealId: Long) = Unit
        override suspend fun favorites() = emptyList<FavoriteInfo>()
        override suspend fun saveFavorite(name: String, items: List<MealItem>) = 0L
        override suspend fun deleteFavorite(favoriteId: Long) = Unit
    }

    private val repository = FakeCustomRepository()

    private fun viewModel() = CustomIngredientViewModel(
        repository = repository,
        meals = FakeMeals(),
        names = { _, key -> if (key == "oats") "Haferflocken" else "Hartkäse" },
        language = { "de" },
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun nameAndCategoryAreRequired() {
        val vm = viewModel()
        vm.start("  Bergkäse ")
        assertEquals("Bergkäse", vm.state.value.name)
        assertFalse(vm.state.value.canCreate)

        vm.onCategoryChange(3)
        assertTrue(vm.state.value.canCreate)
    }

    @Test
    fun componentsGiveCategoryAndAllTheirTriggers() {
        val vm = viewModel()
        vm.start("Dinkelbrötchen vom Bäcker")

        vm.onQueryChange("hart")
        assertEquals(listOf(hardCheese), vm.state.value.results)
        vm.onAddComponent(oats)
        vm.onAddComponent(hardCheese)

        // Category of the first component, triggers of all.
        assertEquals(1L, vm.state.value.categoryId)
        assertEquals(setOf(101L, 102L, 117L), vm.state.value.propertyIds)
        assertTrue(vm.state.value.canCreate)

        vm.onCreate()

        assertEquals(Created("Dinkelbrötchen vom Bäcker", 1, 110, setOf(101L, 102L, 117L)), repository.created.single())
        assertEquals(IngredientInfo(100_000, null, "Dinkelbrötchen vom Bäcker"), vm.state.value.created)
    }

    @Test
    fun removingAComponentUpdatesTheTriggers() {
        val vm = viewModel()
        vm.start("Käsebrot")
        vm.onAddComponent(oats)
        vm.onAddComponent(hardCheese)

        vm.onRemoveComponent(oats)

        assertEquals(3L, vm.state.value.categoryId)
        assertEquals(setOf(101L, 117L), vm.state.value.propertyIds)
    }

    @Test
    fun customIdsStartAtOneHundredThousand() {
        assertEquals(100_000L, nextCustomIngredientId(1208))
        assertEquals(100_000L, nextCustomIngredientId(null))
        assertEquals(100_005L, nextCustomIngredientId(100_004))
    }

    @Test
    fun sameNameOrSynonymCannotBeCreatedAgain() {
        val vm = viewModel()
        vm.start("hartkäse")
        vm.onCategoryChange(3)

        assertEquals(hardCheese, vm.state.value.duplicate)
        assertFalse(vm.state.value.canCreate)

        vm.onUseExisting()
        assertEquals(hardCheese, vm.state.value.created)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun similarNameIsAskedOnceBeforeCreating() {
        val vm = viewModel()
        vm.start("Hartkässe")
        vm.onCategoryChange(3)
        assertEquals(hardCheese, vm.state.value.similar)

        vm.onCreate()
        assertTrue(vm.state.value.askSimilar)
        assertTrue(repository.created.isEmpty())

        vm.onCreate()
        assertEquals("Hartkässe", repository.created.single().name)
    }
}
