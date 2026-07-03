package com.etherfi.app.views.transfer

import com.etherfi.app.core.mvi.Intent

sealed class TransferIntent : Intent {
    data class AmountChanged(val amount: String) : TransferIntent()
    data class RecipientChanged(val recipient: String) : TransferIntent()
    data object Continue : TransferIntent()
    data object Done : TransferIntent()
}
