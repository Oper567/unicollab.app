package com.unicollabapp.data.friends

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun uid() = auth.currentUser?.uid ?: error("Not logged in")

    suspend fun sendFriendRequest(toUid: String) {
        val fromUid = uid()
        if (toUid == fromUid) return

        val data = mapOf(
            "fromUid" to fromUid,
            "toUid" to toUid,
            "status" to "pending",
            "createdAt" to Timestamp.now()
        )
        db.collection("friend_requests").add(data).await()
    }

    suspend fun acceptRequest(requestId: String, fromUid: String) {
        val myUid = uid()
        db.collection("friend_requests").document(requestId)
            .update("status", "accepted").await()

        // add both sides
        db.collection("friends").document(myUid).collection("items").document(fromUid)
            .set(mapOf("friendUid" to fromUid, "createdAt" to Timestamp.now())).await()

        db.collection("friends").document(fromUid).collection("items").document(myUid)
            .set(mapOf("friendUid" to myUid, "createdAt" to Timestamp.now())).await()
    }

    suspend fun declineRequest(requestId: String) {
        db.collection("friend_requests").document(requestId)
            .update("status", "declined").await()
    }

    suspend fun findUserByEmail(email: String): String? {
        val snap = db.collection("users")
            .whereEqualTo("email", email.trim())
            .limit(1)
            .get().await()
        return snap.documents.firstOrNull()?.id
    }
}
