package com.nfar.allergydetective.di

import com.nfar.allergydetective.AppStartup
import com.nfar.allergydetective.ui.profile.ReminderViewModel
import com.nfar.allergydetective.data.db.AppDatabase
import com.nfar.allergydetective.data.db.createAppDatabase
import com.nfar.allergydetective.data.analysis.RoomAnalysisRepository
import com.nfar.allergydetective.data.db.dao.AnalysisDao
import com.nfar.allergydetective.data.db.dao.SeedDao
import com.nfar.allergydetective.data.seed.DatabaseSeeder
import com.nfar.allergydetective.domain.analysis.AnalysisEngine
import com.nfar.allergydetective.domain.analysis.AnalysisRepository
import androidx.compose.ui.text.intl.Locale
import com.nfar.allergydetective.data.db.dao.MealDao
import com.nfar.allergydetective.data.diary.RoomMealRepository
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.ui.meal.LogMealViewModel
import com.nfar.allergydetective.data.db.dao.IngredientDao
import com.nfar.allergydetective.data.diary.RoomCustomIngredientRepository
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.ui.meal.CustomIngredientViewModel
import com.nfar.allergydetective.data.db.dao.ProfileDao
import com.nfar.allergydetective.data.profile.RoomProfileRepository
import com.nfar.allergydetective.domain.profile.ProfileRepository
import com.nfar.allergydetective.ui.StartViewModel
import com.nfar.allergydetective.data.db.dao.HistoryDao
import com.nfar.allergydetective.data.diary.RoomHistoryRepository
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.ui.favorites.DishEditorViewModel
import com.nfar.allergydetective.ui.favorites.FavoritesViewModel
import com.nfar.allergydetective.ui.history.HistoryViewModel
import com.nfar.allergydetective.ui.profile.KnownAllergiesViewModel
import com.nfar.allergydetective.data.db.dao.SymptomDao
import com.nfar.allergydetective.data.diary.RoomSymptomRepository
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.ui.symptom.LogSymptomViewModel
import com.nfar.allergydetective.data.db.dao.DayCheckDao
import com.nfar.allergydetective.data.diary.RoomDayCheckRepository
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.ui.daycheck.DayCheckViewModel
import com.nfar.allergydetective.ui.home.HomeViewModel
import com.nfar.allergydetective.data.analysis.RoomCandidateCatalog
import com.nfar.allergydetective.domain.analysis.CandidateCatalog
import com.nfar.allergydetective.ui.results.ResultsViewModel
import com.nfar.allergydetective.ui.profile.ExportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.ResourceDisplayNameResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

/** Context for blocking work such as database queries. Injected so tests can replace it. */
val IoContext = named("ioContext")

/** Context for CPU-heavy work such as the analysis. */
val ComputeContext = named("computeContext")

/** Scope that lives as long as the process, for work that must not be tied to a screen. */
val AppScope = named("appScope")

/**
 * Objects shared by Android and iOS. The platform module adds what differs,
 * for now only the Room database builder.
 */
val appModule = module {
    single<CoroutineContext>(IoContext) { Dispatchers.IO }
    single<CoroutineScope>(AppScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<AppDatabase> { createAppDatabase(builder = get(), ioContext = get(IoContext)) }
    single<SeedDao> { get<AppDatabase>().seedDao() }
    single<AnalysisDao> { get<AppDatabase>().analysisDao() }
    single { DatabaseSeeder(dao = get()) }
    single<AnalysisRepository> { RoomAnalysisRepository(dao = get()) }
    single { AppClock() }
    single<LanguageProvider> { LanguageProvider { Locale.current.language } }

    single<MealDao> { get<AppDatabase>().mealDao() }
    single<MealRepository> { RoomMealRepository(dao = get()) }
    viewModelOf(::LogMealViewModel)

    single<IngredientDao> { get<AppDatabase>().ingredientDao() }
    single<CustomIngredientRepository> { RoomCustomIngredientRepository(dao = get()) }
    viewModelOf(::CustomIngredientViewModel)

    single<ProfileDao> { get<AppDatabase>().profileDao() }
    single<ProfileRepository> { RoomProfileRepository(dao = get()) }
    viewModelOf(::KnownAllergiesViewModel)
    viewModelOf(::StartViewModel)

    single<HistoryDao> { get<AppDatabase>().historyDao() }
    single<HistoryRepository> { RoomHistoryRepository(dao = get(), mealDao = get(), symptomDao = get()) }
    viewModelOf(::HistoryViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::DishEditorViewModel)

    single<SymptomDao> { get<AppDatabase>().symptomDao() }
    single<SymptomRepository> { RoomSymptomRepository(dao = get()) }
    viewModelOf(::LogSymptomViewModel)

    single<DayCheckDao> { get<AppDatabase>().dayCheckDao() }
    single<DayCheckRepository> { RoomDayCheckRepository(dao = get()) }
    viewModelOf(::DayCheckViewModel)
    viewModelOf(::HomeViewModel)

    single<CoroutineContext>(ComputeContext) { Dispatchers.Default }
    single<CandidateCatalog> { RoomCandidateCatalog(analysisDao = get(), mealDao = get(), dayCheckDao = get()) }
    viewModel {
        ResultsViewModel(
            repository = get(),
            engine = get(),
            catalog = get(),
            clock = get(),
            computeContext = get(ComputeContext),
        )
    }
    viewModel {
        ExportViewModel(
            repository = get(),
            engine = get(),
            catalog = get(),
            clock = get(),
            computeContext = get(ComputeContext),
        )
    }
    single<DisplayNameResolver> { ResourceDisplayNameResolver() }
    single { AnalysisEngine() }
    single { AppStartup(seeder = get(), scope = get(AppScope), settings = get(), reminders = get()) }
    viewModelOf(::ReminderViewModel)
}
