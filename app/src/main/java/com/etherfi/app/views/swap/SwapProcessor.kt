package com.etherfi.app.views.swap

import androidx.lifecycle.viewModelScope
import com.etherfi.app.core.model.OperationType
import com.etherfi.app.core.model.SigningRequest
import com.etherfi.app.core.model.SigningResult
import com.etherfi.app.core.mvi.Processor
import com.etherfi.app.core.signing.SigningCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwapProcessor @Inject constructor(
    private val repository: SwapRepository,
    private val coordinator: SigningCoordinator
) : Processor<SwapIntent, SwapState, SwapSideEffect>() {

    override val state: StateFlow<SwapState> get() = _state.asStateFlow()
    override val sideEffect: Flow<SwapSideEffect> get() = _sideEffect.receiveAsFlow()

    private val _state = MutableStateFlow(SwapState())
    private val _sideEffect = Channel<SwapSideEffect>(Channel.BUFFERED)

    override fun onIntent(intent: SwapIntent) {
        when (intent) {
            is SwapIntent.FromAmountChanged -> _state.update { it.copy(fromAmount = intent.amount, error = null) }
            is SwapIntent.ToAmountChanged -> _state.update { it.copy(toAmount = intent.amount, error = null) }
            SwapIntent.Continue -> handleContinue()
            SwapIntent.Done -> viewModelScope.launch {
                _sideEffect.send(SwapSideEffect.NavigateToHome)
            }
        }
    }

    private fun handleContinue() {
        val state = _state.value
        when {
            state.fromAmount.isBlank() -> { _state.update { it.copy(error = "Please enter amount to swap") }; return }
            state.toAmount.isBlank() -> { _state.update { it.copy(error = "Please enter expected amount") }; return }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val quotation = repository.fetchQuote(state.fromAmount.trim(), state.toAmount.trim()).getOrElse { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to get quote") }
                return@launch
            }

            val signingResult = coordinator.requestSigning(
                SigningRequest(challenge = quotation.challenge, operationType = OperationType.SWAP)
            )

            when (signingResult) {
                is SigningResult.Success -> {
                    repository.submitSwap(quotation.id, signingResult.signature).getOrElse { e ->
                        _state.update { it.copy(isLoading = false, error = e.message ?: "Submission failed") }
                        return@launch
                    }
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is SigningResult.Cancelled -> _state.update { it.copy(isLoading = false) }
                is SigningResult.Error -> _state.update { it.copy(isLoading = false, error = signingResult.message) }
            }
        }
    }
}
