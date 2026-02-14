package com.unicollabapp.ui.friends

import androidx.compose.foundation.layout.*
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
                    IconButton(onClick = onBack) { Text("←") }
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
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Sending..." else "Send Friend Request") }

            msg?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            Divider()
            Text("Next: request list + accept/decline UI", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
