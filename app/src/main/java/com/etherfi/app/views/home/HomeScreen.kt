package com.etherfi.app.views.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.etherfi.app.ui.theme.EtherFiSigningTheme

@Composable
fun HomeScreen(
    onNavigateToWithdrawal: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToSwap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ether.fi",
            style = MaterialTheme.typography.displaySmall
        )

        Text(
            text = "Select an operation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(
            onClick = onNavigateToWithdrawal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Withdrawal")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateToTransfer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Transfer")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateToSwap,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Swap")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    EtherFiSigningTheme {
        HomeScreen(
            onNavigateToWithdrawal = {},
            onNavigateToTransfer = {},
            onNavigateToSwap = {}
        )
    }
}
