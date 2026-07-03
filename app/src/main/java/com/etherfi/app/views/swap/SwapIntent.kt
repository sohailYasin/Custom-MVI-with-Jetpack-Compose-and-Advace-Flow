package com.etherfi.app.views.swap

import com.etherfi.app.core.mvi.Intent

sealed class SwapIntent : Intent {
    data class FromAmountChanged(val amount: String) : SwapIntent()
    data class ToAmountChanged(val amount: String) : SwapIntent()
    data object Continue : SwapIntent()
    data object Done : SwapIntent()
}
