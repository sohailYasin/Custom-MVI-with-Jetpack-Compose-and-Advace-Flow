package com.etherfi.app.views.transfer

import com.etherfi.app.core.model.TransactionQuotation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor() : TransferRepository {

    override suspend fun fetchQuotation(amount: String, recipient: String): Result<TransactionQuotation> = try {
        delay(NETWORK_DELAY_MS)
        Result.success(
            TransactionQuotation(
                id = "transfer_${System.currentTimeMillis()}",
                amount = amount,
                fee = "0.001",
                challenge = "0x${System.currentTimeMillis().toString(16)}transfer",
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
