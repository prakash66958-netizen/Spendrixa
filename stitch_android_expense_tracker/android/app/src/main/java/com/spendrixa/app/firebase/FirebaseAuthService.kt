package com.spendrixa.app.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.spendrixa.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userRole = MutableStateFlow<String?> (null)
    val userRole: StateFlow<String?> = _userRole

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                fetchUserRole(user.uid)
            } else {
                _userRole.value = null
            }
        }
    }

    private fun fetchUserRole(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _userRole.value = document.getString("role") ?: "user"
            }
    }

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                ensureUserProfile(it)
                _currentUser.value = it
                Result.success(it)
            } ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val usernameDoc = firestore.collection("usernames").document(username.lowercase()).get().await()
            if (usernameDoc.exists()) {
                return Result.failure(Exception("Username already taken"))
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                it.sendEmailVerification().await()

                // Save user profile with username
                val profile = mutableMapOf<String, Any>(
                    "email" to email,
                    "name" to username,
                    "username" to username.lowercase(),
                    "role" to "user",
                    "monthlyBudget" to TransactionRepository.DEFAULT_MONTHLY_BUDGET,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(profile).await()

                // Claim username
                firestore.collection("usernames").document(username.lowercase())
                    .set(mapOf("uid" to user.uid)).await()

                _currentUser.value = user
                Result.success(user)
            } ?: Result.failure(Exception("Sign up failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Result<Unit> {
        return try {
            auth.currentUser?.reload()?.await()
            _currentUser.value = auth.currentUser
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureUserProfile(user: FirebaseUser) {
        val document = firestore.collection("users").document(user.uid)
        val snapshot = document.get().await()

        val profile = mutableMapOf<String, Any>(
            "email" to (user.email ?: ""),
            "name" to (user.displayName ?: "")
        )
        if (!snapshot.contains("monthlyBudget")) {
            profile["monthlyBudget"] = TransactionRepository.DEFAULT_MONTHLY_BUDGET
        }

        document.set(profile, SetOptions.merge()).await()
    }
}
