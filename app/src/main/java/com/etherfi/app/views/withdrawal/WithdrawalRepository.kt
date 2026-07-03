package com.etherfi.app.views.withdrawal

import com.etherfi.app.core.model.TransactionQuotation

interface WithdrawalRepository {
    suspend fun fetchQuotation(amount: String): Result<TransactionQuotation>
    suspend fun submitTransaction(quotationId: String, signature: String): Result<Unit>
}
