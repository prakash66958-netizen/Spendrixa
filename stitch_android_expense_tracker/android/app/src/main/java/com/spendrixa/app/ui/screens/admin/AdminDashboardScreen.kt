package com.spendrixa.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.data.repository.AdminRepository
import com.spendrixa.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminRepository: AdminRepository,
    onNavigateUsers: () -> Unit,
    onNavigateTransactions: () -> Unit,
    onBack: () -> Unit
) {
    var totalUsers by remember { mutableStateOf(0) }
    var totalTransactions by remember { mutableStateOf(0) }
    var totalRevenue by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        adminRepository.getGlobalStats().onSuccess { stats ->
            totalUsers = stats["totalUsers"] as? Int ?: 0
            totalTransactions = stats["totalTransactions"] as? Int ?: 0
            totalRevenue = stats["totalRevenue"] as? Double ?: 0.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnSurface
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "System Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    AdminStatCard(
                        title = "Users",
                        value = totalUsers.toString(),
                        icon = Icons.Default.Group,
                        color = Primary
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AdminStatCard(
                        title = "Transactions",
                        value = totalTransactions.toString(),
                        icon = Icons.Default.ReceiptLong,
                        color = Tertiary
                    )
                }
            }

            AdminStatCard(
                title = "Total Revenue",
                value = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).format(totalRevenue),
                icon = Icons.Default.Payments,
                color = com.spendrixa.app.ui.theme.SecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            AdminMenuButton(
                title = "User Management",
                subtitle = "Manage and audit users",
                icon = Icons.Default.People,
                onClick = onNavigateUsers
            )

            AdminMenuButton(
                title = "Transaction Logs",
                subtitle = "Global financial audit",
                icon = Icons.Default.ReceiptLong,
                onClick = onNavigateTransactions
            )
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = title, fontSize = 14.sp, color = OnSurfaceVariant)
                Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface)
            }
        }
    }
}

@Composable
fun AdminMenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = OnSurfaceVariant, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
        }
    }
}
