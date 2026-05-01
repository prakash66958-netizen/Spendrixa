import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../models/app_transaction.dart';
import '../services/transaction_repository.dart';
import '../theme/app_theme.dart';
import '../widgets/transaction_tile.dart';

class DashboardPage extends StatelessWidget {
  const DashboardPage({
    super.key,
    required this.transactions,
    required this.monthlyBudget,
    required this.currencyCode,
    required this.onUpdateBudget,
  });

  final List<AppTransaction> transactions;
  final double monthlyBudget;
  final String currencyCode;
  final Future<void> Function(double amount) onUpdateBudget;

  @override
  Widget build(BuildContext context) {
    final NumberFormat currency = TransactionRepository.currencyFormat(currencyCode);
    final double totalIncome = transactions
        .where((AppTransaction item) => item.type == AppTransaction.incomeType)
        .fold<double>(0, (double total, AppTransaction item) => total + item.amount);
    final double totalExpense = transactions
        .where((AppTransaction item) => item.type == AppTransaction.expenseType)
        .fold<double>(0, (double total, AppTransaction item) => total + item.amount);
    final double remaining = monthlyBudget - totalExpense;
    final double ratio = monthlyBudget <= 0 ? 0 : (totalExpense / monthlyBudget).clamp(0, 1);
    final List<AppTransaction> recent = transactions.take(5).toList(growable: false);

    return ListView(
      padding: const EdgeInsets.fromLTRB(24, 8, 24, 120),
      children: <Widget>[
        Card(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: <Widget>[
                          Text(
                            monthlyBudget <= 0
                                ? 'No Budget Set'
                                : 'Total Monthly Budget',
                            style: const TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                              color: AppTheme.onSurfaceVariant,
                            ),
                          ),
                          const SizedBox(height: 6),
                          if (monthlyBudget <= 0)
                            Text(
                              'Tap edit to set your budget →',
                              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                color: AppTheme.primary,
                              ),
                            )
                          else
                            Text(
                              currency.format(monthlyBudget),
                              style: Theme.of(context).textTheme.headlineMedium,
                            ),
                        ],
                      ),
                    ),
                    IconButton(
                      onPressed: () => _editBudget(context, monthlyBudget),
                      icon: const Icon(Icons.edit_rounded),
                      tooltip: 'Edit budget',
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: <Widget>[
                    const Text(
                      'Usage intensity',
                      style: TextStyle(color: AppTheme.onSurfaceVariant),
                    ),
                    Text(
                      '${(ratio * 100).round()}% spent',
                      style: const TextStyle(
                        color: AppTheme.primary,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: LinearProgressIndicator(
                    value: ratio,
                    minHeight: 12,
                    backgroundColor: AppTheme.surfaceHighest,
                    valueColor: const AlwaysStoppedAnimation<Color>(AppTheme.primary),
                  ),
                ),
                const SizedBox(height: 16),
                Row(
                  children: <Widget>[
                    Expanded(
                      child: _QuickFigureCard(
                        label: 'Spent',
                        value: currency.format(totalExpense),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: _QuickFigureCard(
                        label: 'Remaining',
                        value: currency.format(remaining),
                        accentColor: AppTheme.tertiary,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        Row(
          children: <Widget>[
            Expanded(
              child: _SummaryCard(
                title: 'Income',
                subtitle: 'Money coming in',
                amount: currency.format(totalIncome),
                icon: Icons.trending_up_rounded,
                accentColor: AppTheme.tertiary,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: _SummaryCard(
                title: 'Expenses',
                subtitle: 'Money going out',
                amount: currency.format(totalExpense),
                icon: Icons.trending_down_rounded,
                accentColor: AppTheme.primary,
              ),
            ),
          ],
        ),
        const SizedBox(height: 24),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: <Widget>[
            Text(
              'Recent Transactions',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            Text(
              '${recent.length} shown',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (recent.isEmpty)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 24),
            child: Text(
              'No transactions yet. Tap the + button to add your first entry.',
              style: TextStyle(color: AppTheme.onSurfaceVariant),
            ),
          )
        else
          ...recent.map(
            (AppTransaction transaction) => Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: TransactionTile(
                transaction: transaction,
                currencyCode: currencyCode,
              ),
            ),
          ),
      ],
    );
  }

  Future<void> _editBudget(BuildContext context, double initialBudget) async {
    final TextEditingController controller = TextEditingController(
      text: initialBudget.toStringAsFixed(0),
    );

    final double? updatedBudget = await showDialog<double>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Update Monthly Budget'),
          content: TextField(
            controller: controller,
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            decoration: const InputDecoration(
              labelText: 'Budget amount',
            ),
          ),
          actions: <Widget>[
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () {
                Navigator.of(context).pop(double.tryParse(controller.text.trim()));
              },
              child: const Text('Save'),
            ),
          ],
        );
      },
    );

    controller.dispose();

    if (updatedBudget == null || updatedBudget < 0 || !context.mounted) {
      return;
    }

    await onUpdateBudget(updatedBudget);
    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Budget updated.')),
      );
    }
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({
    required this.title,
    required this.subtitle,
    required this.amount,
    required this.icon,
    required this.accentColor,
  });

  final String title;
  final String subtitle;
  final String amount;
  final IconData icon;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: AppTheme.surfaceLow,
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: AppTheme.surfaceHighest,
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: accentColor),
            ),
            const SizedBox(height: 14),
            Text(title, style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 4),
            Text(subtitle, style: Theme.of(context).textTheme.bodyMedium),
            const SizedBox(height: 12),
            Text(amount, style: Theme.of(context).textTheme.titleLarge),
          ],
        ),
      ),
    );
  }
}

class _QuickFigureCard extends StatelessWidget {
  const _QuickFigureCard({
    required this.label,
    required this.value,
    this.accentColor = AppTheme.onSurface,
  });

  final String label;
  final String value;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: AppTheme.surfaceLow,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              label,
              style: const TextStyle(
                color: AppTheme.onSurfaceVariant,
                fontSize: 12,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              value,
              style: TextStyle(
                color: accentColor,
                fontSize: 18,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
