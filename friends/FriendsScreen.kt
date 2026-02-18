package com.unicollabapp.ui.friends

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unicollabapp.data.friends.FriendsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onOpenChat: (peerUid: String) -> Unit = {},
    repo: FriendsRepository = FriendsRepository()
) {
    var email by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; msg = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search user by email") },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            msg = null
                            try {
                                val uid = repo.findUserByEmail(email)
                                if (uid == null) msg = "No user found"
                                else {
                                    repo.sendFriendRequest(uid)
                                    msg = "Friend request sent ✅"
                                }
                            } catch (e: Exception) {
                                msg = e.message ?: "Failed"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = !loading && email.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text(if (loading) "Sending..." else "Request") }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            loading = true
                            msg = null
                            try {
                                val uid = repo.findUserByEmail(email)
                                if (uid == null) msg = "No user found"
                                else onOpenChat(uid)
                            } catch (e: Exception) {
                                msg = e.message ?: "Failed"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = !loading && email.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text("Chat") }
            }

            msg?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            HorizontalDivider()
            Text("Next: request list + accept/decline UI", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
