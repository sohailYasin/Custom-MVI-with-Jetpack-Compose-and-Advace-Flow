package com.etherfi.app.views.swap

import com.etherfi.app.core.model.TransactionQuotation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import javax.inject.Inject

class SwapRepositoryImpl @Inject constructor() : SwapRepository {

    override suspend fun fetchQuote(fromAmount: String, toAmount: String): Result<TransactionQuotation> = try {
        delay(NETWORK_DELAY_MS)
        Result.success(
            TransactionQuotation(
                id = "swap_${System.currentTimeMillis()}",
                amount = fromAmount,
                fee = "0.002",
                challenge = "0x${System.currentTimeMillis().toString(16)}swap",
                expiresAt = System.currentTimeMillis() + 300_000
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun submitSwap(quotationId: String, signature: String): Result<Unit> = try {
        delay(NETWORK_DELAY_MS)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    companion object {
        private const val NETWORK_DELAY_MS = 1000L
    }
}
