package com.etherfi.app.views.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etherfi.app.ui.theme.EtherFiSigningTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onNavigateBack: () -> Unit,
    processor: TransferProcessor = hiltViewModel()
) {
    val state by processor.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        processor.sideEffect.collect { onNavigateBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            if (state.isSuccess) {
                TransferSuccessContent(onDone = { processor.onIntent(TransferIntent.Done) })
            } else {
                TransferContent(
                    state = state,
                    onAmountChanged = { processor.onIntent(TransferIntent.AmountChanged(it)) },
                    onRecipientChanged = { processor.onIntent(TransferIntent.RecipientChanged(it)) },
                    onContinue = { processor.onIntent(TransferIntent.Continue) }
                )
            }
        }
    }
}

@Composable
private fun TransferContent(
    state: TransferState,
    onAmountChanged: (String) -> Unit,
    onRecipientChanged: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.amount,
            onValueChange = onAmountChanged,
            label = { Text("Amount (ETH)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = state.recipient,
            onValueChange = onRecipientChanged,
            label = { Text("Recipient Address") },
            placeholder = { Text("0x...") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true
        )

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = onContinue,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Processing...")
                }
            } else {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun TransferSuccessContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Transfer Submitted!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your transfer has been signed and submitted.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDone) { Text("Done") }
    }
}

@Preview(showBackground = true, name = "Idle")
@Composable
private fun TransferIdlePreview() {
    EtherFiSigningTheme {
        TransferContent(
            state = TransferState(amount = "1.0", recipient = "0xAbC..."),
            onAmountChanged = {}, onRecipientChanged = {}, onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun TransferLoadingPreview() {
    EtherFiSigningTheme {
        TransferContent(
            state = TransferState(amount = "1.0", recipient = "0xAbC...", isLoading = true),
            onAmountChanged = {}, onRecipientChanged = {}, onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun TransferErrorPreview() {
    EtherFiSigningTheme {
        TransferContent(
            state = TransferState(error = "Please enter a recipient address"),
            onAmountChanged = {}, onRecipientChanged = {}, onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun TransferSuccessPreview() {
    EtherFiSigningTheme {
        TransferSuccessContent(onDone = {})
    }
}
