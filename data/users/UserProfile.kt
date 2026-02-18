package com.unicollabapp.data.users

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val department: String = "",
    val university: String = "",
    val level: String = "",
    val bio: String = "",
    val avatarColor: Long = 0xFF6750A4, // default Material3 purple
    val updatedAt: Timestamp? = null
)
