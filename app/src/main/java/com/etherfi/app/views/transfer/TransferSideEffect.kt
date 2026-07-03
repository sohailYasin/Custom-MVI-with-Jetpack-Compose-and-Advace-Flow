package com.etherfi.app.views.transfer

sealed class TransferSideEffect {
    data object NavigateToHome : TransferSideEffect()
}
