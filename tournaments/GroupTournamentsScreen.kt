package com.unicollabapp.ui.tournaments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unicollabapp.data.tournaments.Tournament
import com.unicollabapp.data.tournaments.TournamentRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupTournamentsScreen(
    groupId: String,
    onBack: () -> Unit,
    repo: TournamentRepository = TournamentRepository()
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var showCreate by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val items = remember { mutableStateListOf<Tournament>() }

    DisposableEffect(groupId) {
        val sub = db.collection("groups").document(groupId)
            .collection("tournaments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    items.clear()
                    items.addAll(
                        snap.documents.map {
                            Tournament(
                                id = it.id,
                                title = it.getString("title") ?: "Tournament",
                                entryFee = it.getLong("entryFee") ?: 0L,
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
                title = { Text("Tournaments") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* realtime already */ }, enabled = true) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Create")
            }
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null)
                    Column(Modifier.weight(1f)) {
                        Text("Group Study Tournaments", fontWeight = FontWeight.Bold)
                        Text(
                            "Create quick competitions (quizzes later). For now it’s a simple join + score placeholder.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            if (items.isEmpty()) {
                Text(
                    "No tournaments yet. Tap + to create one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items) { t ->
                        ElevatedCard(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(t.title, fontWeight = FontWeight.Bold)
                                Text(
                                    "Entry fee: ₦${t.entryFee}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                busy = true
                                                message = null
                                                try {
                                                    repo.joinTournament(groupId, t.id)
                                                    message = "Joined ${t.title} ✅"
                                                } catch (e: Exception) {
                                                    message = e.message ?: "Failed to join"
                                                } finally {
                                                    busy = false
                                                }
                                            }
                                        },
                                        enabled = !busy
                                    ) { Text("Join") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateTournamentDialog(
            onDismiss = { showCreate = false },
            onCreate = { title, fee ->
                scope.launch {
                    busy = true
                    message = null
                    try {
                        repo.createTournament(groupId, title, fee)
                        message = "Tournament created ✅"
                        showCreate = false
                    } catch (e: Exception) {
                        message = e.message ?: "Create failed"
                    } finally {
                        busy = false
                    }
                }
            }
        )
    }
}

@Composable
private fun CreateTournamentDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, fee: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create tournament") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it.filter { c -> c.isDigit() } },
                    label = { Text("Entry fee (₦)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeText.toLongOrNull() ?: 0L
                    onCreate(title.trim(), fee)
                },
                enabled = title.trim().isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
