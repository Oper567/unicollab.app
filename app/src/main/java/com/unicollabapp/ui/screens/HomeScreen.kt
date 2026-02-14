package com.unicollabapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicollabapp.data.auth.AuthRepository
import com.unicollabapp.data.groups.Group
import com.unicollabapp.data.groups.GroupsRepository
import com.unicollabapp.ui.ViewModelFactory
import com.unicollabapp.ui.nav.screens.AuthViewModel

private enum class HomeTab(val label: String) {
    DASHBOARD("Dashboard"),
    GROUPS("Groups")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogoutToAuth: () -> Unit,
    onOpenGroup: (String) -> Unit
) {
    val factory = remember { ViewModelFactory(GroupsRepository(), AuthRepository()) }
    val authVm: AuthViewModel = viewModel(factory = factory)
    val groupsVm: GroupsViewModel = viewModel(factory = factory)
    val state by groupsVm.state.collectAsState()

    var tab by rememberSaveable { mutableStateOf(HomeTab.DASHBOARD) }

    var showCreate by remember { mutableStateOf(false) }
    var showJoin by remember { mutableStateOf(false) }

    var search by rememberSaveable { mutableStateOf("") }
    val filteredGroups = remember(state.groups, search) {
        val q = search.trim().lowercase()
        if (q.isBlank()) state.groups
        else state.groups.filter { g ->
            g.name.lowercase().contains(q) ||
                    g.uni.lowercase().contains(q) ||
                    g.dept.lowercase().contains(q)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("UniCollab", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { groupsVm.refresh() },
                        enabled = !state.isLoading
                    ) { Icon(Icons.Filled.Refresh, contentDescription = "Refresh") }

                    IconButton(
                        onClick = { authVm.logout(); onLogoutToAuth() },
                        enabled = !state.isLoading
                    ) { Icon(Icons.Filled.Logout, contentDescription = "Logout") }
                }
            )
        },
        floatingActionButton = {
            if (tab == HomeTab.GROUPS) {
                FloatingActionButton(
                    onClick = { showCreate = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create group")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == HomeTab.DASHBOARD,
                    onClick = { tab = HomeTab.DASHBOARD },
                    icon = { Icon(Icons.Filled.Home, null) },
                    label = { Text(HomeTab.DASHBOARD.label) }
                )
                NavigationBarItem(
                    selected = tab == HomeTab.GROUPS,
                    onClick = { tab = HomeTab.GROUPS },
                    icon = { Icon(Icons.Filled.Groups, null) },
                    label = { Text(HomeTab.GROUPS.label) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "home_tab_switch"
            ) { current ->
                when (current) {
                    HomeTab.DASHBOARD -> DashboardTabModern(
                        state = state,
                        onOpenGroup = onOpenGroup,
                        onGoGroups = { tab = HomeTab.GROUPS }
                    )

                    HomeTab.GROUPS -> GroupsTabModern(
                        state = state,
                        groups = filteredGroups,
                        search = search,
                        onSearchChange = { search = it },
                        onOpenGroup = onOpenGroup,
                        onCreateClick = { showCreate = true },
                        onJoinClick = { showJoin = true }
                    )
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }

    if (showCreate) {
        CreateGroupDialog(
            isLoading = state.isLoading,
            onDismiss = { showCreate = false },
            onCreate = { n, u, d, l ->
                groupsVm.createGroup(n, u, d, l) {
                    showCreate = false
                }
            }
        )
    }

    if (showJoin) {
        JoinGroupDialog(
            isLoading = state.isLoading,
            onDismiss = { showJoin = false },
            onJoin = { code ->
                groupsVm.joinGroup(code) {
                    showJoin = false
                }
            }
        )
    }
}

@Composable
private fun DashboardTabModern(
    state: GroupsUiState,
    onOpenGroup: (String) -> Unit,
    onGoGroups: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .widthIn(max = 520.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Welcome 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Join course groups, share materials, and stay consistent with your classmates.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onGoGroups) {
                            Icon(Icons.Filled.Search, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Explore Groups")
                        }
                        OutlinedButton(onClick = { /* later: open profile */ }) {
                            Icon(Icons.Filled.Person, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Profile")
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Groups",
                    value = state.groups.size.toString(),
                    icon = Icons.Filled.Groups,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Activity",
                    value = if (state.groups.isNotEmpty()) "Active" else "New",
                    icon = Icons.Filled.Bolt,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent groups",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onGoGroups) { Text("See all") }
            }
        }

        item {
            if (state.groups.isEmpty() && !state.isLoading) {
                EmptyStateCard(
                    title = "No groups yet",
                    subtitle = "Create or join a group to start collaborating.",
                    icon = Icons.Filled.GroupAdd
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.groups.take(10)) { group ->
                        ElevatedCard(
                            modifier = Modifier
                                .width(230.dp)
                                .clickable { onOpenGroup(group.id) },
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    group.name,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${group.uni} • ${group.dept}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                AssistChip(
                                    onClick = { onOpenGroup(group.id) },
                                    label = { Text("Open") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupsTabModern(
    state: GroupsUiState,
    groups: List<Group>,
    search: String,
    onSearchChange: (String) -> Unit,
    onOpenGroup: (String) -> Unit,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search groups") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCreateClick,
                modifier = Modifier.weight(1f),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create")
            }
            OutlinedButton(
                onClick = onJoinClick,
                modifier = Modifier.weight(1f),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Filled.Login, null)
                Spacer(Modifier.width(8.dp))
                Text("Join")
            }
        }

        when {
            state.isLoading && state.groups.isEmpty() -> {
                repeat(4) { SkeletonGroupCard() }
            }

            groups.isEmpty() -> {
                EmptyStateCard(
                    title = "No results",
                    subtitle = "Try a different search or create/join a group.",
                    icon = Icons.Filled.SearchOff
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(groups) { group ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenGroup(group.id) },
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    group.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${group.uni} • ${group.dept}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier, shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ElevatedCard(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null)
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SkeletonGroupCard() {
    ElevatedCard(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            )
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            )
        }
    }
}
