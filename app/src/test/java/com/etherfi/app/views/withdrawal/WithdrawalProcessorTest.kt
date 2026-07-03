package com.etherfi.app.views.withdrawal

import com.etherfi.app.MainDispatcherRule
import com.etherfi.app.core.model.OperationType
import com.etherfi.app.core.model.SigningResult
import com.etherfi.app.core.model.TransactionQuotation
import com.etherfi.app.core.signing.SigningCoordinator
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WithdrawalProcessorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val coordinator = SigningCoordinator()

    // --- Validation ---

    @Test
    fun `blank amount shows error without proceeding`() = runTest(mainDispatcherRule.testDispatcher) {
        val processor = WithdrawalProcessor(FakeWithdrawalRepository(), coordinator)

        processor.onIntent(WithdrawalIntent.Continue)

        assertEquals("Please enter an amount", processor.state.value.error)
        assertFalse(processor.state.value.isLoading)
    }

    // --- Happy path ---

    @Test
    fun `successful signing flow transitions to isSuccess`() = runTest(mainDispatcherRule.testDispatcher) {
        val processor = WithdrawalProcessor(FakeWithdrawalRepository(), coordinator)

        processor.onIntent(WithdrawalIntent.AmountChanged("1.0"))
        processor.onIntent(WithdrawalIntent.Continue)

        val pending = coordinator.currentSigning.value
        assertNotNull("Signing request expected after Continue", pending)
        assertEquals(OperationType.WITHDRAWAL, pending!!.request.operationType)

        pending.deferred.complete(SigningResult.Success("mock_sig"))

        assertTrue(processor.state.value.isSuccess)
        assertFalse(processor.state.value.isLoading)
        assertNull(processor.state.value.error)
    }

    // --- Cancel ---

    @Test
    fun `cancelled signing returns processor to idle`() = runTest(mainDispatcherRule.testDispatcher) {
        val processor = WithdrawalProcessor(FakeWithdrawalRepository(), coordinator)

        processor.onIntent(WithdrawalIntent.AmountChanged("1.0"))
        processor.onIntent(WithdrawalIntent.Continue)

        coordinator.currentSigning.value!!.deferred.complete(SigningResult.Cancelled)

        assertFalse(processor.state.value.isLoading)
        assertFalse(processor.state.value.isSuccess)
        assertNull(processor.state.value.error)
    }

    // --- Repository failures ---

    @Test
    fun `quotation fetch failure shows error`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeWithdrawalRepository(
            quotationResult = Result.failure(Exception("Network error"))
        )
        val processor = WithdrawalProcessor(repository, coordinator)

        processor.onIntent(WithdrawalIntent.AmountChanged("1.0"))
        processor.onIntent(WithdrawalIntent.Continue)

        assertEquals("Network error", processor.state.value.error)
        assertFalse(processor.state.value.isLoading)
    }

    @Test
    fun `submit failure after signing shows error`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeWithdrawalRepository(
            submitResult = Result.failure(Exception("Submit failed"))
        )
        val processor = WithdrawalProcessor(repository, coordinator)

        processor.onIntent(WithdrawalIntent.AmountChanged("1.0"))
        processor.onIntent(WithdrawalIntent.Continue)

        coordinator.currentSigning.value!!.deferred.complete(SigningResult.Success("sig"))

        assertEquals("Submit failed", processor.state.value.error)
        assertFalse(processor.state.value.isLoading)
    }

    // --- Side effects ---

    @Test
    fun `Done intent emits NavigateToHome side effect`() = runTest(mainDispatcherRule.testDispatcher) {
        val processor = WithdrawalProcessor(FakeWithdrawalRepository(), coordinator)
        val effects = mutableListOf<WithdrawalSideEffect>()

        val collectJob = launch { processor.sideEffect.collect { effects.add(it) } }
        processor.onIntent(WithdrawalIntent.Done)

        assertEquals(listOf(WithdrawalSideEffect.NavigateToHome), effects)
        collectJob.cancel()
    }
}

private class FakeWithdrawalRepository(
    private val quotationResult: Result<TransactionQuotation> = Result.success(
        TransactionQuotation(
            id = "test_id",
            amount = "1.0",
            fee = "0.001",
            challenge = "0xtest_challenge",
            expiresAt = Long.MAX_VALUE
        )
    ),
    private val submitResult: Result<Unit> = Result.success(Unit)
) : WithdrawalRepository {
    override suspend fun fetchQuotation(amount: String): Result<TransactionQuotation> = quotationResult
    override suspend fun submitTransaction(quotationId: String, signature: String): Result<Unit> = submitResult
}
