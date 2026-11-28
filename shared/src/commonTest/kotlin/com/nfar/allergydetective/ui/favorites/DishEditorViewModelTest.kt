package com.nfar.allergydetective.ui.favorites

import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
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
        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?, slot: MealSlot?, snapshot: com.nfar.allergydetective.domain.diary.MealSnapshot?) = 0L
        override suspend fun deleteMeal(mealId: Long) = Unit
        val existing = mutableListOf<FavoriteInfo>()
        val deleted = mutableListOf<Long>()
        override suspend fun favorites() = existing.toList()
        override suspend fun ingredientIdsNeedingPreparation() = setOf(702L)
        override suspend fun saveFavorite(name: String, items: List<MealItem>): Long {
            favorites += name to items
            return favorites.size.toLong()
        }
        val updated = mutableListOf<Triple<Long, String, List<MealItem>>>()
        override suspend fun updateFavorite(favoriteId: Long, name: String, items: List<MealItem>) {
            updated += Triple(favoriteId, name, items)
        }
        val loggedMealIds = mutableListOf<Long>()
        val applied = mutableListOf<Triple<Long, List<MealItem>, Set<Long>>>()
        override suspend fun mealIdsWithDish(favoriteId: Long) = loggedMealIds.toList()
        override suspend fun applyDishChange(favoriteId: Long, added: List<MealItem>, removedIngredientIds: Set<Long>, minorIngredientIds: Set<Long>) {
            applied += Triple(favoriteId, added, removedIngredientIds)
        }
        override suspend fun deleteFavorite(favoriteId: Long) {
            deleted += favoriteId
        }
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
        assertFalse(vm.state.value.canSave)
        vm.onPreparationChange(carrot.id, Preparation.COOKED)
        assertEquals("", vm.state.value.query)
        assertTrue(vm.state.value.canSave)

        vm.onSave()

        assertEquals(
            "Linsensuppe" to listOf(
                MealItem(1001, Portion.MEDIUM),
                MealItem(701, Portion.MEDIUM),
                MealItem(702, Portion.MEDIUM, Preparation.COOKED),
            ),
            meals.favorites.single(),
        )
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun anotherDishCanBeFoundAndAddsAllItsIngredients() {
        meals.existing += FavoriteInfo(7, "Gewürzmischung", listOf(MealItem(tomato.id, Portion.SMALL), MealItem(carrot.id, Portion.LARGE, Preparation.COOKED)))
        val vm = viewModel()
        vm.start()
        vm.onAdd(lentils)

        vm.onQueryChange("gewürz")
        assertEquals(listOf("Gewürzmischung"), vm.state.value.dishResults.map { it.name })
        vm.onAddDish(vm.state.value.dishResults.single())

        assertEquals(listOf(lentils, tomato, carrot), vm.state.value.ingredients)
        assertEquals(Portion.LARGE, vm.state.value.portions[carrot.id])
        assertEquals(Preparation.COOKED, vm.state.value.preparations[carrot.id])
        assertEquals("", vm.state.value.query)
    }

    @Test
    fun startClearsTheForm() {
        val vm = viewModel()
        vm.onNameChange("Alt")
        vm.onAdd(lentils)

        vm.start()

        assertEquals(DishEditorState(needsPreparation = setOf(702L)), vm.state.value)
    }

    @Test
    fun sameDishNameAsksToReplace() {
        meals.existing += FavoriteInfo(7, "Linsensuppe", emptyList())
        val vm = viewModel()
        vm.start()

        vm.onNameChange("linsensuppe ")
        assertEquals(7L, vm.state.value.sameNameDish?.id)
        vm.onAdd(lentils)
        vm.onSave()

        assertEquals(listOf(7L), meals.deleted)
        assertEquals("linsensuppe", meals.favorites.single().first)
    }

    @Test
    fun forgottenIngredientIsAddedToAnExistingDish() {
        // Self-test 18.11.: dishes must be editable, not only deletable.
        val soup = FavoriteInfo(7, "Linsensuppe", listOf(MealItem(1001, Portion.LARGE), MealItem(702, Portion.MEDIUM, Preparation.COOKED)))
        meals.existing += soup
        val vm = viewModel()

        vm.start(soup)
        assertEquals(7L, vm.state.value.editingId)
        assertEquals(listOf(lentils, carrot), vm.state.value.ingredients)
        // Its own name is no conflict.
        vm.onNameChange("Linsensuppe")
        assertEquals(null, vm.state.value.sameNameDish)

        vm.onAdd(tomato)
        vm.onSave()

        assertEquals(
            Triple(
                7L,
                "Linsensuppe",
                listOf(
                    MealItem(1001, Portion.LARGE),
                    MealItem(702, Portion.MEDIUM, Preparation.COOKED),
                    MealItem(701, Portion.MEDIUM),
                ),
            ),
            meals.updated.single(),
        )
        assertTrue(meals.favorites.isEmpty())
    }

    // User 28.11.2026: a changed dish is in earlier meals - the user decides whether they change too.
    private val soup = FavoriteInfo(7, "Linsensuppe", listOf(MealItem(1001, Portion.LARGE), MealItem(702, Portion.MEDIUM, Preparation.COOKED)))

    private fun editingSoupInEarlierMeals(): DishEditorViewModel {
        meals.existing += soup
        meals.loggedMealIds += listOf(5L, 6L)
        return viewModel().also { it.start(soup) }
    }

    @Test
    fun anAddedIngredientAsksAboutEarlierMealsBeforeSaving() {
        val vm = editingSoupInEarlierMeals()

        vm.onAdd(tomato)
        vm.onSave()

        assertEquals(2, vm.state.value.askEarlierMeals)
        assertTrue(meals.updated.isEmpty())
        assertFalse(vm.state.value.saved)
    }

    @Test
    fun onlyFromNowSavesTheDishAndLeavesEarlierMealsAlone() {
        val vm = editingSoupInEarlierMeals()
        vm.onAdd(tomato)
        vm.onSave()

        vm.onEarlierMealsChoice(false)

        assertEquals(7L, meals.updated.single().first)
        assertTrue(meals.applied.isEmpty())
        assertEquals(null, vm.state.value.askEarlierMeals)
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun changingEarlierMealsToo_passesTheAddedAndRemovedIngredients() {
        val vm = editingSoupInEarlierMeals()
        vm.onAdd(tomato)
        vm.onRemove(carrot)
        vm.onSave()

        vm.onEarlierMealsChoice(true)

        assertEquals(7L, meals.updated.single().first)
        assertEquals(Triple(7L, listOf(MealItem(701, Portion.MEDIUM)), setOf(702L)), meals.applied.single())
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun dismissingTheQuestionGoesBackToTheFormWithoutSaving() {
        val vm = editingSoupInEarlierMeals()
        vm.onAdd(tomato)
        vm.onSave()

        vm.onEarlierMealsDismiss()

        assertEquals(null, vm.state.value.askEarlierMeals)
        assertFalse(vm.state.value.saving)
        assertFalse(vm.state.value.saved)
        assertTrue(meals.updated.isEmpty())
        assertEquals(listOf(lentils, carrot, tomato), vm.state.value.ingredients)
    }

    @Test
    fun noQuestionWhenOnlyTheNameChangesOrNoMealUsesTheDish() {
        val renamed = editingSoupInEarlierMeals()
        renamed.onNameChange("Linsentopf")
        renamed.onSave()
        assertEquals(null, renamed.state.value.askEarlierMeals)
        assertTrue(renamed.state.value.saved)

        meals.updated.clear()
        meals.loggedMealIds.clear()
        val unused = viewModel().also { it.start(soup) }
        unused.onAdd(tomato)
        unused.onSave()
        assertEquals(null, unused.state.value.askEarlierMeals)
        assertEquals(7L, meals.updated.single().first)
    }

    // User 28.11.2026: a dish from a photo or text.
    @Test
    fun textWithIngredientsIsOfferedAndTheTickedOnesJoinTheDish() {
        val vm = viewModel()
        vm.start()

        vm.onPackageText("Zutaten: Linsen, Tomate, Salz")

        assertEquals(listOf(lentils.id, tomato.id), vm.state.value.packageScan!!.found.map { it.id })
        vm.onPackageConfirm(setOf(tomato.id))
        assertEquals(listOf(tomato), vm.state.value.ingredients)
        assertEquals(null, vm.state.value.packageScan)
    }

    @Test
    fun aNewDishCanStartWithIngredientsReadElsewhereAndReportsItsId() {
        val vm = viewModel()

        vm.start(prefillIngredientIds = listOf(lentils.id, tomato.id, lentils.id))
        assertEquals(listOf(lentils, tomato), vm.state.value.ingredients)
        assertEquals("", vm.state.value.name)
        assertFalse(vm.state.value.canSave)

        vm.onNameChange("Linsentopf")
        vm.onSave()

        assertEquals(1L, vm.state.value.savedId)
        assertEquals("Linsentopf", meals.favorites.single().first)
    }
}
