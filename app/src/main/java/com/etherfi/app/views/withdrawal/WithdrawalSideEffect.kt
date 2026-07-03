package com.etherfi.app.views.withdrawal

sealed class WithdrawalSideEffect {
    data object NavigateToHome : WithdrawalSideEffect()
}
