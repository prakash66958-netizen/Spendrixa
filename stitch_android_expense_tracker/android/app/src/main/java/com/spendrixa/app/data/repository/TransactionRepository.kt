package com.spendrixa.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.spendrixa.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        const val DEFAULT_MONTHLY_BUDGET = 12_840.0
    }

    private val userId: String?
        get() = auth.currentUser?.uid

    fun observeTransactions(): Flow<List<Transaction>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .document(uid)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(transactions)
            }

        awaitClose { registration.remove() }
    }

    suspend fun addTransaction(transaction: Transaction): Result<String> {
        return try {
            userId?.let { uid ->
                if (transaction.sourceHash.isNotBlank() && hasTransactionWithSourceHash(uid, transaction.sourceHash)) {
                    return@let Result.success(transaction.sourceHash)
                }

                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("transactions")
                    .add(transaction)
                    .await()
                Result.success(docRef.id)
            } ?: Result.failure(Exception("Not logged in"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeMonthlyBudget(): Flow<Double> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(DEFAULT_MONTHLY_BUDGET)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val monthlyBudget = snapshot?.getDouble("monthlyBudget") ?: DEFAULT_MONTHLY_BUDGET
                trySend(monthlyBudget)
            }

        awaitClose { registration.remove() }
    }

    suspend fun updateMonthlyBudget(amount: Double): Result<Unit> {
        return try {
            userId?.let { uid ->
                firestore.collection("users").document(uid)
                    .set(mapOf("monthlyBudget" to amount), SetOptions.merge())
                    .await()
                Result.success(Unit)
            } ?: Result.failure(Exception("Not logged in"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun hasTransactionWithSourceHash(uid: String, sourceHash: String): Boolean {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("transactions")
            .whereEqualTo("sourceHash", sourceHash)
            .limit(1)
            .get()
            .await()
        return !snapshot.isEmpty
    }
}
