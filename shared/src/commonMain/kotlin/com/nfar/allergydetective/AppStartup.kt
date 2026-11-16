package com.nfar.allergydetective

import com.nfar.allergydetective.data.seed.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Work that runs once per process start, off the main thread. */
class AppStartup(
    private val seeder: DatabaseSeeder,
    private val scope: CoroutineScope,
) {
    /** Fills the reference tables on the very first start. Returns the job so callers can wait for it. */
    fun start(): Job = scope.launch { seeder.seedIfEmpty() }
}
