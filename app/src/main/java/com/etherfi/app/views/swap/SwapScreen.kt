package com.etherfi.app.views.swap

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
import androidx.compose.material3.HorizontalDivider
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
fun SwapScreen(
    onNavigateBack: () -> Unit,
    processor: SwapProcessor = hiltViewModel()
) {
    val state by processor.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        processor.sideEffect.collect { onNavigateBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swap") },
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
                SwapSuccessContent(onDone = { processor.onIntent(SwapIntent.Done) })
            } else {
                SwapContent(
                    state = state,
                    onFromAmountChanged = { processor.onIntent(SwapIntent.FromAmountChanged(it)) },
                    onToAmountChanged = { processor.onIntent(SwapIntent.ToAmountChanged(it)) },
                    onContinue = { processor.onIntent(SwapIntent.Continue) }
                )
            }
        }
    }
}

@Composable
private fun SwapContent(
    state: SwapState,
    onFromAmountChanged: (String) -> Unit,
    onToAmountChanged: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.fromAmount,
            onValueChange = onFromAmountChanged,
            label = { Text("From (ETH)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true
        )

        HorizontalDivider()

        OutlinedTextField(
            value = state.toAmount,
            onValueChange = onToAmountChanged,
            label = { Text("To (weETH)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                Text("Review Swap")
            }
        }
    }
}

@Composable
private fun SwapSuccessContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Swap Submitted!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your swap has been signed and submitted.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDone) { Text("Done") }
    }
}

@Preview(showBackground = true, name = "Idle")
@Composable
private fun SwapIdlePreview() {
    EtherFiSigningTheme {
        SwapContent(
            state = SwapState(fromAmount = "1.0", toAmount = "1.02"),
            onFromAmountChanged = {}, onToAmountChanged = {}, onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun SwapLoadingPreview() {
    EtherFiSigningTheme {
        SwapContent(
            state = SwapState(fromAmount = "1.0", toAmount = "1.02", isLoading = true),
            onFromAmountChanged = {}, onToAmountChanged = {}, onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun SwapErrorPreview() {
    EtherFiSigningTheme {
        SwapContent(
            state = SwapState(error = "Please enter amount to swap"),
            onFromAmountChanged = {}, onToAmountChanged = {}, onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun SwapSuccessPreview() {
    EtherFiSigningTheme {
        SwapSuccessContent(onDone = {})
    }
}
