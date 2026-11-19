package com.nfar.allergydetective.ui.daycheck

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
class DayCheckViewModelTest {

    private val clock = AppClock(
        clock = object : Clock { override fun now() = Instant.parse("2026-11-17T19:00:00Z") },
        zone = { TimeZone.of("Europe/Berlin") },
    )
    private val today = clock.today()

    private class FakeRepository : DayCheckRepository {
        val checks = mutableMapOf<Long, DayCheckData>()
        val symptomTimes = mutableListOf<Long>()

        override suspend fun load(epochDay: Long) = checks[epochDay]
        override suspend fun lifestyleFactors() = listOf(FactorInfo(1, "sport"), FactorInfo(3, "medication"))
        override suspend fun symptomCount(fromMillis: Long, toMillis: Long) = symptomTimes.count { it in fromMillis until toMillis }
        override suspend fun save(check: DayCheckData) {
            checks[check.epochDay] = check
        }
        override suspend fun confirmedDays(days: List<Long>) = days.filter { checks[it]?.isConfirmed == true }.toSet()
        override suspend fun confirmedDayCount() = checks.values.count { it.isConfirmed }
    }

    private val repository = FakeRepository()

    private fun viewModel() = DayCheckViewModel(repository, clock)

    private fun confirm(day: Long) {
        repository.checks[day] = DayCheckData(day, status = DayCheckStatus.COMPLETE)
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun todayIsOfferedFirstAndOpenPastDaysAreListed() {
        confirm(today - 2)

        val state = viewModel().state.value

        assertEquals(listOf(today, today - 1, today - 2), state.days)
        // Preselecting an open past day made users confirm the wrong day (self-test 17.11.).
        assertEquals(today, state.check.epochDay)
        assertEquals(setOf(today - 2), state.confirmedDays)
        assertEquals(listOf(today - 1), state.openPastDays)
    }

    @Test
    fun todayIsOfferedWhenThePastDaysAreConfirmed() {
        confirm(today - 1)
        confirm(today - 2)

        assertEquals(today, viewModel().state.value.check.epochDay)
    }

    @Test
    fun newDayStartsWithSevenHoursOfSleep() {
        confirm(today - 1)
        confirm(today - 2)

        val check = viewModel().state.value.check

        assertEquals(7f, check.sleepHours)
        assertEquals(DayCheckStatus.OPEN, check.status)
    }

    @Test
    fun withSymptomsFinishingCompletesTheDayWithoutAsking() {
        confirm(today - 1)
        confirm(today - 2)
        repository.symptomTimes += clock.startOfDayMillis(today) + 3_600_000L
        val vm = viewModel()

        vm.onFinish()

        assertFalse(vm.state.value.askNoSymptoms)
        assertEquals(DayCheckStatus.COMPLETE, repository.checks[today]?.status)
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun withoutSymptomsFinishingAsksFirst() {
        confirm(today - 1)
        confirm(today - 2)
        val vm = viewModel()

        vm.onFinish()
        assertTrue(vm.state.value.askNoSymptoms)
        assertTrue(repository.checks[today] == null)

        // "I had some": the day stays open, a symptom gets logged, then back here.
        vm.onNoSymptomsDismissed()
        repository.symptomTimes += clock.startOfDayMillis(today) + 3_600_000L
        vm.onResume()
        vm.onFinish()

        assertEquals(DayCheckStatus.COMPLETE, repository.checks[today]?.status)
    }

    @Test
    fun answersAreSavedRightAwayAndTheDayStaysOpen() {
        // Self-test 19.11.: "add later" was unclear; leaving the screen keeps everything.
        confirm(today - 1)
        confirm(today - 2)
        val vm = viewModel()

        vm.onStressChange(3)

        assertEquals(DayCheckData(today, sleepHours = 7f, stress = 3, status = DayCheckStatus.OPEN), repository.checks[today])
        assertFalse(vm.state.value.saved)
    }

    @Test
    fun changingAConfirmedDayKeepsItConfirmed() {
        repository.checks[today] = DayCheckData(today, stress = 2, status = DayCheckStatus.COMPLETE)
        confirm(today - 1)
        confirm(today - 2)
        val vm = viewModel()

        vm.onStressChange(4)

        assertEquals(DayCheckStatus.COMPLETE, repository.checks[today]?.status)
    }

    @Test
    fun allAnswersAreSaved() {
        confirm(today - 1)
        confirm(today - 2)
        val vm = viewModel()

        vm.onWellbeingChange(4)
        vm.onSleepHoursChange(-1.5f)
        vm.onSleepQualityChange(2)
        vm.onStressChange(5)
        vm.onFactorToggle(1)
        vm.onFactorToggle(3)
        vm.onFactorToggle(1)
        vm.onFinish()
        vm.onConfirmNoSymptoms()

        assertEquals(
            DayCheckData(today, wellbeing = 4, sleepHours = 5.5f, sleepQuality = 2, stress = 5, status = DayCheckStatus.NO_SYMPTOMS, factorIds = setOf(3)),
            repository.checks[today],
        )
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun existingCheckIsLoadedForEditing() {
        confirm(today - 2)
        repository.checks[today - 1] = DayCheckData(today - 1, wellbeing = 2, status = DayCheckStatus.OPEN, factorIds = setOf(1))
        val vm = viewModel()
        vm.onDaySelected(today - 1)

        assertEquals(2, vm.state.value.check.wellbeing)
        assertEquals(setOf(1L), vm.state.value.check.factorIds)

        vm.onDaySelected(today)
        assertEquals(today, vm.state.value.check.epochDay)
        assertEquals(null, vm.state.value.check.wellbeing)
    }
}
