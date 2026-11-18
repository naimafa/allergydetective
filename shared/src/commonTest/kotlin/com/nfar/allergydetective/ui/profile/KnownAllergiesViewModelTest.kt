package com.nfar.allergydetective.ui.profile

import com.nfar.allergydetective.domain.diary.CategoryInfo
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.PropertyInfo
import com.nfar.allergydetective.domain.profile.ProfileRepository
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class KnownAllergiesViewModelTest {

    private class FakeProfile : ProfileRepository {
        var completedAt: Long? = null
        var allergies = setOf<Long>()
        override suspend fun isOnboardingCompleted() = completedAt != null
        override suspend fun completeOnboarding(atMillis: Long) {
            completedAt = atMillis
        }
        override suspend fun knownAllergyPropertyIds() = allergies
        override suspend fun saveKnownAllergies(propertyIds: Set<Long>) {
            allergies = propertyIds
        }
    }

    private class FakeCatalog : CustomIngredientRepository {
        override suspend fun categories() = emptyList<CategoryInfo>()
        override suspend fun properties() = listOf(PropertyInfo(103, "lactose"), PropertyInfo(201, "peanut"))
        override suspend fun categoryOf(ingredientId: Long): Long? = null
        override suspend fun propertiesOf(ingredientId: Long) = emptySet<Long>()
        override suspend fun create(name: String, categoryId: Long, baseIngredientId: Long?, propertyIds: Set<Long>) =
            IngredientInfo(0, null, name)
    }

    private val profile = FakeProfile()
    private val now = Instant.parse("2026-11-17T12:00:00Z")

    private fun viewModel() = KnownAllergiesViewModel(
        profile = profile,
        catalog = FakeCatalog(),
        clock = AppClock(clock = object : Clock { override fun now() = now }),
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun savingStoresAllergiesAndCompletesTheOnboarding() {
        val vm = viewModel()
        vm.onToggle(201)
        vm.onSave()

        assertEquals(setOf(201L), profile.allergies)
        assertEquals(now.toEpochMilliseconds(), profile.completedAt)
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun noAllergiesIsAValidAnswer() {
        viewModel().onSave()

        assertTrue(profile.allergies.isEmpty())
        assertNotNull(profile.completedAt)
    }

    @Test
    fun editingLaterKeepsTheFirstCompletionTime() {
        profile.completedAt = 42L
        profile.allergies = setOf(103L)
        val vm = viewModel()

        assertEquals(setOf(103L), vm.state.value.selected)
        vm.onToggle(103)
        vm.onSave()

        assertTrue(profile.allergies.isEmpty())
        assertEquals(42L, profile.completedAt)
    }
}
