package com.unicollabapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicollabapp.data.auth.AuthRepository
import com.unicollabapp.data.groups.GroupsRepository
import com.unicollabapp.ui.ViewModelFactory
import com.unicollabapp.ui.nav.screens.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onGoLogin: () -> Unit,
    onSuccess: () -> Unit,
    vm: AuthViewModel = viewModel(factory = ViewModelFactory(GroupsRepository(), AuthRepository()))
) {
    val state by vm.state.collectAsState()

    val focusManager = LocalFocusManager.current
    var showPassword by remember { mutableStateOf(false) }

    val email = state.email.trim()
    val password = state.password

    val emailValid = email.contains("@") && email.contains(".")
    val passwordValid = password.length >= 6
    val canSubmit = !state.isLoading && emailValid && passwordValid

    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Create your UniCollab account",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Join course groups, share notes, and study smarter with classmates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Email
                            OutlinedTextField(
                                value = state.email,
                                onValueChange = vm::updateEmail,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Email") },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                singleLine = true,
                                isError = state.email.isNotBlank() && !emailValid,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                )
                            )

                            AnimatedVisibility(
                                visible = state.email.isNotBlank() && !emailValid,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    "Enter a valid email address.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            // Password
                            OutlinedTextField(
                                value = state.password,
                                onValueChange = vm::updatePassword,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (showPassword) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                singleLine = true,
                                isError = state.password.isNotBlank() && !passwordValid,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (canSubmit) vm.register(onSuccess)
                                    }
                                )
                            )

                            val passwordHint = when {
                                password.isBlank() -> "Min 6 characters"
                                password.length < 6 -> "Too short (min 6 characters)"
                                else -> "Looks good"
                            }

                            Text(
                                text = passwordHint,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (password.isNotBlank() && password.length >= 6)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AnimatedVisibility(
                                visible = state.error != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(state.error ?: "") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        disabledLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }

                            Spacer(Modifier.height(2.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    vm.register(onSuccess)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                enabled = canSubmit,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text("Creating…", fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text("Create account", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Already have an account? ",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = onGoLogin) { Text("Login") }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "By continuing, you agree to UniCollab’s Terms and Privacy Policy.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
