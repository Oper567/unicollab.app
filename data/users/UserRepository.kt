package com.unicollabapp.data.users

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun doc(uid: String) = db.collection("users").document(uid)

    suspend fun getProfile(uid: String): UserProfile {
        val snap = doc(uid).get().await()
        if (!snap.exists()) {
            // Create a minimal profile so the UI always has something.
            val created = UserProfile(uid = uid, updatedAt = Timestamp.now())
            doc(uid).set(created.toMap()).await()
            return created
        }

        return UserProfile(
            uid = uid,
            displayName = snap.getString("displayName") ?: "",
            department = snap.getString("department") ?: "",
            university = snap.getString("university") ?: "",
            level = snap.getString("level") ?: "",
            bio = snap.getString("bio") ?: "",
            avatarColor = snap.getLong("avatarColor") ?: 0xFF6750A4,
            updatedAt = snap.getTimestamp("updatedAt")
        )
    }

    suspend fun upsertProfile(uid: String, profile: UserProfile) {
        doc(uid).set(profile.copy(uid = uid, updatedAt = Timestamp.now()).toMap()).await()
    }

    suspend fun updateFields(uid: String, fields: Map<String, Any>) {
        val map = fields.toMutableMap()
        map["updatedAt"] = Timestamp.now()
        doc(uid).set(map, com.google.firebase.firestore.SetOptions.merge()).await()
    }
}

private fun UserProfile.toMap(): Map<String, Any?> = mapOf(
    "uid" to uid,
    "displayName" to displayName,
    "department" to department,
    "university" to university,
    "level" to level,
    "bio" to bio,
    "avatarColor" to avatarColor,
    "updatedAt" to (updatedAt ?: Timestamp.now())
)
