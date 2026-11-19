package com.nfar.allergydetective.ui.home

import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.FactorInfo
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HomeViewModelTest {

    private val clock = AppClock(
        clock = object : Clock { override fun now() = Instant.parse("2026-11-19T19:00:00Z") },
        zone = { TimeZone.of("Europe/Berlin") },
    )
    private val today = clock.today()

    private class FakeRepository : DayCheckRepository {
        val checks = mutableMapOf<Long, DayCheckData>()

        override suspend fun load(epochDay: Long) = checks[epochDay]
        override suspend fun lifestyleFactors() = emptyList<FactorInfo>()
        override suspend fun symptomCount(fromMillis: Long, toMillis: Long) = 0
        override suspend fun save(check: DayCheckData) {
            checks[check.epochDay] = check
        }
        override suspend fun confirmedDays(days: List<Long>) = days.filter { checks[it]?.isConfirmed == true }.toSet()
        override suspend fun confirmedDayCount() = checks.values.count { it.isConfirmed }
    }

    private val repository = FakeRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun refreshedState(): HomeState = HomeViewModel(repository, clock).apply { refresh() }.state.value

    @Test
    fun todayIsOpenUntilItIsConfirmed() {
        repository.checks[today - 1] = DayCheckData(today - 1, status = DayCheckStatus.COMPLETE)
        repository.checks[today] = DayCheckData(today, sleepHours = 7f)

        val state = refreshedState()

        assertTrue(state.todayOpen)
        assertEquals(1, state.confirmedDays)
    }

    @Test
    fun confirmedTodayIsNotOpen() {
        repository.checks[today] = DayCheckData(today, status = DayCheckStatus.NO_SYMPTOMS)

        assertFalse(refreshedState().todayOpen)
    }

    @Test
    fun quoteChangesDailyAndRepeatsAfterThePool() {
        assertEquals(0, HomeViewModel.quoteIndex(20_400, 40))
        assertEquals(1, HomeViewModel.quoteIndex(20_401, 40))
        assertEquals(0, HomeViewModel.quoteIndex(20_440, 40))
    }

    @Test
    fun emptyPoolDoesNotCrash() {
        assertEquals(0, HomeViewModel.quoteIndex(20_401, 0))
    }
}
