package com.etherfi.app.core.signing

import com.etherfi.app.core.model.OperationType
import com.etherfi.app.core.model.SigningRequest
import com.etherfi.app.core.model.SigningResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Test

class SigningCoordinatorTest {

    private val coordinator = SigningCoordinator()

    @Test
    fun `requestSigning suspends until deferred is completed`() = runTest(UnconfinedTestDispatcher()) {
        var result: SigningResult? = null

        launch { result = coordinator.requestSigning(SigningRequest("0xabc", OperationType.WITHDRAWAL)) }

        val pending = coordinator.currentSigning.value
        assertNotNull(pending)
        assertEquals("0xabc", pending!!.request.challenge)

        pending.deferred.complete(SigningResult.Success("mock_sig"))

        assertEquals(SigningResult.Success("mock_sig"), result)
    }

    @Test
    fun `completing deferred with Cancelled resumes coroutine correctly`() = runTest(UnconfinedTestDispatcher()) {
        var result: SigningResult? = null

        launch { result = coordinator.requestSigning(SigningRequest("0xabc", OperationType.WITHDRAWAL)) }

        coordinator.currentSigning.value!!.deferred.complete(SigningResult.Cancelled)

        assertEquals(SigningResult.Cancelled, result)
    }

    @Test
    fun `currentSigning reflects the active request details`() = runTest(UnconfinedTestDispatcher()) {
        val request = SigningRequest("0xcafebabe", OperationType.SWAP)

        launch { coordinator.requestSigning(request) }

        val pending = coordinator.currentSigning.value
        assertNotNull(pending)
        assertEquals(OperationType.SWAP, pending!!.request.operationType)
        assertEquals("0xcafebabe", pending.request.challenge)

        pending.deferred.complete(SigningResult.Cancelled)
    }

    @Test
    fun `sequential calls create independent PendingSigning objects`() = runTest(UnconfinedTestDispatcher()) {
        var result1: SigningResult? = null
        var result2: SigningResult? = null

        launch { result1 = coordinator.requestSigning(SigningRequest("0x111", OperationType.WITHDRAWAL)) }
        val pending1 = coordinator.currentSigning.value!!
        pending1.deferred.complete(SigningResult.Success("sig1"))

        launch { result2 = coordinator.requestSigning(SigningRequest("0x222", OperationType.TRANSFER)) }
        val pending2 = coordinator.currentSigning.value!!
        pending2.deferred.complete(SigningResult.Cancelled)

        assertNotSame(pending1, pending2)
        assertEquals(SigningResult.Success("sig1"), result1)
        assertEquals(SigningResult.Cancelled, result2)
    }
}
