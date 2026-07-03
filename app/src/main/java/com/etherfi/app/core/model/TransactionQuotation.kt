package com.etherfi.app.core.model

data class TransactionQuotation(
    val id: String,
    val amount: String,
    val fee: String,
    val challenge: String,
    val expiresAt: Long
)
