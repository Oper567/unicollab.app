package com.unicollabapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.unicollabapp.data.auth.AuthRepository
import com.unicollabapp.data.groups.GroupsRepository
import com.unicollabapp.data.users.UserProfile
import com.unicollabapp.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val email = remember { FirebaseAuth.getInstance().currentUser?.email }

    val factory = remember { ViewModelFactory(GroupsRepository(), AuthRepository()) }
    val vm: ProfileViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    // Editable draft (only saved when user clicks Save)
    var draft by remember { mutableStateOf(UserProfile()) }
    var draftInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid != null) {
            draftInitialized = false
            isEditing = false
            vm.load(uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    when (val s = state) {
                        is ProfileUiState.Ready -> {
                            if (isEditing) {
                                IconButton(onClick = {
                                    if (uid != null) vm.save(uid, draft) {
                                        isEditing = false
                                        draftInitialized = false
                                    }
                                }) {
                                    Icon(Icons.Filled.Save, contentDescription = "Save")
                                }
                            } else {
                                IconButton(onClick = { isEditing = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
                .widthIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (uid == null) {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Not signed in", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Please log in to view your profile.")
                    }
                }
                return@Column
            }

            when (val s = state) {
                is ProfileUiState.Loading -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    ProfileSkeleton()
                }
                is ProfileUiState.Error -> {
                    ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Couldn’t load profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(s.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { vm.load(uid) }) { Text("Retry") }
                                OutlinedButton(onClick = onBack) { Text("Go back") }
                            }
                        }
                    }
                }
                is ProfileUiState.Ready -> {
                    // Initialize draft once per load, or after saving.
                    if (!draftInitialized) {
                        draft = s.profile
                        draftInitialized = true
                    }

                    // Hero card
                    ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            AvatarCircle(
                                name = if (draft.displayName.isBlank()) (email ?: "User") else draft.displayName,
                                colorLong = draft.avatarColor
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    if (draft.displayName.isBlank()) "Your name" else draft.displayName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    listOf(draft.department, draft.university, draft.level)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • ")
                                        .ifBlank { "Add your department & level" },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!email.isNullOrBlank()) {
                                    Text(email, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Edit form
                    ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("About you", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                            ProfileTextField(
                                label = "Full name",
                                value = draft.displayName,
                                enabled = isEditing,
                                onChange = { draft = draft.copy(displayName = it) }
                            )
                            ProfileTextField(
                                label = "Department",
                                value = draft.department,
                                enabled = isEditing,
                                onChange = { draft = draft.copy(department = it) }
                            )
                            ProfileTextField(
                                label = "University",
                                value = draft.university,
                                enabled = isEditing,
                                onChange = { draft = draft.copy(university = it) }
                            )
                            ProfileTextField(
                                label = "Level",
                                value = draft.level,
                                enabled = isEditing,
                                onChange = { draft = draft.copy(level = it) }
                            )
                            ProfileTextField(
                                label = "Bio (optional)",
                                value = draft.bio,
                                enabled = isEditing,
                                singleLine = false,
                                minLines = 3,
                                onChange = { draft = draft.copy(bio = it) }
                            )

                            HorizontalDivider()

                            Text(
                                "Avatar color",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ColorChips(
                                selected = draft.avatarColor,
                                enabled = isEditing,
                                onSelect = { draft = draft.copy(avatarColor = it) }
                            )

                            if (isEditing) {
                                Button(
                                    onClick = {
                                        vm.save(uid, draft)
                                        isEditing = false
                                        draftInitialized = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Save, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Save changes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    enabled: Boolean,
    singleLine: Boolean = true,
    minLines: Int = 1,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines
    )
}

@Composable
private fun AvatarCircle(name: String, colorLong: Long) {
    val bg = Color(colorLong)
    val initials = remember(name) {
        name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" }
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun ColorChips(selected: Long, enabled: Boolean, onSelect: (Long) -> Unit) {
    val colors = listOf(
        0xFF6750A4,
        0xFF0F6CBD,
        0xFF2E7D32,
        0xFFB3261E,
        0xFF7C4DFF,
        0xFF00897B,
        0xFFF57C00,
        0xFF455A64
    )

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.forEach { c ->
            val isSelected = c == selected
            val border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color(c),
                border = border,
                tonalElevation = if (isSelected) 2.dp else 0.dp,
                onClick = { if (enabled) onSelect(c) },
                enabled = enabled
            ) {}
        }
    }
}

@Composable
private fun ProfileSkeleton() {
    ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.45f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}
