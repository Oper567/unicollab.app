package com.unicollabapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicollabapp.data.auth.AuthRepository
import com.unicollabapp.data.groups.GroupsRepository
import com.unicollabapp.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(onOpenGroup: (String) -> Unit) {
    val factory = remember { ViewModelFactory(GroupsRepository(), AuthRepository()) }
    val vm: GroupsViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var showJoin by remember { mutableStateOf(false) }

    // Search
    var search by rememberSaveable { mutableStateOf("") }
    val filtered = remember(state.groups, search) {
        val q = search.trim().lowercase()
        if (q.isBlank()) state.groups
        else state.groups.filter { g ->
            g.name.lowercase().contains(q) ||
                    g.uni.lowercase().contains(q) ||
                    g.dept.lowercase().contains(q)
        }
    }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        // If your VM doesn't have events, remove this block
        vm.events.collect { msg ->
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .widthIn(max = 520.dp)
        ) {
            Text(
                "Study Groups",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Find your course mates, share notes, and stay consistent.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(14.dp))

            // Search bar
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search groups") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Actions
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCreate = true },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isActionLoading
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create")
                }
                OutlinedButton(
                    onClick = { showJoin = true },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isActionLoading
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Join")
                }
            }

            // Loading indicator
            if (state.isLoading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            } else {
                Spacer(Modifier.height(12.dp))
            }

            // Content
            when {
                state.error != null -> {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Couldn’t load groups",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                state.error ?: "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { vm.refresh() },
                                enabled = !state.isLoading
                            ) { Text("Retry") }
                        }
                    }
                }

                !state.isLoading && filtered.isEmpty() -> {
                    EmptyStateCard(
                        title = if (search.isBlank()) "No groups yet" else "No results",
                        subtitle = if (search.isBlank())
                            "Create or join a group to start collaborating."
                        else
                            "Try a different search word.",
                        icon = Icons.Filled.SearchOff
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filtered) { g ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenGroup(g.id) },
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            g.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${g.uni} • ${g.dept}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs (UPDATED: isLoading)
    if (showCreate) {
        CreateGroupDialog(
            isLoading = state.isActionLoading,
            onDismiss = { showCreate = false },
            onCreate = { n, u, d, l ->
                vm.createGroup(n, u, d, l) { showCreate = false }
            }
        )
    }

    if (showJoin) {
        JoinGroupDialog(
            isLoading = state.isActionLoading,
            onDismiss = { showJoin = false },
            onJoin = { c ->
                vm.joinGroup(c) { showJoin = false }
            }
        )
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
