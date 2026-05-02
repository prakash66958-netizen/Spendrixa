package com.spendrixa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.spendrixa.app.data.model.Transaction
import com.spendrixa.app.data.model.UserAccount
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeAllUsers(): Flow<List<UserAccount>> = callbackFlow {
        val registration = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val username = doc.getString("username") ?: ""
                    val role = doc.getString("role") ?: "user"
                    val budget = doc.getDouble("monthlyBudget") ?: 0.0
                    val currency = doc.getString("currency") ?: "USD"
                    UserAccount(id = doc.id, name = name, email = email, username = username, role = role, monthlyBudget = budget, currency = currency)
                }.orEmpty()
                trySend(users)
            }

        awaitClose { registration.remove() }
    }

    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAllTransactions(): Flow<List<Triple<String, String, Transaction>>> = callbackFlow {
        val registration = firestore.collection("users")
            .addSnapshotListener { usersSnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (usersSnapshot == null || usersSnapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val allTxns = mutableListOf<Triple<String, String, Transaction>>()
                var processedUsers = 0

                usersSnapshot.documents.forEach { userDoc ->
                    val currency = userDoc.getString("currency") ?: "USD"
                    userDoc.reference.collection("transactions").get()
                        .addOnSuccessListener { txnSnapshot ->
                            txnSnapshot.documents.mapNotNull { doc ->
                                val txn = doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                                if (txn != null) allTxns.add(Triple(userDoc.id, currency, txn))
                            }
                            processedUsers++
                            if (processedUsers == usersSnapshot.size()) {
                                trySend(allTxns.sortedByDescending { it.third.date })
                            }
                        }
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun deleteTransaction(userId: String, transactionId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("transactions").document(transactionId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(userId: String, transactionId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("transactions").document(transactionId)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGlobalStats(): Result<Map<String, Any>> {
        return try {
            val usersSnapshot = firestore.collection("users").get().await()
            val totalUsers = usersSnapshot.size()

            var totalTxns = 0
            var totalRevenue = 0.0

            // Manually aggregate to avoid collectionGroup index
            for (userDoc in usersSnapshot.documents) {
                val txnSnapshot = userDoc.reference.collection("transactions").get().await()
                totalTxns += txnSnapshot.size()
                for (txnDoc in txnSnapshot.documents) {
                    if (txnDoc.getString("type") == "INCOME") {
                        totalRevenue += txnDoc.getDouble("amount") ?: 0.0
                    }
                }
            }

            Result.success(mapOf(
                "totalUsers" to totalUsers,
                "totalTransactions" to totalTxns,
                "totalRevenue" to totalRevenue
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
