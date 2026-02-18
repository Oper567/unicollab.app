package com.unicollabapp.data.wallet

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class WalletSnapshot(
    val uid: String,
    val balance: Long
)

class WalletRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun uid(): String = auth.currentUser?.uid.orEmpty()

    suspend fun getWallet(): WalletSnapshot {
        val me = uid()
        require(me.isNotBlank()) { "Not signed in" }
        val ref = db.collection("wallets").document(me)
        val snap = ref.get().await()
        val bal = snap.getLong("balance") ?: 0L
        return WalletSnapshot(uid = me, balance = bal)
    }

    /**
     * Demo top-up: increments balance and records a transaction.
     * In a real app, this must be driven by a payment provider webhook.
     */
    suspend fun topUp(amount: Long) {
        require(amount > 0) { "Amount must be > 0" }
        val me = uid()
        require(me.isNotBlank()) { "Not signed in" }

        val wRef = db.collection("wallets").document(me)
        wRef.set(mapOf("balance" to FieldValue.increment(amount)), com.google.firebase.firestore.SetOptions.merge()).await()
        wRef.collection("tx").add(
            mapOf(
                "type" to "TOPUP",
                "amount" to amount,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it, onCancellation = null) }
        addOnFailureListener { cont.resumeWithException(it) }
    }
