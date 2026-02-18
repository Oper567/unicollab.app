package com.unicollabapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicollabapp.data.groups.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupDetailsViewModel(
    private val repo: GroupsRepository = GroupsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupDetailsUiState>(GroupDetailsUiState.Loading)
    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()

    private var lastGroupId: String? = null

    fun fetchGroup(groupId: String, force: Boolean = false) {
        // Avoid re-fetching same id unless forced (prevents recomposition spam)
        if (!force && lastGroupId == groupId && _uiState.value is GroupDetailsUiState.Success) return

        lastGroupId = groupId
        _uiState.value = GroupDetailsUiState.Loading

        viewModelScope.launch {
            runCatching {
                repo.getGroupById(groupId) ?: throw Exception("Group not found")
            }.onSuccess { group ->
                _uiState.value = GroupDetailsUiState.Success(group)
            }.onFailure { e ->
                _uiState.value = GroupDetailsUiState.Error(e.userMessage())
            }
        }
    }

    fun refresh() {
        val id = lastGroupId ?: return
        fetchGroup(id, force = true)
    }

    fun retry() = refresh()
}

private fun Throwable.userMessage(): String {
    val raw = this.localizedMessage ?: this.message ?: "Something went wrong."
    return when {
        raw.contains("network", ignoreCase = true) -> "Network error. Check your connection."
        raw.contains("permission", ignoreCase = true) -> "Permission denied. Please sign in again."
        raw.contains("not found", ignoreCase = true) -> "This group no longer exists."
        raw.isBlank() -> "Something went wrong."
        else -> raw
    }
}
