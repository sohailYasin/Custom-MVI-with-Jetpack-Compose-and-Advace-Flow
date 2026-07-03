package com.etherfi.app.core.model

data class SigningRequest(
    val challenge: String,
    val operationType: OperationType
)
