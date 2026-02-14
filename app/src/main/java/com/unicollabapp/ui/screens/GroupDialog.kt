package com.unicollabapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    val focus = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var uni by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }

    val nameT = name.trim()
    val uniT = uni.trim()
    val deptT = dept.trim()
    val levelT = level.trim()

    val nameOk = nameT.length >= 3
    val uniOk = uniT.isNotBlank()
    val deptOk = deptT.isNotBlank()
    val levelOk = levelT.isNotBlank()

    val canCreate = !isLoading && nameOk && uniOk && deptOk && levelOk

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Create a new group", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                DialogField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Group name",
                    placeholder = "e.g. CSC 201 Study Group",
                    isError = name.isNotBlank() && !nameOk,
                    supportingText = if (name.isNotBlank() && !nameOk) "Name should be at least 3 characters" else null,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )

                DialogField(
                    value = uni,
                    onValueChange = { uni = it },
                    label = "University",
                    isError = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )

                DialogField(
                    value = dept,
                    onValueChange = { dept = it },
                    label = "Department",
                    isError = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )

                // Level: allow only digits (simple filter)
                DialogField(
                    value = level,
                    onValueChange = { input -> level = input.filter { it.isDigit() } },
                    label = "Level (e.g. 100, 200)",
                    isError = level.isNotBlank() && !levelOk,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focus.clearFocus()
                            if (canCreate) onCreate(nameT, uniT, deptT, levelT)
                        }
                    )
                )

                Text(
                    "Tip: Use a clear name so your classmates find it fast.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    focus.clearFocus()
                    onCreate(nameT, uniT, deptT, levelT)
                },
                enabled = canCreate,
                shape = MaterialTheme.shapes.large
            ) {
                if (isLoading) {
                    MiniLoader(text = "Creating…")
                } else {
                    Text("Create", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    val focus = LocalFocusManager.current
    var code by remember { mutableStateOf("") }

    val cleaned = code.trim()
    val valid = cleaned.length == 6 // your join code is 6 digits in repository

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Join a group", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Code: digits only (since you generate 6-digit code)
                DialogField(
                    value = code,
                    onValueChange = { input -> code = input.filter { it.isDigit() }.take(6) },
                    label = "6-digit group code",
                    placeholder = "e.g. 123456",
                    isError = cleaned.isNotBlank() && !valid,
                    supportingText = if (cleaned.isNotBlank() && !valid) "Code must be 6 digits" else null,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focus.clearFocus()
                            if (!isLoading && valid) onJoin(cleaned)
                        }
                    )
                )

                Text(
                    "Ask your class rep or admin for the group code.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    focus.clearFocus()
                    onJoin(cleaned)
                },
                enabled = !isLoading && valid,
                shape = MaterialTheme.shapes.large
            ) {
                if (isLoading) {
                    MiniLoader(text = "Joining…")
                } else {
                    Text("Join", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    isError: Boolean,
    supportingText: String? = null,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { if (placeholder != null) Text(placeholder) },
        singleLine = true,
        isError = isError,
        supportingText = { if (supportingText != null) Text(supportingText) else Text(" ") },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions
    )
}

@Composable
private fun MiniLoader(text: String) {
    CircularProgressIndicator(
        modifier = Modifier.size(18.dp),
        strokeWidth = 2.dp
    )
    Spacer(Modifier.width(10.dp))
    Text(text, fontWeight = FontWeight.SemiBold)
}
