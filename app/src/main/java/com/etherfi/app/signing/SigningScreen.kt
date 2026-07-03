package com.etherfi.app.signing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etherfi.app.core.model.OperationType
import com.etherfi.app.core.model.SigningRequest
import com.etherfi.app.core.model.SigningResult
import com.etherfi.app.ui.theme.EtherFiSigningTheme

@Composable
fun SigningScreen(
    request: SigningRequest,
    onDeliver: (SigningResult) -> Unit,
    onNavigateBack: () -> Unit,
    processor: SigningProcessor = hiltViewModel()
) {
    val state by processor.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        processor.initialize(onDeliver)
    }

    LaunchedEffect(Unit) {
        processor.sideEffect.collect {
            onNavigateBack()
        }
    }

    BackHandler(enabled = !state.isLoading) {
        processor.onIntent(SigningIntent.Cancel)
    }

    SigningContent(
        request = request,
        state = state,
        onSign = { processor.onIntent(SigningIntent.Sign(it)) },
        onCancel = { processor.onIntent(SigningIntent.Cancel) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SigningContent(
    request: SigningRequest,
    state: SigningState,
    onSign: (SigningMethod) -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign ${request.operationType.name}") },
                navigationIcon = {
                    IconButton(onClick = onCancel, enabled = !state.isLoading) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel signing")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Challenge",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = request.challenge,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                Text(text = "Signing...", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(text = "Choose signing method", style = MaterialTheme.typography.titleSmall)

                SigningMethod.entries.forEach { method ->
                    Button(
                        onClick = { onSign(method) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = method.name)
                    }
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Idle - Withdrawal")
@Composable
private fun SigningIdleWithdrawalPreview() {
    EtherFiSigningTheme {
        SigningContent(
            request = SigningRequest(
                challenge = "0x1a2b3c4d5e6f7890abcdef1234567890",
                operationType = OperationType.WITHDRAWAL
            ),
            state = SigningState(),
            onSign = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle - Transfer")
@Composable
private fun SigningIdleTransferPreview() {
    EtherFiSigningTheme {
        SigningContent(
            request = SigningRequest(
                challenge = "0xdeadbeef1234567890abcdef",
                operationType = OperationType.TRANSFER
            ),
            state = SigningState(),
            onSign = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle - Swap")
@Composable
private fun SigningIdleSwapPreview() {
    EtherFiSigningTheme {
        SigningContent(
            request = SigningRequest(
                challenge = "0xcafebabe9876543210fedcba",
                operationType = OperationType.SWAP
            ),
            state = SigningState(),
            onSign = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun SigningLoadingPreview() {
    EtherFiSigningTheme {
        SigningContent(
            request = SigningRequest(
                challenge = "0x1a2b3c4d5e6f7890abcdef",
                operationType = OperationType.WITHDRAWAL
            ),
            state = SigningState(isLoading = true),
            onSign = {},
            onCancel = {}
        )
    }
}
