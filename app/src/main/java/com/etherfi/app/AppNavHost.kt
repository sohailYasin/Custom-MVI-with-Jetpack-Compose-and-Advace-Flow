package com.etherfi.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.etherfi.app.core.signing.SigningCoordinator
import com.etherfi.app.signing.SigningScreen
import com.etherfi.app.views.home.HomeScreen
import com.etherfi.app.views.swap.SwapScreen
import com.etherfi.app.views.transfer.TransferScreen
import com.etherfi.app.views.withdrawal.WithdrawalScreen

@Composable
fun AppNavHost(
    coordinator: SigningCoordinator,
    navController: NavHostController = rememberNavController()
) {
    val currentSigning by coordinator.currentSigning.collectAsStateWithLifecycle()

    LaunchedEffect(currentSigning) {
        if (currentSigning != null) {
            navController.navigate("signing")
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToWithdrawal = { navController.navigate("withdrawal") },
                onNavigateToTransfer = { navController.navigate("transfer") },
                onNavigateToSwap = { navController.navigate("swap") }
            )
        }
        composable("withdrawal") {
            WithdrawalScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("transfer") {
            TransferScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("swap") {
            SwapScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("signing") {
            currentSigning?.let { pending ->
                SigningScreen(
                    request = pending.request,
                    onDeliver = { result -> pending.deferred.complete(result) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
