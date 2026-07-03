package com.etherfi.app.views.withdrawal

import com.etherfi.app.core.mvi.UiState

data class WithdrawalState(
    val amount: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) : UiState
