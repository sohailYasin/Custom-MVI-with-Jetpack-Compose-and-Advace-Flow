package com.etherfi.app.views.withdrawal

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
class WithdrawalProcessor @Inject constructor(
    private val repository: WithdrawalRepository,
    private val coordinator: SigningCoordinator
) : Processor<WithdrawalIntent, WithdrawalState, WithdrawalSideEffect>() {

    override val state: StateFlow<WithdrawalState> get() = _state.asStateFlow()
    override val sideEffect: Flow<WithdrawalSideEffect> get() = _sideEffect.receiveAsFlow()

    private val _state = MutableStateFlow(WithdrawalState())
    private val _sideEffect = Channel<WithdrawalSideEffect>(Channel.BUFFERED)

    override fun onIntent(intent: WithdrawalIntent) {
        when (intent) {
            is WithdrawalIntent.AmountChanged -> _state.update { it.copy(amount = intent.amount, error = null) }
            WithdrawalIntent.Continue -> handleContinue()
            WithdrawalIntent.Done -> viewModelScope.launch {
                _sideEffect.send(WithdrawalSideEffect.NavigateToHome)
            }
        }
    }

    private fun handleContinue() {
        val amount = _state.value.amount.trim()
        if (amount.isBlank()) {
            _state.update { it.copy(error = "Please enter an amount") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val quotation = repository.fetchQuotation(amount).getOrElse { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to get quotation") }
                return@launch
            }

            val signingResult = coordinator.requestSigning(
                SigningRequest(challenge = quotation.challenge, operationType = OperationType.WITHDRAWAL)
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
