package com.unicollabapp.ui.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unicollabapp.data.wallet.WalletRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBack: () -> Unit,
    repo: WalletRepository = WalletRepository()
) {
    var balance by remember { mutableStateOf<Long?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            loading = true
            msg = null
            try {
                balance = repo.getWallet().balance
            } catch (e: Exception) {
                msg = e.message ?: "Failed to load wallet"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }, enabled = !loading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current balance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = balance?.let { "₦$it" } ?: if (loading) "…" else "₦0",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Demo wallet for UniCollab (top-up is simulated).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            msg = null
                            try {
                                repo.topUp(500)
                                balance = (balance ?: 0) + 500
                                msg = "Top-up successful ✅"
                            } catch (e: Exception) {
                                msg = e.message ?: "Top-up failed"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.weight(1f)
                ) { Text("Top up ₦500") }

                OutlinedButton(
                    onClick = { refresh() },
                    enabled = !loading,
                    modifier = Modifier.weight(1f)
                ) { Text("Reload") }
            }

            msg?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            HorizontalDivider()

            Text(
                "Next upgrades: real payments, withdrawals, escrow for tournaments.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
