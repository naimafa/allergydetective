package com.nfar.allergydetective.ui.favorites

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository
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
class DishEditorViewModelTest {

    private val lentils = IngredientInfo(1001, "lentils", null)
    private val tomato = IngredientInfo(701, "tomato", null)
    private val carrot = IngredientInfo(702, "carrot", null)
    private val names = mapOf("lentils" to "Linsen", "tomato" to "Tomate", "carrot" to "Karotte")

    private inner class FakeMeals : MealRepository {
        val favorites = mutableListOf<Pair<String, List<MealItem>>>()
        override suspend fun ingredients() = listOf(lentils, tomato, carrot)
        override suspend fun synonyms(language: String) = emptyMap<Long, List<String>>()
        override suspend fun recentIngredientIds(limit: Int) = emptyList<Long>()
        override suspend fun ingredientIdsWithKnownAllergy() = emptySet<Long>()
        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?) = 0L
        override suspend fun deleteMeal(mealId: Long) = Unit
        override suspend fun favorites() = emptyList<FavoriteInfo>()
        override suspend fun saveFavorite(name: String, items: List<MealItem>): Long {
            favorites += name to items
            return favorites.size.toLong()
        }
        override suspend fun deleteFavorite(favoriteId: Long) = Unit
    }

    private val meals = FakeMeals()

    private fun viewModel() = DishEditorViewModel(meals, names = { _, key -> names.getValue(key) }, language = { "de" })

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun lentilSoupIsSavedAsFavorite() {
        val vm = viewModel()
        vm.start()
        vm.onNameChange(" Linsensuppe ")
        assertFalse(vm.state.value.canSave)

        vm.onQueryChange("lins")
        assertEquals(listOf(lentils), vm.state.value.results)
        vm.onAdd(lentils)
        vm.onAdd(tomato)
        vm.onAdd(carrot)
        vm.onAdd(tomato)
        vm.onRemove(carrot)
        assertEquals("", vm.state.value.query)
        assertTrue(vm.state.value.canSave)

        vm.onSave()

        assertEquals(
            "Linsensuppe" to listOf(MealItem(1001, Portion.MEDIUM), MealItem(701, Portion.MEDIUM)),
            meals.favorites.single(),
        )
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun startClearsTheForm() {
        val vm = viewModel()
        vm.onNameChange("Alt")
        vm.onAdd(lentils)

        vm.start()

        assertEquals(DishEditorState(), vm.state.value)
    }
}
