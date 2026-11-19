package com.nfar.allergydetective.ui.symptom

import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.domain.diary.LoggedSymptom
import com.nfar.allergydetective.domain.diary.SymptomInfo
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.SymptomDayPart
import com.nfar.allergydetective.ui.common.ExactTime
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
class LogSymptomViewModelTest {

    private data class Saved(val symptomId: Long, val startedAt: Long, val offset: Int, val intensity: Int, val note: String?)

    private val bloating = SymptomInfo(1002, "bloating", "digestive", isRedFlag = false)
    private val hives = SymptomInfo(2003, "hives", "skin", isRedFlag = false)
    private val swelling = SymptomInfo(2004, "swelling", "skin", isRedFlag = true)
    private val headache = SymptomInfo(3001, "headache", "head", isRedFlag = false)
    private val now = Instant.parse("2026-11-17T12:00:00Z")

    private inner class FakeRepository : SymptomRepository {
        val saved = mutableListOf<Saved>()
        val deleted = mutableListOf<Long>()

        override suspend fun symptoms() = listOf(headache, bloating, hives, swelling)
        override suspend fun mostUsedSymptomIds(limit: Int) =
            saved.groupingBy { it.symptomId }.eachCount().entries.sortedByDescending { it.value }.map { it.key }.take(limit)
        override suspend fun saveEntry(symptomId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?): Long {
            saved += Saved(symptomId, startedAt, tzOffsetMin, intensity, note)
            return saved.size.toLong()
        }
        override suspend fun deleteEntry(entryId: Long) {
            deleted += entryId
        }
        var lastMeal: Long? = null
        override suspend fun lastMealAt(beforeMillis: Long) = lastMeal?.takeIf { it <= beforeMillis }
        var sickDays = emptySet<Long>()
        val relations = mutableMapOf<Long, IllnessRelation?>()
        override suspend fun isSickDay(epochDay: Long) = epochDay in sickDays
        override suspend fun setIllnessRelation(entryId: Long, relation: IllnessRelation?) {
            relations[entryId] = relation
        }

        // Entry id = position in [saved] + 1.
        override suspend fun entry(entryId: Long) = saved.getOrNull(entryId.toInt() - 1)?.let {
            LoggedSymptom(entryId, it.symptomId, it.startedAt, it.intensity, it.note, relations[entryId])
        }
        override suspend fun updateEntry(entryId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?) {
            saved[entryId.toInt() - 1] = saved[entryId.toInt() - 1].copy(startedAt = startedAt, offset = tzOffsetMin, intensity = intensity, note = note)
        }
    }

    @Test
    fun loggedComplaintIsCorrectedInPlace() {
        // Feedback 19.11.: complaints could only be deleted, not corrected.
        val clock = AppClock(clock = object : Clock { override fun now() = now }, zone = { TimeZone.of("Europe/Berlin") })
        repository.saved += Saved(headache.id, clock.millisAt(clock.today() - 1, 9, 0), 60, 2, "leicht")
        val vm = viewModel()

        vm.startEditing(1L)
        assertEquals(ExactTime(1, 9, 0), vm.state.value.draft?.exactTime)
        assertEquals(2, vm.state.value.draft?.intensity)
        // Yesterday: parts of the day, but no "now" or "after the meal".
        assertFalse(SymptomDayPart.NOW in vm.dayParts())
        assertTrue(SymptomDayPart.MORNING in vm.dayParts())

        vm.onIntensityChange(4)
        vm.onExactTimeChange(ExactTime(1, 18, 30))
        vm.onSaveDraft()

        assertEquals(1, repository.saved.size)
        assertEquals(Saved(headache.id, clock.millisAt(clock.today() - 1, 18, 30), 60, 4, "leicht"), repository.saved.single())
        assertTrue(vm.state.value.finished)
    }

    @Test
    fun complaintCanBeAddedForYesterdayFromTheHistory() {
        val clock = AppClock(clock = object : Clock { override fun now() = now }, zone = { TimeZone.of("Europe/Berlin") })
        val vm = viewModel()

        vm.startForDay(1)
        vm.onSymptomClick(bloating)
        assertEquals(SymptomDayPart.MIDDAY, vm.state.value.draft?.dayPart)
        vm.onSaveDraft()

        // Midday of yesterday (13:00).
        assertEquals(clock.millisAt(clock.today() - 1, 13, 0), repository.saved.single().startedAt)
    }

    @Test
    fun onASickDayTheRelationToTheIllnessIsAskedAndSaved() {
        val clock = AppClock(clock = object : Clock { override fun now() = now }, zone = { TimeZone.of("Europe/Berlin") })
        repository.sickDays = setOf(clock.today())
        val vm = viewModel()

        vm.onSymptomClick(headache)
        assertTrue(vm.state.value.draftOnSickDay)
        assertEquals(IllnessRelation.UNKNOWN, vm.state.value.draft?.illnessRelation)

        vm.onIllnessRelationChange(IllnessRelation.RELATED)
        vm.onSaveDraft()

        assertEquals<Map<Long, IllnessRelation?>>(mapOf(1L to IllnessRelation.RELATED), repository.relations)
    }

