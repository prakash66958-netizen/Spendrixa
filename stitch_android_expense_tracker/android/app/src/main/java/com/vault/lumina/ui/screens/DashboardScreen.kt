package com.vault.lumina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vault.lumina.data.model.Transaction
import com.vault.lumina.data.model.TransactionType
import com.vault.lumina.data.repository.TransactionRepository
import com.vault.lumina.ui.components.SpendrixaBottomBar
import com.vault.lumina.ui.theme.Background
import com.vault.lumina.ui.theme.OnPrimary
import com.vault.lumina.ui.theme.OnSurface
import com.vault.lumina.ui.theme.OnSurfaceVariant
import com.vault.lumina.ui.theme.Primary
import com.vault.lumina.ui.theme.PrimaryContainer
import com.vault.lumina.ui.theme.SurfaceContainer
import com.vault.lumina.ui.theme.SurfaceContainerHigh
import com.vault.lumina.ui.theme.SurfaceContainerHighest
import com.vault.lumina.ui.theme.SurfaceContainerLow
import com.vault.lumina.ui.theme.Tertiary
import com.vault.lumina.ui.theme.TertiaryContainer
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    repository: TransactionRepository,
    onAddTransaction: () -> Unit,
    onViewHistory: () -> Unit,
    onViewInsights: () -> Unit,
    onLogout: () -> Unit
) {
    val transactions by repository.observeTransactions().collectAsState(initial = emptyList())
    val monthlyBudget by repository.observeMonthlyBudget()
        .collectAsState(initial = TransactionRepository.DEFAULT_MONTHLY_BUDGET)

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val recentTransactions = remember(transactions) { transactions.take(5) }
    val remaining = remember(monthlyBudget, totalExpense) { monthlyBudget - totalExpense }
    val usageProgress = remember(monthlyBudget, totalExpense) {
        if (monthlyBudget > 0) {
            (totalExpense / monthlyBudget).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    val usagePercent = (usageProgress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 80.dp, bottom = 120.dp)
        ) {
            item {
                HeroBudgetCard(
                    monthlyBudget = monthlyBudget,
                    spentAmount = totalExpense,
                    remaining = remaining,
                    usagePercent = usagePercent,
                    usageProgress = usageProgress
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(
                        title = "Income",
                        subtitle = "Money coming in",
                        amount = totalIncome,
                        icon = Icons.Default.TrendingUp,
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Expenses",
                        subtitle = "Money going out",
                        amount = totalExpense,
                        icon = Icons.Default.TrendingDown,
                        isPositive = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    TextButton(onClick = onViewHistory) {
                        Text(
                            text = "Open History",
                            color = Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            if (recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions yet. Tap + to add your first one.",
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                items(recentTransactions) { transaction ->
                    TransactionItem(transaction = transaction)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onViewHistory) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = OnSurface
                    )
                }
                Text(
                    text = "Spendrixa",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLogout) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = OnSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = OnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        SpendrixaBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentRoute = "dashboard",
            onDashboardClick = { },
            onHistoryClick = onViewHistory,
            onInsightsClick = onViewInsights
        )

        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 120.dp),
            containerColor = Primary,
            contentColor = OnPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }
}

@Composable
private fun HeroBudgetCard(
    monthlyBudget: Double,
    spentAmount: Double,
    remaining: Double,
    usagePercent: Int,
    usageProgress: Float
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total Monthly Budget",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormat.format(monthlyBudget),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SurfaceContainerHigh, CircleShape)
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Budget usage",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "$usagePercent% spent",
                        fontSize = 14.sp,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = usageProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = Primary,
                    trackColor = SurfaceContainerHighest,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickFigureCard(
                    modifier = Modifier.weight(1f),
                    label = "Spent",
                    value = currencyFormat.format(spentAmount)
                )
                QuickFigureCard(
                    modifier = Modifier.weight(1f),
                    label = "Remaining",
                    value = currencyFormat.format(remaining)
                )
            }
        }
    }
}

@Composable
private fun QuickFigureCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    subtitle: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isPositive) TertiaryContainer else SurfaceContainerHighest,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isPositive) Tertiary else Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = currencyFormat.format(amount),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.US) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SurfaceContainerHighest, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        transactionCategoryIcon(transaction.category.name),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = transaction.merchant.ifEmpty { prettifyCategory(transaction.category.name) },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = "${prettifyCategory(transaction.category.name)} • ${dateFormat.format(Date(transaction.date))}",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.INCOME) Tertiary else OnSurface
                )
                if (transaction.isVerified) {
                    Text(
                        text = "Verified",
                        fontSize = 10.sp,
                        color = Tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
