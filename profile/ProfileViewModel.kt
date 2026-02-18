package com.unicollabapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicollabapp.data.users.UserProfile
import com.unicollabapp.data.users.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Ready(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val repo: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load(uid: String) {
        _state.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                _state.value = ProfileUiState.Ready(repo.getProfile(uid))
            } catch (e: Exception) {
                _state.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun save(uid: String, profile: UserProfile, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.upsertProfile(uid, profile)
                _state.value = ProfileUiState.Ready(profile)
                onDone()
            } catch (e: Exception) {
                _state.value = ProfileUiState.Error(e.message ?: "Failed to save profile")
            }
        }
    }
}
