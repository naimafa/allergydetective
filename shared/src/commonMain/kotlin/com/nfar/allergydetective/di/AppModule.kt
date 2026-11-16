package com.nfar.allergydetective.di

import com.nfar.allergydetective.AppStartup
import com.nfar.allergydetective.data.db.AppDatabase
import com.nfar.allergydetective.data.db.createAppDatabase
import com.nfar.allergydetective.data.db.dao.SeedDao
import com.nfar.allergydetective.data.seed.DatabaseSeeder
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
    single { DatabaseSeeder(dao = get()) }
    single { AppStartup(seeder = get(), scope = get(AppScope)) }
}
