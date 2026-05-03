import 'package:flutter/material.dart';
import '../../services/transaction_repository.dart';
import '../../theme/app_theme.dart';
import '../../models/app_transaction.dart';
import 'package:intl/intl.dart';

class AdminUserDetailPage extends StatelessWidget {
  const AdminUserDetailPage({
    super.key,
    required this.repository,
    required this.userId,
    required this.userName,
  });

  final TransactionRepository repository;
  final String userId;
  final String userName;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(userName),
      ),
      body: StreamBuilder<List<AppTransaction>>(
        stream: repository.watchTransactions(userId),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }

          final transactions = snapshot.data ?? [];

          return CustomScrollView(
            slivers: [
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'User Audit',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: AppTheme.tertiary,
                          letterSpacing: 2,
                        ),
                      ),
                      Text(
                        userName,
                        style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'User ID: $userId',
                        style: const TextStyle(
                          fontSize: 10,
                          color: AppTheme.onSurfaceVariant,
                          fontFamily: 'monospace',
                        ),
                      ),
                      const SizedBox(height: 24),
                      Row(
                        children: [
                          _SummaryMiniCard(
                            label: 'Total Txns',
                            value: transactions.length.toString(),
                            icon: Icons.receipt_long_rounded,
                            color: AppTheme.primary,
                          ),
                          const SizedBox(width: 16),
                          _SummaryMiniCard(
                            label: 'Total Spend',
                            value: TransactionRepository.currencyFormat('USD').format(
                              transactions
                                  .where((t) => t.type == AppTransaction.expenseType)
                                  .fold(0.0, (sum, t) => sum + t.amount),
                            ),
                            icon: Icons.shopping_bag_rounded,
                            color: AppTheme.tertiary,
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
              const SliverPadding(
                padding: EdgeInsets.symmetric(horizontal: 24),
                sliver: SliverToBoxAdapter(
                  child: Text(
                    'Recent Activity',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                ),
              ),
              if (transactions.isEmpty)
                const SliverFillRemaining(
                  child: Center(child: Text('No transactions for this user.')),
                )
              else
                SliverPadding(
                  padding: const EdgeInsets.all(24),
                  sliver: SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        final txn = transactions[index];
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: Card(
                            color: AppTheme.surfaceLow,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(16),
                            ),
                            child: ListTile(
                              title: Text(
                                txn.merchant.isEmpty ? txn.category.name : txn.merchant,
                                style: const TextStyle(fontWeight: FontWeight.bold),
                              ),
                              subtitle: Text(
                                DateFormat('MMM dd, yyyy').format(
                                  DateTime.fromMillisecondsSinceEpoch(txn.date),
                                ),
                              ),
                              trailing: Text(
                                '${txn.type == AppTransaction.incomeType ? '+' : '-'}${TransactionRepository.currencyFormat('USD').format(txn.amount)}',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: txn.type == AppTransaction.incomeType
                                      ? Colors.emerald
                                      : AppTheme.onSurface,
                                ),
                              ),
                            ),
                          ),
                        );
                      },
                      childCount: transactions.length,
                    ),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}

class _SummaryMiniCard extends StatelessWidget {
  const _SummaryMiniCard({
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
  });

  final String label;
  final String value;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppTheme.surfaceLow,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: AppTheme.surfaceHighest.withOpacity(0.5)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: color, size: 20),
            const SizedBox(height: 12),
            Text(
              label,
              style: const TextStyle(
                fontSize: 12,
                color: AppTheme.onSurfaceVariant,
              ),
            ),
            Text(
              value,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
