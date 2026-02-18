package com.unicollabapp.ui.screens

import com.unicollabapp.data.groups.Group

sealed interface GroupDetailsUiState {

    /** First load */
    data object Loading : GroupDetailsUiState

    /** Loaded and stable */
    data class Success(
        val group: Group,
        val isRefreshing: Boolean = false,
        val isUpdating: Boolean = false
    ) : GroupDetailsUiState

    /** Failed to load */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : GroupDetailsUiState
}
