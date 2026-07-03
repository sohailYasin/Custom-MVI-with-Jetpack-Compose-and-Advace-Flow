package com.etherfi.app.di

import com.etherfi.app.views.swap.SwapRepository
import com.etherfi.app.views.swap.SwapRepositoryImpl
import com.etherfi.app.views.transfer.TransferRepository
import com.etherfi.app.views.transfer.TransferRepositoryImpl
import com.etherfi.app.views.withdrawal.WithdrawalRepository
import com.etherfi.app.views.withdrawal.WithdrawalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWithdrawalRepository(impl: WithdrawalRepositoryImpl): WithdrawalRepository

    @Binds
    @Singleton
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository

    @Binds
    @Singleton
    abstract fun bindSwapRepository(impl: SwapRepositoryImpl): SwapRepository
}
