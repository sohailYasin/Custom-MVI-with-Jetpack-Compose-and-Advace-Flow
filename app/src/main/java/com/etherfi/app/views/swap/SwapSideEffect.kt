package com.etherfi.app.views.swap

sealed class SwapSideEffect {
    data object NavigateToHome : SwapSideEffect()
}
