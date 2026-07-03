package com.etherfi.app.signing

import com.etherfi.app.core.mvi.Intent

sealed class SigningIntent : Intent {
    data class Sign(val method: SigningMethod) : SigningIntent()
    data object Cancel : SigningIntent()
}
