package com.etherfi.app.signing

sealed class SigningSideEffect {
    data object NavigateBack : SigningSideEffect()
}
