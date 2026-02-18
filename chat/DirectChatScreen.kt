package com.unicollabapp.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unicollabapp.data.chat.DirectChatRepository
import kotlinx.coroutines.launch

data class DirectMsg(
    val id: String,
    val senderUid: String,
    val text: String,
    val createdAt: Timestamp?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    peerUid: String,
    onBack: () -> Unit,
    repo: DirectChatRepository = DirectChatRepository()
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val chatId = remember(peerUid) { repo.chatIdFor(peerUid) }

    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val messages = remember { mutableStateListOf<DirectMsg>() }

    DisposableEffect(chatId) {
        val sub = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    messages.clear()
                    messages.addAll(
                        snap.documents.map {
                            DirectMsg(
                                id = it.id,
                                senderUid = it.getString("senderUid") ?: "",
                                text = it.getString("text") ?: "",
                                createdAt = it.getTimestamp("createdAt")
                            )
                        }
                    )
                }
            }
        onDispose { sub.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { m ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(m.text)
                            m.createdAt?.let {
                                Text(
                                    it.toDate().toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message…") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        val text = input.trim()
                        if (text.isBlank()) return@Button
                        scope.launch {
                            sending = true
                            try {
                                repo.sendMessage(peerUid, text)
                                input = ""
                            } finally {
                                sending = false
                            }
                        }
                    },
                    enabled = !sending
                ) {
                    Text(if (sending) "…" else "Send")
                }
            }
        }
    }
}
