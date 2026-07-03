package com.etherfi.app.core.model

sealed class SigningResult {
    data class Success(val signature: String) : SigningResult()
    data object Cancelled : SigningResult()
    data class Error(val message: String) : SigningResult()
}
