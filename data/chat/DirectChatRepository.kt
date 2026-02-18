package com.unicollabapp.data.chat

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Simple 1-1 chat on Firestore.
 *
 * Data model:
 * chats/{chatId}
 *   - participants: [uidA, uidB]
 *   - updatedAt: server timestamp
 *   messages/{messageId}
 *     - senderUid
 *     - text
 *     - createdAt
 */
class DirectChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun currentUid(): String = auth.currentUser?.uid.orEmpty()

    fun chatIdFor(peerUid: String): String {
        val me = currentUid()
        val a = minOf(me, peerUid)
        val b = maxOf(me, peerUid)
        return "${a}_$b"
    }

    suspend fun sendMessage(peerUid: String, text: String) {
        val me = currentUid()
        require(me.isNotBlank()) { "Not signed in" }
        require(peerUid.isNotBlank()) { "Invalid peer" }

        val chatId = chatIdFor(peerUid)
        val chatRef = db.collection("chats").document(chatId)

        // Ensure chat exists
        chatRef.set(
            mapOf(
                "participants" to listOf(me, peerUid),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()

        chatRef.collection("messages").add(
            mapOf(
                "senderUid" to me,
                "text" to text,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}

// --- small Task->suspend helper (keeps deps minimal) ---
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it, onCancellation = null) }
        addOnFailureListener { cont.resumeWithException(it) }
    }
