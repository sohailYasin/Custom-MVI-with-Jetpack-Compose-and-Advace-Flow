package com.etherfi.app.views.withdrawal

import com.etherfi.app.core.mvi.Intent

sealed class WithdrawalIntent : Intent {
    data class AmountChanged(val amount: String) : WithdrawalIntent()
    data object Continue : WithdrawalIntent()
    data object Done : WithdrawalIntent()
}
