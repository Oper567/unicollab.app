package com.unicollabapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unicollabapp.data.auth.AuthRepository
import com.unicollabapp.data.groups.GroupsRepository
import com.unicollabapp.data.users.UserRepository
import com.unicollabapp.ui.nav.screens.AuthViewModel
import com.unicollabapp.ui.profile.ProfileViewModel
import com.unicollabapp.ui.screens.GroupsViewModel

class ViewModelFactory(
    private val groupsRepo: GroupsRepository,
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository = UserRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepo) as T
            }
            modelClass.isAssignableFrom(GroupsViewModel::class.java) -> {
                GroupsViewModel(groupsRepo) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}