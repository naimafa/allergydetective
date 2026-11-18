package com.nfar.allergydetective.ui.results

import com.nfar.allergydetective.domain.analysis.AnalysisEngine
import com.nfar.allergydetective.domain.analysis.AnalysisRepository
import com.nfar.allergydetective.domain.analysis.CandidateKeys
import com.nfar.allergydetective.domain.analysis.fixtures.DiaryBuilder
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BLOATING
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BREAD
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.FRUCTANS
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.GLUTEN
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.LACTOSE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK_PROTEIN
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.RICE
import com.nfar.allergydetective.domain.analysis.model.AnalysisInput
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.diary.IngredientInfo
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResultsViewModelTest {

    private val milk = IngredientInfo(MILK, "cow_milk", null)
    private val bread = IngredientInfo(BREAD, "white_bread", null)
    private val keys = CandidateKeys(
        ingredients = listOf(milk, bread, IngredientInfo(RICE, "rice", null)).associateBy { it.id },
        properties = mapOf(LACTOSE to "lactose", MILK_PROTEIN to "milk_protein", GLUTEN to "gluten", FRUCTANS to "fructans"),
        factors = emptyMap(),
    )

    /** Bread every morning, milk every third afternoon followed by bloating. */
    private val input: AnalysisInput = DiaryBuilder().apply {
        days(21) { i ->
            meal(8, BREAD)
            if (i % 3 == 0) {
                meal(15, MILK)
                symptom(16, 30, BLOATING, intensity = 4)
            }
        }
    }.build(reference = TestCatalog.reference())

    private fun viewModel(reference: AnalysisInput = input) = ResultsViewModel(
        repository = AnalysisRepository { _, _ -> reference },
        engine = AnalysisEngine(),
        catalog = { keys },
        clock = AppClock(),
        computeContext = Dispatchers.Unconfined,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun suspectsAreListedWithTheirReason() {
        val state = viewModel().state.value

        val lactose = state.items.single { it.label == CandidateLabel.Property("lactose") }
        assertTrue(lactose.level >= EvidenceLevel.SUSPICION)
        assertEquals(7, lactose.exposures)
        assertEquals(7, lactose.withComplaints)
        // 21 bread meals without lactose; bloating is outside their window
        assertEquals(21, lactose.otherExposures)
        assertEquals(0, lactose.otherWithComplaints)
        assertEquals(21, state.confirmedDays)
    }

    @Test
    fun candidatesWithoutATraceAreNotListed() {
        val state = viewModel().state.value

        assertTrue(state.items.none { it.label == CandidateLabel.Ingredient(bread) })
        assertTrue(state.items.all { it.level >= EvidenceLevel.TRACE })
    }

    @Test
    fun indistinguishableCandidatesAreGrouped() {
        val group = viewModel().state.value.groups.single()

        assertEquals(
            setOf(CandidateLabel.Property("lactose"), CandidateLabel.Property("milk_protein"), CandidateLabel.Ingredient(milk)),
            group.toSet(),
        )
    }

    @Test
    fun toleratedBreadIsOnTheSafeList() {
        // Without any complaints: in the milk diary the long gluten window (48 h) still overlaps the bloating.
        val calmDiary = DiaryBuilder().apply { days(14) { meal(8, BREAD) } }.build(reference = TestCatalog.reference())

        assertEquals(listOf<CandidateLabel>(CandidateLabel.Ingredient(bread)), viewModel(calmDiary).state.value.safeIngredients)
    }

    @Test
    fun knownAllergyIsReportedSeparately() {
        val withAllergy = input.copy(reference = TestCatalog.reference(knownAllergyPropertyIds = setOf(LACTOSE)))

        val state = viewModel(withAllergy).state.value

        assertEquals(listOf<CandidateLabel>(CandidateLabel.Ingredient(milk)), state.knownAllergyIngredients)
        assertTrue(state.items.none { it.label == CandidateLabel.Property("lactose") })
    }
}
