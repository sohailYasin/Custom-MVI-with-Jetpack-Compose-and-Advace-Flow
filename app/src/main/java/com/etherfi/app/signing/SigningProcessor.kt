package com.etherfi.app.signing

import androidx.lifecycle.viewModelScope
import com.etherfi.app.core.model.SigningResult
import com.etherfi.app.core.mvi.Processor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SigningProcessor @Inject constructor() : Processor<SigningIntent, SigningState, SigningSideEffect>() {

    override val state: StateFlow<SigningState> get() = _state.asStateFlow()
    override val sideEffect: Flow<SigningSideEffect> get() = _sideEffect.receiveAsFlow()

    private val _state = MutableStateFlow(SigningState())
    private val _sideEffect = Channel<SigningSideEffect>(Channel.BUFFERED)

    private var onDeliver: ((SigningResult) -> Unit)? = null

    fun initialize(onDeliver: (SigningResult) -> Unit) {
        this.onDeliver = onDeliver
    }

    override fun onIntent(intent: SigningIntent) {
        when (intent) {
            is SigningIntent.Sign -> handleSign(intent.method)
            SigningIntent.Cancel -> handleCancel()
        }
    }

    private fun handleSign(method: SigningMethod) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            delay(SIGNING_DELAY_MS)
            val signature = "mock_${method.name.lowercase()}_${System.currentTimeMillis()}"
            _state.update { it.copy(isLoading = false) }
            onDeliver?.invoke(SigningResult.Success(signature))
            _sideEffect.send(SigningSideEffect.NavigateBack)
        }
    }

    private fun handleCancel() {
        viewModelScope.launch {
            onDeliver?.invoke(SigningResult.Cancelled)
            _sideEffect.send(SigningSideEffect.NavigateBack)
        }
    }

    companion object {
        private const val SIGNING_DELAY_MS = 1500L
    }
}
