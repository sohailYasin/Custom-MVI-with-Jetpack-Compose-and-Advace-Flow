package com.etherfi.app.views.transfer

import com.etherfi.app.core.model.TransactionQuotation

interface TransferRepository {
    suspend fun fetchQuotation(amount: String, recipient: String): Result<TransactionQuotation>
    suspend fun submitTransaction(quotationId: String, signature: String): Result<Unit>
}
