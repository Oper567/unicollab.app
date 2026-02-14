package com.unicollabapp.data.groups

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class GroupsRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun currentUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    suspend fun getGroupById(groupId: String): Group? {
        val doc = db.collection("groups").document(groupId).get().await()
        if (!doc.exists()) return null
        return doc.toObject(Group::class.java)?.copy(id = doc.id)
    }

    suspend fun fetchMyGroups(): List<Group> {
        val uid = currentUid()
        val snap = db.collection("groups")
            .whereArrayContains("members", uid)
            .orderBy("createdAt") // needs index sometimes; if it complains, remove this line
            .get()
            .await()

        return snap.documents.mapNotNull { d ->
            d.toObject(Group::class.java)?.copy(id = d.id)
        }.reversed() // newest first
    }

    suspend fun createGroup(
        name: String,
        uni: String,
        dept: String,
        level: String
    ): String {
        val uid = currentUid()

        val code = generateUniqueJoinCode()

        val data = hashMapOf(
            "name" to name.trim(),
            "uni" to uni.trim(),
            "dept" to dept.trim(),
            "level" to level.trim(),
            "code" to code,
            "ownerId" to uid,
            "members" to listOf(uid),
            "memberCount" to 1,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val ref = db.collection("groups").add(data).await()
        return ref.id
    }

    suspend fun joinGroupByCode(code: String): String {
        val uid = currentUid()
        val clean = code.trim()

        val snap = db.collection("groups")
            .whereEqualTo("code", clean)
            .limit(1)
            .get()
            .await()

        val doc = snap.documents.firstOrNull()
            ?: throw IllegalArgumentException("Invalid group code")

        val groupId = doc.id

        // Prevent duplicate joins
        val currentMembers = (doc.get("members") as? List<*>)?.filterIsInstance<String>().orEmpty()
        if (currentMembers.contains(uid)) {
            return groupId // already a member; treat as success
        }

        // Update members + count
        db.collection("groups").document(groupId)
            .update(
                mapOf(
                    "members" to FieldValue.arrayUnion(uid),
                    "memberCount" to FieldValue.increment(1)
                )
            )
            .await()

        return groupId
    }

    /**
     * Ensures join code uniqueness.
     * Tries multiple times; extremely unlikely to fail.
     */
    private suspend fun generateUniqueJoinCode(maxAttempts: Int = 8): String {
        repeat(maxAttempts) {
            val code = generateJoinCode()
            val exists = db.collection("groups")
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()
                .documents
                .isNotEmpty()

            if (!exists) return code
        }
        throw IllegalStateException("Unable to generate unique code. Try again.")
    }

    private fun generateJoinCode(): String {
        return (100000 + Random.nextInt(900000)).toString() // 6 digits
    }
}
