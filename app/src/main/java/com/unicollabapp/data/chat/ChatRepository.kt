package com.unicollabapp.data.chat

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun uid() = auth.currentUser?.uid ?: error("Not logged in")

    suspend fun sendMessage(groupId: String, text: String) {
        val data = mapOf(
            "senderUid" to uid(),
            "text" to text.trim(),
            "createdAt" to Timestamp.now()
        )
        db.collection("groups").document(groupId)
            .collection("messages")
            .add(data).await()
    }
}
