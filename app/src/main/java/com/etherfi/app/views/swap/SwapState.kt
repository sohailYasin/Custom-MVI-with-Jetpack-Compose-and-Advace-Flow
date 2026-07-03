package com.etherfi.app.views.swap

import com.etherfi.app.core.mvi.UiState

data class SwapState(
    val fromAmount: String = "",
    val toAmount: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) : UiState
