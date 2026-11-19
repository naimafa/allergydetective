package com.nfar.allergydetective.ui.sick

import com.nfar.allergydetective.domain.diary.SickPeriod
import com.nfar.allergydetective.domain.diary.SickPeriodRepository
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class SickPeriodsViewModelTest {

    private val clock = AppClock(clock = object : Clock { override fun now() = Instant.parse("2026-11-18T12:00:00Z") }, zone = { TimeZone.UTC })
    private val today = clock.today()

    private class FakeRepository : SickPeriodRepository {
        val periods = mutableListOf<SickPeriod>()
        override suspend fun periods() = periods.sortedByDescending { it.startDay }
        override suspend fun add(startDay: Long, endDay: Long?): Long {
            val id = periods.size + 1L
            periods += SickPeriod(id, startDay, endDay)
            return id
        }
        override suspend fun setEnd(id: Long, endDay: Long?) {
            periods.replaceAll { if (it.id == id) it.copy(endDay = endDay) else it }
        }
        override suspend fun delete(id: Long) {
            periods.removeAll { it.id == id }
        }
        override suspend fun update(id: Long, startDay: Long, endDay: Long?) {
            periods.replaceAll { if (it.id == id) SickPeriod(id, startDay, endDay) else it }
        }
    }

    @Test
    fun periodCanBeCorrectedFromTheHistory() {
        val vm = SickPeriodsViewModel(repository, clock)
        vm.onAdd(today - 5, today - 3)

        vm.onUpdate(1, today - 6, today + 2)

        // Same id, future end cut to today.
        assertEquals(SickPeriod(1, today - 6, today), repository.periods.single())
    }

    private val repository = FakeRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun sinusitisSinceYesterdayRunsUntilWellAgain() {
        val vm = SickPeriodsViewModel(repository, clock)

        vm.onAdd(today - 1, null)
        assertEquals(today - 1, vm.state.value.ongoing?.startDay)
        assertTrue(vm.state.value.isSick(today))

        vm.onRecovered()
        // Well again today: yesterday was the last sick day.
        assertNull(vm.state.value.ongoing)
        assertEquals(SickPeriod(1, today - 1, today - 1), repository.periods.single())
        assertFalse(vm.state.value.isSick(today))
    }

    @Test
    fun futureEndIsCutToTodayAndEndNeverBeforeStart() {
        val vm = SickPeriodsViewModel(repository, clock)

        vm.onAdd(today - 3, today + 5)
        vm.onAdd(today - 10, today - 20)

        assertEquals(listOf(SickPeriod(1, today - 3, today), SickPeriod(2, today - 10, today - 10)), repository.periods)
    }

    @Test
    fun aNewOpenPeriodClosesTheOldOne() {
        val vm = SickPeriodsViewModel(repository, clock)
        vm.onAdd(today - 6, null)

        vm.onAdd(today - 1, null)

        assertEquals(today - 2, repository.periods.first { it.id == 1L }.endDay)
        assertEquals(2L, vm.state.value.ongoing?.id)
    }
}
