package com.etherfi.app.views.transfer

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
class TransferProcessor @Inject constructor(
    private val repository: TransferRepository,
    private val coordinator: SigningCoordinator
) : Processor<TransferIntent, TransferState, TransferSideEffect>() {

    override val state: StateFlow<TransferState> get() = _state.asStateFlow()
    override val sideEffect: Flow<TransferSideEffect> get() = _sideEffect.receiveAsFlow()

    private val _state = MutableStateFlow(TransferState())
    private val _sideEffect = Channel<TransferSideEffect>(Channel.BUFFERED)

    override fun onIntent(intent: TransferIntent) {
        when (intent) {
            is TransferIntent.AmountChanged -> _state.update { it.copy(amount = intent.amount, error = null) }
            is TransferIntent.RecipientChanged -> _state.update { it.copy(recipient = intent.recipient, error = null) }
            TransferIntent.Continue -> handleContinue()
            TransferIntent.Done -> viewModelScope.launch {
                _sideEffect.send(TransferSideEffect.NavigateToHome)
            }
        }
    }

    private fun handleContinue() {
        val state = _state.value
        when {
            state.amount.isBlank() -> { _state.update { it.copy(error = "Please enter an amount") }; return }
            state.recipient.isBlank() -> { _state.update { it.copy(error = "Please enter a recipient address") }; return }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val quotation = repository.fetchQuotation(state.amount.trim(), state.recipient.trim()).getOrElse { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to get quotation") }
                return@launch
            }

            val signingResult = coordinator.requestSigning(
                SigningRequest(challenge = quotation.challenge, operationType = OperationType.TRANSFER)
            )

            when (signingResult) {
                is SigningResult.Success -> {
                    repository.submitTransaction(quotation.id, signingResult.signature).getOrElse { e ->
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
