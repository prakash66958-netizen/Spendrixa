import 'package:flutter/material.dart';
import '../../services/transaction_repository.dart';
import '../../theme/app_theme.dart';
import '../../models/app_transaction.dart';
import 'package:intl/intl.dart';

class AdminTransactionLogsPage extends StatelessWidget {
  const AdminTransactionLogsPage({super.key, required this.repository});

  final TransactionRepository repository;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Transaction Logs'),
      ),
      body: StreamBuilder<List<Map<String, dynamic>>>(
        stream: repository.watchGlobalTransactions(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }

          final transactions = snapshot.data ?? [];

          if (transactions.isEmpty) {
            return const Center(child: Text('No transactions found.'));
          }

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: transactions.length,
            separatorBuilder: (context, index) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final txn = transactions[index];
              final amount = (txn['amount'] as num?)?.toDouble() ?? 0.0;
              final type = txn['type'] ?? 'EXPENSE';
              final merchant = txn['merchant'] ?? 'Unknown';
              final date = (txn['date'] as num?)?.toInt() ?? 0;
              final category = txn['category'] ?? 'OTHER';

              final formattedDate = DateFormat('MMM dd, yyyy HH:mm').format(
                DateTime.fromMillisecondsSinceEpoch(date),
              );

              final isIncome = type == 'INCOME';

              return Card(
                color: AppTheme.surfaceLow,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: (isIncome ? Colors.emerald : Colors.red).withOpacity(0.1),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(
                          isIncome ? Icons.arrow_downward_rounded : Icons.arrow_upward_rounded,
                          color: isIncome ? Colors.emerald : Colors.red,
                          size: 20,
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              merchant,
                              style: const TextStyle(fontWeight: FontWeight.bold),
                            ),
                            Text(
                              '$category • $formattedDate',
                              style: const TextStyle(
                                fontSize: 12,
                                color: AppTheme.onSurfaceVariant,
                              ),
                            ),
                          ],
                        ),
                      ),
                      Text(
                        '${isIncome ? '+' : '-'}${TransactionRepository.currencyFormat('USD').format(amount)}',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: isIncome ? Colors.emerald : AppTheme.onSurface,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
