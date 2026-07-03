package com.etherfi.app.views.swap

import com.etherfi.app.core.model.TransactionQuotation

interface SwapRepository {
    suspend fun fetchQuote(fromAmount: String, toAmount: String): Result<TransactionQuotation>
    suspend fun submitSwap(quotationId: String, signature: String): Result<Unit>
}
