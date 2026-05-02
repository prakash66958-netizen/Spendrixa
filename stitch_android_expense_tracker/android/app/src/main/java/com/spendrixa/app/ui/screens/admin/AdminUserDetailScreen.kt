package com.spendrixa.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.data.model.Transaction
import com.spendrixa.app.data.model.UserAccount
import com.spendrixa.app.data.repository.AdminRepository
import com.spendrixa.app.data.repository.TransactionRepository
import com.spendrixa.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    adminRepository: AdminRepository,
    userId: String,
    onBack: () -> Unit
) {
    var user by remember { mutableStateOf<UserAccount?>(null) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US) }

    LaunchedEffect(userId) {
        // Fetch user basic info from users list
        adminRepository.observeAllUsers().collect { users ->
            user = users.find { it.id == userId }
        }
    }

    LaunchedEffect(userId) {
        // Fetch user transactions
        adminRepository.observeAllTransactions().collect { allTxns ->
            transactions = allTxns.filter { it.first == userId }.map { it.third }
        }
    }

    val userCurrencyFormat = remember(user?.currency) {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            this.currency = Currency.getInstance(user?.currency ?: "USD")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(user?.name ?: "User Details", fontWeight = FontWeight.Bold) },
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
        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    UserHeader(user!!, userCurrencyFormat)
                }

                item {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        Text(
                            text = "No transactions found for this user.",
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(transactions) { transaction ->
                        AdminTransactionItem(
                            userId = userId,
                            transaction = transaction,
                            currencyFormat = userCurrencyFormat,
                            dateFormat = dateFormat,
                            onEdit = { /* Edit logic would go here if needed */ },
                            onDelete = {
                                scope.launch {
                                    adminRepository.deleteTransaction(userId, transaction.id).onSuccess {
                                        snackbarHostState.showSnackbar("Transaction deleted")
                                    }
                                }
                            },
                            onUserClick = { } // Already in detail page
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserHeader(user: UserAccount, currencyFormat: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = Primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(user.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface)
                    Text(user.email, fontSize = 14.sp, color = OnSurfaceVariant)
                    Text("ID: ${user.id}", fontSize = 10.sp, color = OnSurfaceVariant.copy(0.7f))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Monthly Budget", fontSize = 12.sp, color = OnSurfaceVariant)
                    Text(
                        currencyFormat.format(user.monthlyBudget),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
            }
        }
    }
}

