package com.unicollabapp.data.tournaments

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class Tournament(
    val id: String,
    val title: String,
    val entryFee: Long,
    val createdAt: Timestamp?
)

class TournamentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun uid(): String = auth.currentUser?.uid.orEmpty()

    suspend fun createTournament(groupId: String, title: String, entryFee: Long) {
        val me = uid()
        require(me.isNotBlank()) { "Not signed in" }
        require(groupId.isNotBlank()) { "Invalid group" }
        require(title.isNotBlank()) { "Title required" }
        require(entryFee >= 0) { "Entry fee must be >= 0" }

        val ref = db.collection("groups").document(groupId)
            .collection("tournaments")

        ref.add(
            mapOf(
                "title" to title,
                "entryFee" to entryFee,
                "createdBy" to me,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun joinTournament(groupId: String, tournamentId: String) {
        val me = uid()
        require(me.isNotBlank()) { "Not signed in" }
        val pRef = db.collection("groups").document(groupId)
            .collection("tournaments").document(tournamentId)
            .collection("participants").document(me)

        pRef.set(
            mapOf(
                "uid" to me,
                "score" to 0,
                "joinedAt" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it, onCancellation = null) }
        addOnFailureListener { cont.resumeWithException(it) }
    }
