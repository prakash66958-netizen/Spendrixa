package com.spendrixa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.ui.theme.OnSurfaceVariant
import com.spendrixa.app.ui.theme.Primary
import com.spendrixa.app.ui.theme.SurfaceContainer

@Composable
fun SpendrixaBottomBar(
    modifier: Modifier = Modifier,
    currentRoute: String,
    userRole: String? = null,
    onDashboardClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onAdminClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceContainer.copy(alpha = 0.92f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Dashboard",
                isSelected = currentRoute == "dashboard",
                onClick = onDashboardClick
            )
            BottomNavItem(
                icon = Icons.Default.History,
                label = "History",
                isSelected = currentRoute == "history",
                onClick = onHistoryClick
            )
            BottomNavItem(
                icon = Icons.Default.QueryStats,
                label = "Insights",
                isSelected = currentRoute == "insights",
                onClick = onInsightsClick
            )
            if (userRole?.lowercase() == "admin") {
                BottomNavItem(
                    icon = Icons.Default.AdminPanelSettings,
                    label = "Admin",
                    isSelected = currentRoute == "admin_dashboard",
                    onClick = onAdminClick
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                if (isSelected) Primary.copy(alpha = 0.18f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Primary else OnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Primary else OnSurfaceVariant
        )
    }
}
