package com.unicollabapp.ui.screens

import com.unicollabapp.data.groups.Group

sealed interface GroupListUiState {

    /** Initial load */
    data object Loading : GroupListUiState

    /** Loaded but there are no groups */
    data class Empty(
        val message: String = "No groups yet. Create or join one to get started."
    ) : GroupListUiState

    /** Loaded with data */
    data class Success(
        val groups: List<Group>,
        val isRefreshing: Boolean = false,
        val isActionLoading: Boolean = false
    ) : GroupListUiState

    /** Failed to load */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : GroupListUiState
}
