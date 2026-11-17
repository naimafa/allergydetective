package com.nfar.allergydetective.di

import com.nfar.allergydetective.AppStartup
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
import com.nfar.allergydetective.data.db.dao.SymptomDao
import com.nfar.allergydetective.data.diary.RoomSymptomRepository
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.ui.symptom.LogSymptomViewModel
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

    single<SymptomDao> { get<AppDatabase>().symptomDao() }
    single<SymptomRepository> { RoomSymptomRepository(dao = get()) }
    viewModelOf(::LogSymptomViewModel)
    single<DisplayNameResolver> { ResourceDisplayNameResolver() }
    single { AnalysisEngine() }
    single { AppStartup(seeder = get(), scope = get(AppScope)) }
}
