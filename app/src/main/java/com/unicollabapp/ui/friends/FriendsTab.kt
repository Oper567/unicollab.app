package com.unicollabapp.ui.friends

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FriendsTab(
    onOpenFriendsFull: () -> Unit
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Friends", style = MaterialTheme.typography.titleLarge)
                Text("Add classmates, accept requests, and build your network.")
                Button(onClick = onOpenFriendsFull, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Friends")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Next features")
                Text("• Friend requests\n• Mutual friends\n• Search by username\n• Invite link")
            }
        }
    }
}
