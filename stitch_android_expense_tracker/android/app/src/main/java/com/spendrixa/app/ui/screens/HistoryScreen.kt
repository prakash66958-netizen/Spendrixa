package com.spendrixa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.data.model.Transaction
import com.spendrixa.app.data.model.TransactionType
import com.spendrixa.app.data.repository.TransactionRepository
import com.spendrixa.app.ui.components.SpendrixaBottomBar
import com.spendrixa.app.ui.theme.Background
import com.spendrixa.app.ui.theme.OnSurface
import com.spendrixa.app.ui.theme.OnSurfaceVariant
import com.spendrixa.app.ui.theme.Outline
import com.spendrixa.app.ui.theme.OutlineVariant
import com.spendrixa.app.ui.theme.Primary
import com.spendrixa.app.ui.theme.PrimaryFixed
import com.spendrixa.app.ui.theme.SurfaceContainerLow
import com.spendrixa.app.ui.theme.Tertiary
import com.spendrixa.app.ui.theme.OnTertiaryFixed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: TransactionRepository,
    onBack: () -> Unit,
    onNavigateDashboard: () -> Unit,
    onNavigateInsights: () -> Unit
) {
    val transactions by repository.observeTransactions().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = remember { listOf("All", "Expenses", "Income", "Subscriptions") }

    val filteredTransactions = remember(transactions, searchQuery, selectedFilter) {
        transactions.filter { transaction ->
            val matchesSearch = searchQuery.isBlank() ||
                transaction.merchant.contains(searchQuery, ignoreCase = true) ||
                transaction.note.contains(searchQuery, ignoreCase = true) ||
                transaction.category.name.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Expenses" -> transaction.type == TransactionType.EXPENSE
                "Income" -> transaction.type == TransactionType.INCOME
                "Subscriptions" -> transaction.category.name == "SUBSCRIPTION"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { transaction ->
            SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date(transaction.date))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 80.dp, bottom = 100.dp)
        ) {
            Column {
                Text(
                    text = "History",
                    fontSize = 12.sp,
                    color = Tertiary,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Transactions",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search merchants, notes, or categories...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Outline)
                },
                trailingIcon = {
                    IconButton(onClick = { if (searchQuery.isNotEmpty()) searchQuery = "" }) {
                        Icon(
                            if (searchQuery.isEmpty()) Icons.Default.Tune else Icons.Default.Close,
                            contentDescription = if (searchQuery.isEmpty()) "Filters" else "Clear search",
                            tint = Outline
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary.copy(alpha = 0.3f),
                    unfocusedBorderColor = SurfaceContainerLow,
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedContainerColor = SurfaceContainerLow,
                    cursorColor = Primary,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Tertiary,
                            selectedLabelColor = OnTertiaryFixed
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                groupedTransactions.forEach { (date, itemsForDate) ->
                    item {
                        Text(
                            text = date,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    items(itemsForDate, key = { it.id.ifBlank { "${it.date}-${it.amount}-${it.merchant}" } }) { transaction ->
                        TransactionHistoryItem(transaction)
                    }
                }

                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (transactions.isEmpty()) {
                                    "No transactions yet."
                                } else {
                                    "No transactions match this filter."
                                },
                                color = OnSurfaceVariant
                            )
                        }
                    }
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
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurface
                )
            }
            Text(
                text = "Spendrixa",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            IconButton(onClick = { if (searchQuery.isNotEmpty()) searchQuery = "" }) {
                Icon(
                    if (searchQuery.isEmpty()) Icons.Default.Search else Icons.Default.Close,
                    contentDescription = "Search",
                    tint = OnSurface
                )
            }
        }

        SpendrixaBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentRoute = "history",
            onDashboardClick = onNavigateDashboard,
            onHistoryClick = { },
            onInsightsClick = onNavigateInsights
        )
    }
}

@Composable
private fun TransactionHistoryItem(transaction: Transaction) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }

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
                        .background(SurfaceContainerLow, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        transactionCategoryIcon(transaction.category.name),
                        contentDescription = null,
                        tint = PrimaryFixed,
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
                        text = "${prettifyCategory(transaction.category.name)} • ${timeFormat.format(Date(transaction.date))}",
                        fontSize = 14.sp,
                        color = OutlineVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                if (transaction.points > 0) {
                    Text(
                        text = "+${transaction.points} pts",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
