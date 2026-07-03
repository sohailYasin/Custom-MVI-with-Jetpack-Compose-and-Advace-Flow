package com.etherfi.app.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class Processor<I : Intent, S : UiState, E : Any> : ViewModel() {
    abstract val state: StateFlow<S>
    abstract val sideEffect: Flow<E>
    abstract fun onIntent(intent: I)
}
