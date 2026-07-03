package com.etherfi.app.views.transfer

import com.etherfi.app.core.mvi.UiState

data class TransferState(
    val amount: String = "",
    val recipient: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) : UiState
