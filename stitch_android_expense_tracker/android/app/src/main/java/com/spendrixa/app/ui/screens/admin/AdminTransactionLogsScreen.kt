package com.spendrixa.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.data.model.Transaction
import com.spendrixa.app.data.model.TransactionType
import com.spendrixa.app.data.repository.AdminRepository
import com.spendrixa.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTransactionLogsScreen(
    adminRepository: AdminRepository,
    onNavigateUser: (String) -> Unit,
    onBack: () -> Unit
) {
    val transactions by adminRepository.observeAllTransactions().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US) }
    var editingTransaction by remember { mutableStateOf<Triple<String, String, Transaction>?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTransactions = remember(transactions, searchQuery) {
        transactions.filter { (userId, _, transaction) ->
            transaction.merchant.contains(searchQuery, ignoreCase = true) ||
            transaction.category.name.contains(searchQuery, ignoreCase = true) ||
            userId.contains(searchQuery, ignoreCase = true) ||
            transaction.id.contains(searchQuery, ignoreCase = true)
        }
    }

    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!.third,
            onDismiss = { editingTransaction = null },
            onConfirm = { updates ->
                scope.launch {
                    adminRepository.updateTransaction(editingTransaction!!.first, editingTransaction!!.third.id, updates)
                        .onSuccess {
                            snackbarHostState.showSnackbar("Transaction updated")
                        }.onFailure {
                            snackbarHostState.showSnackbar("Failed to update transaction")
                        }
                    editingTransaction = null
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Platform Logs", fontWeight = FontWeight.Bold) },
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
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter by merchant, ID, or user...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No matching logs found", color = OnSurfaceVariant)
                        }
                    }
                } else {
                    items(filteredTransactions) { (userId, currency, transaction) ->
                        val currencyFormat = remember(currency) {
                            NumberFormat.getCurrencyInstance(Locale.US).apply {
                                this.currency = Currency.getInstance(currency)
                            }
                        }
                        AdminTransactionItem(
                            userId = userId,
                            transaction = transaction,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat,
                            onEdit = { editingTransaction = Triple(userId, currency, transaction) },
                            onDelete = {
                                scope.launch {
                                    adminRepository.deleteTransaction(userId, transaction.id).onSuccess {
                                        snackbarHostState.showSnackbar("Transaction deleted")
                                    }
                                }
                            },
                            onUserClick = { onNavigateUser(userId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Any>) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var merchant by remember { mutableStateOf(transaction.merchant) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Label") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountDouble = amount.toDoubleOrNull() ?: transaction.amount
                onConfirm(mapOf("amount" to amountDouble, "merchant" to merchant))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminTransactionItem(
    userId: String,
    transaction: Transaction,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUserClick: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant.ifEmpty { transaction.category.name },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    text = "By User: $userId",
                    fontSize = 11.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onUserClick() }
                )
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    fontSize = 10.sp,
                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (transaction.type == TransactionType.INCOME) Tertiary else Error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
