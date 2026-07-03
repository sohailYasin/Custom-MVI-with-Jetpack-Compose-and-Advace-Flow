package com.etherfi.app.core.signing

import com.etherfi.app.core.model.SigningRequest
import com.etherfi.app.core.model.SigningResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates signing requests between business processors and the shared
 * signing UI. A processor suspends by calling requestSigning(), and is
 * resumed when the signing screen delivers a SigningResult.
 */

@Singleton
class SigningCoordinator @Inject constructor() {

    data class PendingSigning(
        val request: SigningRequest,
        val deferred: CompletableDeferred<SigningResult>
    )

    private val _currentSigning = MutableStateFlow<PendingSigning?>(null)
    val currentSigning: StateFlow<PendingSigning?> = _currentSigning.asStateFlow()

    suspend fun requestSigning(request: SigningRequest): SigningResult {
        val pending = PendingSigning(request, CompletableDeferred())
        _currentSigning.value = pending
        return try {
            pending.deferred.await()
        } finally {
            _currentSigning.value = null
        }
    }
}