    @Test
    fun onAHealthyDayNothingIsAsked() {
        val vm = viewModel()

        vm.onSymptomClick(headache)
        assertFalse(vm.state.value.draftOnSickDay)
        vm.onSaveDraft()

        assertTrue(repository.relations.isEmpty())
    }

    private val repository = FakeRepository()

    private fun viewModel() = LogSymptomViewModel(
        repository = repository,
        clock = AppClock(clock = object : Clock { override fun now() = now }, zone = { TimeZone.of("Europe/Berlin") }),
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun symptomsAreGroupedInCatalogOrder() {
        val groups = viewModel().state.value.groups

        assertEquals(listOf("digestive", "skin", "head"), groups.map { it.first })
        assertEquals(listOf(hives, swelling), groups[1].second)
    }

    @Test
    fun overlaySavesIntensityTimeAndNote() {
        val vm = viewModel()
        vm.onSymptomClick(bloating)
        assertEquals(3, vm.state.value.draft?.intensity)

        vm.onIntensityChange(5)
        vm.onDayPartChange(SymptomDayPart.MORNING)
        vm.onNoteChange("  after lunch ")
        vm.onSaveDraft()

        // Morning = 07:00 in Berlin = 06:00 UTC
        assertEquals(Saved(1002, Instant.parse("2026-11-17T06:00:00Z").toEpochMilliseconds(), 60, 5, "after lunch"), repository.saved.single())
        assertNull(vm.state.value.draft)
        assertEquals(1L, vm.state.value.savedEntryId)
    }

    @Test
    fun onlyDayPartsThatAlreadyStartedAreOffered() {
        // 13:00 in Berlin, no meal yet: no afternoon, evening or "after the meal".
        assertEquals(
            listOf(SymptomDayPart.NOW, SymptomDayPart.MORNING, SymptomDayPart.MIDDAY, SymptomDayPart.ALL_DAY),
            viewModel().dayParts(),
        )
    }

    @Test
    fun afterTheMealIsHalfAnHourAfterTodaysLastMeal() {
        repository.lastMeal = Instant.parse("2026-11-17T11:00:00Z").toEpochMilliseconds()
        val vm = viewModel()
        assertTrue(SymptomDayPart.AFTER_MEAL in vm.dayParts())

        vm.onSymptomClick(bloating)
        vm.onDayPartChange(SymptomDayPart.AFTER_MEAL)
        vm.onSaveDraft()

        assertEquals(Instant.parse("2026-11-17T11:30:00Z").toEpochMilliseconds(), repository.saved.single().startedAt)
    }

    @Test
    fun yesterdaysMealDoesNotCountForAfterTheMeal() {
        repository.lastMeal = Instant.parse("2026-11-16T18:00:00Z").toEpochMilliseconds()

        assertFalse(SymptomDayPart.AFTER_MEAL in viewModel().dayParts())
    }

    @Test
    fun pickedTimeIsUsedForTheSymptom() {
        val vm = viewModel()
        vm.onDayChange(2)
        vm.onSymptomClick(bloating)
        vm.onExactTimeChange(ExactTime(daysAgo = 2, hour = 7, minute = 15))
        vm.onSaveDraft()

        assertEquals(Instant.parse("2026-11-15T06:15:00Z").toEpochMilliseconds(), repository.saved.single().startedAt)
    }

    @Test
    fun blankNoteIsStoredAsNull() {
        val vm = viewModel()
        vm.onSymptomClick(bloating)
        vm.onNoteChange("   ")
        vm.onSaveDraft()

        assertNull(repository.saved.single().note)
    }

    @Test
    fun longPressSavesAtOnceWithDefaults() {
        val vm = viewModel()

        vm.onSymptomLongClick(headache)

        assertEquals(Saved(3001, now.toEpochMilliseconds(), 60, 3, null), repository.saved.single())
        assertEquals(listOf(headache), vm.state.value.favorites)
    }

    @Test
    fun emergencySymptomShowsTheNoticeBeforeTheOverlay() {
        val vm = viewModel()

        vm.onSymptomClick(swelling)
        assertEquals(swelling, vm.state.value.emergencyNoticeFor)
        assertNull(vm.state.value.draft)

        vm.onEmergencyNoticeContinue()
        assertNull(vm.state.value.emergencyNoticeFor)
        assertEquals(swelling, vm.state.value.draft?.symptom)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun emergencyQuickLogSavesOnlyAfterTheNotice() {
        val vm = viewModel()

        vm.onSymptomLongClick(swelling)
        assertTrue(repository.saved.isEmpty())

        vm.onEmergencyNoticeContinue()
        assertEquals(2004L, repository.saved.single().symptomId)
        assertNull(vm.state.value.draft)
    }

    @Test
    fun dismissingTheNoticeSavesNothing() {
        val vm = viewModel()
        vm.onSymptomLongClick(swelling)

        vm.onEmergencyNoticeDismiss()

        assertNull(vm.state.value.emergencyNoticeFor)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun undoDeletesTheEntry() {
        val vm = viewModel()
        vm.onSymptomLongClick(headache)

        vm.onUndo(1L)
        vm.onSavedShown()

        assertEquals(listOf(1L), repository.deleted)
        assertNull(vm.state.value.savedEntryId)
    }
}
