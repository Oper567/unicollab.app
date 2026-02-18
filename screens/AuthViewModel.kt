package com.unicollabapp.ui.nav.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicollabapp.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun updateEmail(v: String) { _state.value = _state.value.copy(email = v, error = null) }
    fun updatePassword(v: String) { _state.value = _state.value.copy(password = v, error = null) }

    fun isLoggedIn(): Boolean = repo.currentUser() != null

    fun logout() = repo.logout()

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                repo.login(_state.value.email, _state.value.password)
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                repo.register(_state.value.email, _state.value.password)
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Register failed"
                )
            }
        }
    }
}
