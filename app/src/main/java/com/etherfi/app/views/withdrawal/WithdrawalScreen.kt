package com.etherfi.app.views.withdrawal

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
fun WithdrawalScreen(
    onNavigateBack: () -> Unit,
    processor: WithdrawalProcessor = hiltViewModel()
) {
    val state by processor.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        processor.sideEffect.collect { onNavigateBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdrawal") },
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
                SuccessContent(onDone = { processor.onIntent(WithdrawalIntent.Done) })
            } else {
                WithdrawalContent(
                    state = state,
                    onAmountChanged = { processor.onIntent(WithdrawalIntent.AmountChanged(it)) },
                    onContinue = { processor.onIntent(WithdrawalIntent.Continue) }
                )
            }
        }
    }
}

@Composable
private fun WithdrawalContent(
    state: WithdrawalState,
    onAmountChanged: (String) -> Unit,
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
            isError = state.error != null,
            singleLine = true,
            supportingText = state.error?.let {
                { Text(text = it, color = MaterialTheme.colorScheme.error) }
            }
        )

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
private fun SuccessContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Withdrawal Submitted!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your transaction has been signed and submitted successfully.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDone) { Text("Done") }
    }
}

@Preview(showBackground = true, name = "Idle")
@Composable
private fun WithdrawalIdlePreview() {
    EtherFiSigningTheme {
        WithdrawalContent(
            state = WithdrawalState(amount = "1.5"),
            onAmountChanged = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun WithdrawalLoadingPreview() {
    EtherFiSigningTheme {
        WithdrawalContent(
            state = WithdrawalState(amount = "1.5", isLoading = true),
            onAmountChanged = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun WithdrawalErrorPreview() {
    EtherFiSigningTheme {
        WithdrawalContent(
            state = WithdrawalState(error = "Please enter an amount"),
            onAmountChanged = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun WithdrawalSuccessPreview() {
    EtherFiSigningTheme {
        SuccessContent(onDone = {})
    }
}
