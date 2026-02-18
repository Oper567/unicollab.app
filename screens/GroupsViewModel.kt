package com.unicollabapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicollabapp.data.groups.Group
import com.unicollabapp.data.groups.GroupsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,      // initial screen loading
    val isActionLoading: Boolean = false, // create/join loading
    val error: String? = null
)

class GroupsViewModel(private val repo: GroupsRepository) : ViewModel() {

    private val _state = MutableStateFlow(GroupsUiState(isLoading = true))
    val state = _state.asStateFlow()

    // One-time UI events (snackbar/toast)
    private val _events = Channel<String>(capacity = Channel.BUFFERED)
    val events: Flow<String> = _events.receiveAsFlow()

    init {
        refresh()
    }

    fun refresh() {
        // Avoid spamming refresh while already loading
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.fetchMyGroups() }
                .onSuccess { groups ->
                    _state.update { it.copy(groups = groups, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.userMessage(), isLoading = false) }
                }
        }
    }

    fun createGroup(
        name: String,
        uni: String,
        dept: String,
        level: String,
        onSuccess: () -> Unit
    ) {
        // Prevent double taps
        if (_state.value.isActionLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true, error = null) }

            runCatching {
                repo.createGroup(name, uni, dept, level)
            }.onSuccess {
                _events.trySend("Group created ✅")
                // Keep old list visible while we refresh
                _state.update { it.copy(isActionLoading = false) }
                refreshForce()
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(error = e.userMessage(), isActionLoading = false) }
            }
        }
    }

    fun joinGroup(code: String, onSuccess: () -> Unit) {
        if (_state.value.isActionLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true, error = null) }

            runCatching {
                repo.joinGroupByCode(code)
            }.onSuccess {
                _events.trySend("Joined group 🎉")
                _state.update { it.copy(isActionLoading = false) }
                refreshForce()
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(error = e.userMessage(), isActionLoading = false) }
            }
        }
    }

    /**
     * Refresh even if not in initial loading (used after create/join)
     */
    private fun refreshForce() {
        viewModelScope.launch {
            runCatching { repo.fetchMyGroups() }
                .onSuccess { groups -> _state.update { it.copy(groups = groups, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.userMessage(), isLoading = false) } }
        }
    }
}

/**
 * Converts exceptions to user-friendly messages.
 * Keeps your UI clean and consistent.
 */
private fun Throwable.userMessage(): String {
    val raw = this.localizedMessage ?: this.message ?: "Something went wrong."
    return when {
        raw.contains("network", ignoreCase = true) -> "Network error. Check your connection."
        raw.contains("permission", ignoreCase = true) -> "Permission denied. Please sign in again."
        raw.contains("already", ignoreCase = true) -> raw
        raw.isBlank() -> "Something went wrong."
        else -> raw
    }
}
