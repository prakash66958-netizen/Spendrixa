package com.spendrixa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.firebase.FirebaseAuthService
import com.spendrixa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authService: FirebaseAuthService,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authService.isLoggedIn) {
        if (authService.isLoggedIn) {
            onLoginSuccess()
        }
    }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo and Title
            Text(
                text = "Spendrixa",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSignUp) "Create your account" else "Welcome back",
                fontSize = 16.sp,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant,
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = OnSurfaceVariant,
                    cursorColor = Primary,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = OnSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant,
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = OnSurfaceVariant,
                    cursorColor = Primary,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                ),
                shape = RoundedCornerShape(16.dp)
            )

            if (!isSignUp) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = {
                            if (email.isBlank()) {
                                errorMessage = "Please enter your email address first"
                                return@TextButton
                            }
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            scope.launch {
                                authService.sendPasswordResetEmail(email)
                                    .onSuccess {
                                        successMessage = "Password reset email sent! Check your inbox"
                                    }
                                    .onFailure { e ->
                                        errorMessage = e.message ?: "Failed to send reset email"
                                    }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Forgot Password?",
                            fontSize = 12.sp,
                            color = Primary
                        )
                    }
                }
            }

            // Messages
            if (errorMessage != null || successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: successMessage ?: "",
                    color = if (errorMessage != null) Error else Secondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = if (isSignUp) {
                            authService.signUp(email, password)
                        } else {
                            authService.signIn(email, password)
                        }
                        result.onSuccess {
                            onLoginSuccess()
                        }.onFailure { e ->
                            errorMessage = e.message ?: "Authentication failed"
                        }
                        isLoading = false
                    }
                },
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnPrimary
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Sign Up / Sign In
            TextButton(
                onClick = {
                    isSignUp = !isSignUp
                    errorMessage = null
                }
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    color = Primary
                )
            }
        }
    }
}