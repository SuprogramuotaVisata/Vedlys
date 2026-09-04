package com.suprogramuota_visata.vedlys.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Minimali bazinė ViewModel klasė Compose Desktop aplikacijai.
 * Suteikia coroutine scope ir gyvavimo ciklo valdymą.
 */
abstract class BaseViewModel {
    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Kviečiama kai ViewModel'is daugiau nebereikalingas. */
    open fun dispose() {
        scope.cancel()
    }
}
