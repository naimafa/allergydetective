package com.nfar.allergydetective.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Platform-specific objects, e.g. the Room database builder. */
expect val platformModule: Module

/** Starts dependency injection. Call once per process, before anything is injected. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(platformModule, appModule)
}
