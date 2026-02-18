package com.unicollabapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicollabapp.data.auth.FirebaseAuthRepository
import kotlinx.coroutines.launch

class AuthVm(
    private val repo: FirebaseAuthRepository
) : ViewModel() {

    var state = androidx.compose.runtime.mutableStateOf(AuthUiState())
        private set

    fun onEmail(v: String) { state.value = state.value.copy(email = v, error = null) }
    fun onPassword(v: String) { state.value = state.value.copy(password = v, error = null) }
    fun onConfirm(v: String) { state.value = state.value.copy(confirm = v, error = null) }

    fun login(onSuccess: () -> Unit) = viewModelScope.launch {
        val s = state.value
        if (!s.email.contains("@")) { state.value = s.copy(error = "Enter a valid email"); return@launch }
        if (s.password.length < 6) { state.value = s.copy(error = "Password must be at least 6 characters"); return@launch }

        state.value = s.copy(loading = true, error = null)
        try {
            repo.login(s.email, s.password)
            state.value = state.value.copy(loading = false)
            onSuccess()
        } catch (e: Exception) {
            state.value = state.value.copy(loading = false, error = e.message ?: "Login failed")
        }
    }

    fun signup(onSuccess: () -> Unit) = viewModelScope.launch {
        val s = state.value
        if (!s.email.contains("@")) { state.value = s.copy(error = "Enter a valid email"); return@launch }
        if (s.password.length < 6) { state.value = s.copy(error = "Password must be at least 6 characters"); return@launch }
        if (s.password != s.confirm) { state.value = s.copy(error = "Passwords do not match"); return@launch }

        state.value = s.copy(loading = true, error = null)
        try {
            repo.signup(s.email, s.password)
            state.value = state.value.copy(loading = false)
            onSuccess()
        } catch (e: Exception) {
            state.value = state.value.copy(loading = false, error = e.message ?: "Sign up failed")
        }
    }
}
