package com.etherfi.app.signing

import com.etherfi.app.core.mvi.UiState

data class SigningState(
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState
