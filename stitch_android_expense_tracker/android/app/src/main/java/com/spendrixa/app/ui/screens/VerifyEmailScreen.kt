package com.spendrixa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.firebase.FirebaseAuthService
import com.spendrixa.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyEmailScreen(
    authService: FirebaseAuthService,
    onVerified: () -> Unit
) {
    val user = authService.currentUser.collectAsState().value
    var isResending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Periodically check verification status
    LaunchedEffect(Unit) {
        while (true) {
            authService.reloadUser()
            if (user?.isEmailVerified == true) {
                onVerified()
                break
            }
            delay(3000)
        }
    }

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
            Icon(
                imageVector = Icons.Default.MarkEmailUnread,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Check your inbox",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We sent a verification link to\n${user?.email}\n\nPlease click the link to continue.",
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isResending) {
                CircularProgressIndicator(color = Primary)
            } else {
                Button(
                    onClick = {
                        isResending = true
                        scope.launch {
                            authService.resendVerificationEmail()
                                .onSuccess { message = "Email resent!" }
                                .onFailure { e -> message = e.message }
                            isResending = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    Text("Resend Email", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    scope.launch {
                        authService.reloadUser()
                        if (user?.isEmailVerified == true) {
                            onVerified()
                        } else {
                            message = "Email not verified yet"
                        }
                    }
                }
            ) {
                Text("I've verified", color = Primary)
            }

            message?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = if (it.contains("Error", ignoreCase = true) || it.contains("not", ignoreCase = true)) Error else Secondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = { authService.signOut() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign out", color = OnSurfaceVariant)
            }
        }
    }
}
