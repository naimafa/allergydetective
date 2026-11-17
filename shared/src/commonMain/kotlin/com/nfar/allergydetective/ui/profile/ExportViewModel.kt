package com.nfar.allergydetective.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.AnalysisEngine
import com.nfar.allergydetective.domain.analysis.AnalysisRepository
import com.nfar.allergydetective.domain.analysis.CandidateCatalog
import com.nfar.allergydetective.domain.export.buildExport
import com.nfar.allergydetective.domain.export.toJson
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/** Builds the JSON export for the self-test. Sharing it is the user's decision (share sheet). */
class ExportViewModel(
    private val repository: AnalysisRepository,
    private val engine: AnalysisEngine,
    private val catalog: CandidateCatalog,
    private val clock: AppClock,
    private val computeContext: CoroutineContext,
) : ViewModel() {

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun export(onReady: (String) -> Unit) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            val now = clock.nowMillis()
            val input = repository.loadInput(now, clock.offsetMinAt(now))
            val keys = catalog.keys()
            val json = withContext(computeContext) { buildExport(input, engine.analyze(input), keys).toJson() }
            _busy.value = false
            onReady(json)
        }
    }
}
