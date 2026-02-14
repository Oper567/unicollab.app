package com.unicollabapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    vm: AuthVm,
    onGoSignUp: () -> Unit,
    onSuccessToHome: () -> Unit
) {
    val s = vm.state.value
    var showPass by remember { mutableStateOf(false) }

    AuthShell(
        title = "Welcome back 👋",
        subtitle = "Login to continue learning, chatting & joining tournaments."
    ) {
        OutlinedTextField(
            value = s.email,
            onValueChange = vm::onEmail,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = s.password,
            onValueChange = vm::onPassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPass = !showPass }) {
                    Icon(if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                }
            },
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true
        )

        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (s.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        Button(
            onClick = { vm.login(onSuccessToHome) },
            enabled = !s.loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Login") }

        OutlinedButton(
            onClick = onGoSignUp,
            enabled = !s.loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create new account") }
    }
}
