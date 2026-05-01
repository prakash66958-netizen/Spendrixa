package com.spendrixa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.data.model.Transaction
import com.spendrixa.app.data.model.TransactionCategory
import com.spendrixa.app.data.model.TransactionType
import com.spendrixa.app.data.repository.TransactionRepository
import com.spendrixa.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    repository: TransactionRepository,
    onBack: () -> Unit
) {
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var selectedExpenseCategory by remember { mutableStateOf(TransactionCategory.FOOD) }
    var note by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Spendrixa Platinum Card (...4290)") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val categories = TransactionCategory.values().filter {
        it != TransactionCategory.INCOME
    }
    val amountValue = amount.toDoubleOrNull()
    val effectiveCategory = if (transactionType == TransactionType.INCOME) {
        TransactionCategory.INCOME
    } else {
        selectedExpenseCategory
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 80.dp, bottom = 100.dp)
        ) {
            // Transaction Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .background(SurfaceContainer.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = {
                            transactionType = TransactionType.EXPENSE
                            errorMessage = null
                        },
                        label = { Text("Expense") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer,
                            selectedLabelColor = Primary
                        ),
                        border = null
                    )
                    FilterChip(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = {
                            transactionType = TransactionType.INCOME
                            errorMessage = null
                        },
                        label = { Text("Income") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer,
                            selectedLabelColor = Primary
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Amount Input
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Amount",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                amount = newValue
                                errorMessage = null
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Primary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OnSurface.copy(alpha = 0.3f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (transactionType == TransactionType.EXPENSE) {
                Text(
                    text = "Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose where this expense belongs",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    categories.forEach { category ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedExpenseCategory == category,
                            onClick = { selectedExpenseCategory = category }
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Tertiary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Income transaction",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "Income entries are stored in the Income category automatically.",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date and Note Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InputCard(
                    label = "Date",
                    value = SimpleDateFormat("MMM dd", Locale.US).format(Date()),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
                InputCard(
                    label = "Note",
                    value = note,
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f),
                    onValueChange = { note = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
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
                                .size(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Primary, PrimaryContainer)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = OnPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Payment Method",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = paymentMethod,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Error,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Save Button
            Button(
                onClick = {
                    isSaving = true
                    errorMessage = null
                    val parsedAmount = amountValue ?: 0.0
                    if (parsedAmount > 0) {
                        scope.launch {
                            repository.addTransaction(
                                Transaction(
                                    amount = parsedAmount,
                                    category = effectiveCategory,
                                    type = transactionType,
                                    note = note.trim(),
                                    merchant = note.trim().take(40),
                                    date = System.currentTimeMillis(),
                                    paymentMethod = paymentMethod,
                                    isVerified = transactionType == TransactionType.INCOME
                                )
                            ).onSuccess {
                                onBack()
                            }.onFailure { error ->
                                isSaving = false
                                errorMessage = error.message ?: "Unable to save transaction."
                            }
                        }
                    } else {
                        isSaving = false
                        errorMessage = "Enter a valid amount greater than zero."
                    }
                },
                enabled = amountValue != null && amountValue > 0 && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnPrimary
                    )
                } else {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Transaction",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Top App Bar
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
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = OnSurface
                )
            }
            Text(
                text = "Spendrixa",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = OnSurface,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: TransactionCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (isSelected) PrimaryContainer else SurfaceContainerHigh,
                    RoundedCornerShape(24.dp)
                )
                .then(
                    if (isSelected) Modifier.border(2.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when (category.name.lowercase()) {
                    "food" -> Icons.Default.Restaurant
                    "transport" -> Icons.Default.DirectionsCar
                    "shopping" -> Icons.Default.ShoppingBag
                    "gaming" -> Icons.Default.SportsEsports
                    "gym" -> Icons.Default.FitnessCenter
                    "entertainment" -> Icons.Default.Movie
                    "technology" -> Icons.Default.Cloud
                    "health" -> Icons.Default.LocalHospital
                    "travel" -> Icons.Default.Flight
                    "subscription" -> Icons.Default.Subscriptions
                    "housing" -> Icons.Default.Home
                    else -> Icons.Default.Receipt
                },
                contentDescription = category.name,
                tint = if (isSelected) Primary else OnSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Primary else OnSurfaceVariant
        )
    }
}

@Composable
private fun InputCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (onValueChange != null) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Primary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (value.isEmpty()) {
                                    Text(
                                        text = "Add a description...",
                                        fontSize = 14.sp,
                                        color = Outline
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                }
            }
        }
    }
}
