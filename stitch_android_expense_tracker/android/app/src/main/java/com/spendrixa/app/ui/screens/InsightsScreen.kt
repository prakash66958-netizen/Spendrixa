package com.spendrixa.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.data.model.Transaction
import com.spendrixa.app.data.model.TransactionCategory
import com.spendrixa.app.data.model.TransactionType
import com.spendrixa.app.data.repository.TransactionRepository
import com.spendrixa.app.ui.components.SpendrixaBottomBar
import com.spendrixa.app.ui.theme.Background
import com.spendrixa.app.ui.theme.OnBackground
import com.spendrixa.app.ui.theme.OnSurface
import com.spendrixa.app.ui.theme.OnSurfaceVariant
import com.spendrixa.app.ui.theme.Outline
import com.spendrixa.app.ui.theme.Primary
import com.spendrixa.app.ui.theme.SecondaryContainer
import com.spendrixa.app.ui.theme.SurfaceContainer
import com.spendrixa.app.ui.theme.SurfaceContainerHigh
import com.spendrixa.app.ui.theme.SurfaceContainerHighest
import com.spendrixa.app.ui.theme.SurfaceContainerLow
import com.spendrixa.app.ui.theme.Tertiary
import com.spendrixa.app.ui.theme.TertiaryContainer
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
    repository: TransactionRepository,
    userRole: String?,
    onBack: () -> Unit,
    onNavigateDashboard: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateAdmin: () -> Unit
) {
    val transactions by repository.observeTransactions().collectAsState(initial = emptyList())

    val expenses = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
    }
    val income = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }
    }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val totalIncome = remember(income) { income.sumOf { it.amount } }
    val avgExpense = remember(expenses) {
        if (expenses.isEmpty()) 0.0 else expenses.sumOf { it.amount } / expenses.size
    }
    val categoryBreakdown = remember(expenses) {
        expenses
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { transaction -> transaction.amount } }
            .toList()
            .sortedByDescending { it.second }
    }
    val topCategories = remember(categoryBreakdown) { categoryBreakdown.take(4) }
    val monthlyCashflow = remember(transactions) { buildMonthlyCashflow(transactions) }

    val chartSegments = remember(topCategories, totalExpenses) {
        if (topCategories.isEmpty() || totalExpenses <= 0) {
            listOf(DonutSegment(SurfaceContainerHighest, 1f, "No expenses yet"))
        } else {
            topCategories.mapIndexed { index, (category, amount) ->
                DonutSegment(
                    color = chartColor(index),
                    proportion = (amount / totalExpenses).toFloat(),
                    label = prettifyCategory(category.name)
                )
            }
        }
    }

    val insightMessage = remember(topCategories, totalExpenses, totalIncome) {
        when {
            totalExpenses <= 0 -> "Add a few expenses to unlock category and trend insights."
            totalIncome < totalExpenses -> "You are spending more than you have recorded as income. Review recent expenses first."
            topCategories.isNotEmpty() -> {
                val (category, amount) = topCategories.first()
                val share = if (totalExpenses > 0) ((amount / totalExpenses) * 100).toInt() else 0
                "${prettifyCategory(category.name)} is your top expense at $share% of total spend."
            }
            else -> "Your cashflow is looking balanced."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 80.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Financial Intelligence",
                        fontSize = 12.sp,
                        color = Tertiary,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Trends & Insights",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnBackground
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.QueryStats,
                                contentDescription = null,
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Spending Structure",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        DonutChart(
                            segments = chartSegments,
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Total expenses",
                            fontSize = 12.sp,
                            color = Outline,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.US).format(totalExpenses),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (topCategories.isEmpty()) {
                                LegendItem(color = SurfaceContainerHighest, label = "Add expenses to populate the chart")
                            } else {
                                topCategories.forEachIndexed { index, (category, amount) ->
                                    val percent = if (totalExpenses > 0) ((amount / totalExpenses) * 100).toInt() else 0
                                    LegendItem(
                                        color = chartColor(index),
                                        label = "${prettifyCategory(category.name)} ($percent%)"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceContainerHighest.copy(alpha = 0.75f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .background(TertiaryContainer, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Tertiary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Smart Insight",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = insightMessage,
                                fontSize = 14.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        label = "Avg Expense",
                        value = NumberFormat.getCurrencyInstance(Locale.US).format(avgExpense),
                        icon = Icons.Default.TrendingDown,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Net Cashflow",
                        value = NumberFormat.getCurrencyInstance(Locale.US).format(totalIncome - totalExpenses),
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Monthly Trend",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "Inflow vs outflow over the last 6 months",
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        LineChart(
                            points = monthlyCashflow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Top Categories",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (categoryBreakdown.isEmpty()) {
                            Text(
                                text = "No expense categories to analyze yet.",
                                fontSize = 14.sp,
                                color = OnSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                categoryBreakdown.take(5).forEachIndexed { index, (category, amount) ->
                                    val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0
                                    CategoryProgressItem(
                                        category = category,
                                        amount = amount,
                                        percentage = percentage.toInt(),
                                        color = chartColor(index)
                                    )
                                }
                            }
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceContainerHighest, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = OnSurface
                )
            }
        }

        SpendrixaBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentRoute = "insights",
            userRole = userRole,
            onDashboardClick = onNavigateDashboard,
            onHistoryClick = onNavigateHistory,
            onInsightsClick = { },
            onAdminClick = onNavigateAdmin
        )
    }
}

private data class DonutSegment(
    val color: Color,
    val proportion: Float,
    val label: String
)

private data class MonthlyCashflowPoint(
    val label: String,
    val income: Double,
    val expense: Double
)

@Composable
private fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 36.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        segments.forEach { segment ->
            val sweepAngle = segment.proportion.coerceIn(0f, 1f) * 360f
            drawArc(
                color = segment.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 14.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Outline,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            Icon(icon, contentDescription = null, tint = Tertiary, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun LineChart(
    points: List<MonthlyCashflowPoint>,
    modifier: Modifier = Modifier
) {
    val maxValue = remember(points) {
        points.maxOfOrNull { maxOf(it.income, it.expense) }?.takeIf { it > 0 } ?: 1.0
    }

    Canvas(modifier = modifier) {
        val gridColor = OnSurface.copy(alpha = 0.1f)
        for (i in 1..4) {
            val y = size.height * i / 4
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.size < 2) return@Canvas

        val incomePath = androidx.compose.ui.graphics.Path().apply {
            points.forEachIndexed { index, point ->
                val x = size.width * index / (points.size - 1)
                val y = size.height * (1 - (point.income / maxValue).toFloat())
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = incomePath,
            color = Primary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        val expensePath = androidx.compose.ui.graphics.Path().apply {
            points.forEachIndexed { index, point ->
                val x = size.width * index / (points.size - 1)
                val y = size.height * (1 - (point.expense / maxValue).toFloat())
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = expensePath,
            color = Tertiary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun CategoryProgressItem(
    category: TransactionCategory,
    amount: Double,
    percentage: Int,
    color: Color
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceContainerHighest, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        transactionCategoryIcon(category.name),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = prettifyCategory(category.name),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = "$percentage% of total",
                        fontSize = 12.sp,
                        color = Tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = currencyFormat.format(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = (percentage / 100f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
            trackColor = SurfaceContainerHighest,
            strokeCap = StrokeCap.Round
        )
    }
}

private fun buildMonthlyCashflow(transactions: List<Transaction>): List<MonthlyCashflowPoint> {
    val monthFormat = SimpleDateFormat("MMM", Locale.US)
    val baseCalendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return (5 downTo 0).map { monthsAgo ->
        val calendar = baseCalendar.clone() as Calendar
        calendar.add(Calendar.MONTH, -monthsAgo)
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis

        val monthlyTransactions = transactions.filter { transaction ->
            transaction.date in start until end
        }

        MonthlyCashflowPoint(
            label = monthFormat.format(Date(start)),
            income = monthlyTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
            expense = monthlyTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        )
    }
}

private fun chartColor(index: Int): Color {
    return when (index) {
        0 -> SecondaryContainer
        1 -> Tertiary
        2 -> Primary
        else -> SurfaceContainerHighest
    }
}
