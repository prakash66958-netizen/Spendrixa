import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../models/app_transaction.dart';
import '../services/transaction_repository.dart';
import '../theme/app_theme.dart';

class InsightsPage extends StatelessWidget {
  const InsightsPage({
    super.key,
    required this.transactions,
    required this.currencyCode,
  });

  final List<AppTransaction> transactions;
  final String currencyCode;

  @override
  Widget build(BuildContext context) {
    final NumberFormat currency = TransactionRepository.currencyFormat(currencyCode);
    final List<AppTransaction> expenses = transactions
        .where((AppTransaction item) => item.type == AppTransaction.expenseType)
        .toList(growable: false);
    final List<AppTransaction> income = transactions
        .where((AppTransaction item) => item.type == AppTransaction.incomeType)
        .toList(growable: false);
    final double totalExpense = expenses.fold<double>(0, (double total, AppTransaction item) => total + item.amount);
    final double totalIncome = income.fold<double>(0, (double total, AppTransaction item) => total + item.amount);
    final double averageExpense = expenses.isEmpty ? 0 : totalExpense / expenses.length;
    final List<MapEntry<String, double>> categoryBreakdown = _categoryBreakdown(expenses);
    final List<_MonthSummary> monthlyTrend = _buildMonthlyTrend(transactions);
    final double maxMonthlyValue = monthlyTrend.fold<double>(
      1,
      (double current, _MonthSummary item) => [
        current,
        item.income,
        item.expense,
      ].reduce((double a, double b) => a > b ? a : b),
    );

    return ListView(
      padding: const EdgeInsets.fromLTRB(24, 8, 24, 32),
      children: <Widget>[
        Text(
          'A quick read on where your money is going.',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        const SizedBox(height: 16),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    const Icon(Icons.lightbulb_rounded, color: AppTheme.tertiary),
                    const SizedBox(width: 10),
                    Text('Smart Insight', style: Theme.of(context).textTheme.titleMedium),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  _insightMessage(
                    totalExpense: totalExpense,
                    totalIncome: totalIncome,
                    categoryBreakdown: categoryBreakdown,
                  ),
                  style: Theme.of(context).textTheme.bodyLarge,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        Row(
          children: <Widget>[
            Expanded(
              child: _MetricCard(
                label: 'Average Expense',
                value: currency.format(averageExpense),
                icon: Icons.trending_down_rounded,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: _MetricCard(
                label: 'Net Cashflow',
                value: currency.format(totalIncome - totalExpense),
                icon: Icons.trending_up_rounded,
              ),
            ),
          ],
        ),
        const SizedBox(height: 20),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text('Top Categories', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 18),
                if (categoryBreakdown.isEmpty)
                  const Text(
                    'Add some expenses to unlock category trends.',
                    style: TextStyle(color: AppTheme.onSurfaceVariant),
                  )
                else
                  ...categoryBreakdown.take(5).map(
                    (MapEntry<String, double> entry) {
                      final double ratio = totalExpense <= 0 ? 0 : entry.value / totalExpense;
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: <Widget>[
                                Text(prettifyLabel(entry.key)),
                                Text(currency.format(entry.value)),
                              ],
                            ),
                            const SizedBox(height: 8),
                            ClipRRect(
                              borderRadius: BorderRadius.circular(12),
                              child: LinearProgressIndicator(
                                value: ratio,
                                minHeight: 10,
                                backgroundColor: AppTheme.surfaceHighest,
                                valueColor: const AlwaysStoppedAnimation<Color>(
                                  AppTheme.primary,
                                ),
                              ),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text('Monthly Trend', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 6),
                Text(
                  'Income vs expenses for the last six months.',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 18),
                ...monthlyTrend.map(
                  (_MonthSummary summary) => Padding(
                    padding: const EdgeInsets.only(bottom: 16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: <Widget>[
                            Text(summary.label),
                            Text(
                              '${currency.format(summary.income)} / ${currency.format(summary.expense)}',
                              style: const TextStyle(
                                color: AppTheme.onSurfaceVariant,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        _DualBar(
                          incomeRatio: summary.income / maxMonthlyValue,
                          expenseRatio: summary.expense / maxMonthlyValue,
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  List<MapEntry<String, double>> _categoryBreakdown(List<AppTransaction> expenses) {
    final Map<String, double> totals = <String, double>{};
    for (final AppTransaction expense in expenses) {
      totals.update(
        expense.category,
        (double current) => current + expense.amount,
        ifAbsent: () => expense.amount,
      );
    }
    final List<MapEntry<String, double>> entries = totals.entries.toList(growable: false);
    entries.sort((MapEntry<String, double> a, MapEntry<String, double> b) => b.value.compareTo(a.value));
    return entries;
  }

  List<_MonthSummary> _buildMonthlyTrend(List<AppTransaction> items) {
    final DateFormat format = DateFormat('MMM');
    final DateTime now = DateTime.now();
    final List<_MonthSummary> output = <_MonthSummary>[];

    for (int offset = 5; offset >= 0; offset--) {
      final DateTime start = DateTime(now.year, now.month - offset, 1);
      final DateTime end = DateTime(start.year, start.month + 1, 1);
      final Iterable<AppTransaction> monthItems = items.where(
        (AppTransaction item) => item.date.isAfter(start.subtract(const Duration(milliseconds: 1))) &&
            item.date.isBefore(end),
      );
      final double income = monthItems
          .where((AppTransaction item) => item.type == AppTransaction.incomeType)
          .fold<double>(0, (double total, AppTransaction item) => total + item.amount);
      final double expense = monthItems
          .where((AppTransaction item) => item.type == AppTransaction.expenseType)
          .fold<double>(0, (double total, AppTransaction item) => total + item.amount);

      output.add(
        _MonthSummary(
          label: format.format(start),
          income: income,
          expense: expense,
        ),
      );
    }

    return output;
  }

  String _insightMessage({
    required double totalExpense,
    required double totalIncome,
    required List<MapEntry<String, double>> categoryBreakdown,
  }) {
    if (totalExpense == 0) {
      return 'Add a few expenses to unlock deeper spending insights.';
    }
    if (totalIncome < totalExpense) {
      return 'You are spending more than your recorded income. Review recent expenses first.';
    }
    if (categoryBreakdown.isNotEmpty) {
      final MapEntry<String, double> top = categoryBreakdown.first;
      final int percent = ((top.value / totalExpense) * 100).round();
      return '${prettifyLabel(top.key)} is your biggest category at $percent% of total spend.';
    }
    return 'Your spending is currently balanced.';
  }
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({
    required this.label,
    required this.value,
    required this.icon,
  });

  final String label;
  final String value;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: AppTheme.surfaceLow,
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Icon(icon, color: AppTheme.tertiary),
            const SizedBox(height: 14),
            Text(label, style: Theme.of(context).textTheme.bodyMedium),
            const SizedBox(height: 6),
            Text(value, style: Theme.of(context).textTheme.titleLarge),
          ],
        ),
      ),
    );
  }
}

class _DualBar extends StatelessWidget {
  const _DualBar({
    required this.incomeRatio,
    required this.expenseRatio,
  });

  final double incomeRatio;
  final double expenseRatio;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        ClipRRect(
          borderRadius: BorderRadius.circular(10),
          child: LinearProgressIndicator(
            value: incomeRatio.clamp(0, 1),
            minHeight: 10,
            backgroundColor: AppTheme.surfaceHighest,
            valueColor: const AlwaysStoppedAnimation<Color>(AppTheme.primary),
          ),
        ),
        const SizedBox(height: 6),
        ClipRRect(
          borderRadius: BorderRadius.circular(10),
          child: LinearProgressIndicator(
            value: expenseRatio.clamp(0, 1),
            minHeight: 10,
            backgroundColor: AppTheme.surfaceHighest,
            valueColor: const AlwaysStoppedAnimation<Color>(AppTheme.tertiary),
          ),
        ),
      ],
    );
  }
}

class _MonthSummary {
  const _MonthSummary({
    required this.label,
    required this.income,
    required this.expense,
  });

  final String label;
  final double income;
  final double expense;
}
