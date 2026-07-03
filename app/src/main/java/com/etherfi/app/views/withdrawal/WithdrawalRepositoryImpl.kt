package com.etherfi.app.views.withdrawal

import com.etherfi.app.core.model.TransactionQuotation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import javax.inject.Inject

class WithdrawalRepositoryImpl @Inject constructor() : WithdrawalRepository {

    override suspend fun fetchQuotation(amount: String): Result<TransactionQuotation> = try {
        delay(NETWORK_DELAY_MS)
        Result.success(
            TransactionQuotation(
                id = "quote_${System.currentTimeMillis()}",
                amount = amount,
                fee = "0.001",
                challenge = "0x${System.currentTimeMillis().toString(16)}abcdef1234567890",
                expiresAt = System.currentTimeMillis() + 300_000
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun submitTransaction(quotationId: String, signature: String): Result<Unit> = try {
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
