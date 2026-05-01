import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../models/app_transaction.dart';
import '../services/transaction_repository.dart';
import '../theme/app_theme.dart';

class TransactionTile extends StatelessWidget {
  const TransactionTile({
    super.key,
    required this.transaction,
    required this.currencyCode,
    this.showDate = true,
  });

  final AppTransaction transaction;
  final String currencyCode;
  final bool showDate;

  static final DateFormat _dateFormat = DateFormat('MMM dd, hh:mm a');

  @override
  Widget build(BuildContext context) {
    final bool isIncome = transaction.type == AppTransaction.incomeType;
    final ThemeData theme = Theme.of(context);
    final NumberFormat currency = TransactionRepository.currencyFormat(currencyCode);

    return Card(
      color: AppTheme.surfaceLow,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: Container(
          width: 52,
          height: 52,
          decoration: BoxDecoration(
            color: AppTheme.surfaceHighest,
            borderRadius: BorderRadius.circular(16),
          ),
          child: Icon(_categoryIcon(transaction.category), color: AppTheme.primary),
        ),
        title: Text(
          transaction.displayTitle,
          style: theme.textTheme.titleMedium,
        ),
        subtitle: Text(
          showDate
              ? '${prettifyLabel(transaction.category)} • ${_dateFormat.format(transaction.date)}'
              : prettifyLabel(transaction.category),
          style: theme.textTheme.bodyMedium,
        ),
        trailing: Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              '${isIncome ? '+' : '-'}${currency.format(transaction.amount)}',
              style: theme.textTheme.titleMedium?.copyWith(
                color: isIncome ? AppTheme.tertiary : AppTheme.onSurface,
              ),
            ),
            if (transaction.isVerified)
              const Text(
                'Verified',
                style: TextStyle(
                  color: AppTheme.tertiary,
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                ),
              ),
          ],
        ),
      ),
    );
  }

  IconData _categoryIcon(String category) {
    switch (category) {
      case 'FOOD':
        return Icons.restaurant_rounded;
      case 'TRANSPORT':
        return Icons.directions_car_rounded;
      case 'SHOPPING':
        return Icons.shopping_bag_rounded;
      case 'GAMING':
        return Icons.sports_esports_rounded;
      case 'GYM':
        return Icons.fitness_center_rounded;
      case 'ENTERTAINMENT':
        return Icons.movie_rounded;
      case 'TECHNOLOGY':
        return Icons.cloud_rounded;
      case 'HEALTH':
        return Icons.local_hospital_rounded;
      case 'TRAVEL':
        return Icons.flight_rounded;
      case 'SUBSCRIPTION':
        return Icons.subscriptions_rounded;
      case 'HOUSING':
        return Icons.home_rounded;
      case 'INCOME':
        return Icons.account_balance_wallet_rounded;
      default:
        return Icons.receipt_long_rounded;
    }
  }
}
