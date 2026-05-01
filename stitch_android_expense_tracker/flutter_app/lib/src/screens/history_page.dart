import 'package:flutter/material.dart';

import '../models/app_transaction.dart';
import '../theme/app_theme.dart';
import '../widgets/transaction_tile.dart';

class HistoryPage extends StatefulWidget {
  const HistoryPage({
    super.key,
    required this.transactions,
    required this.currencyCode,
  });

  final List<AppTransaction> transactions;
  final String currencyCode;

  @override
  State<HistoryPage> createState() => _HistoryPageState();
}

class _HistoryPageState extends State<HistoryPage> {
  final TextEditingController _searchController = TextEditingController();
  String _filter = 'All';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final String query = _searchController.text.trim().toLowerCase();
    final List<AppTransaction> filtered = widget.transactions.where((AppTransaction item) {
      final bool matchesQuery = query.isEmpty ||
          item.merchant.toLowerCase().contains(query) ||
          item.note.toLowerCase().contains(query) ||
          item.category.toLowerCase().contains(query);
      final bool matchesFilter = switch (_filter) {
        'Expenses' => item.type == AppTransaction.expenseType,
        'Income' => item.type == AppTransaction.incomeType,
        'Subscriptions' => item.category == 'SUBSCRIPTION',
        _ => true,
      };
      return matchesQuery && matchesFilter;
    }).toList(growable: false);

    return Padding(
      padding: const EdgeInsets.fromLTRB(24, 8, 24, 32),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'Search and filter your transactions in one place.',
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _searchController,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Search merchants, notes, or categories',
              prefixIcon: const Icon(Icons.search_rounded),
              suffixIcon: _searchController.text.isEmpty
                  ? const Icon(Icons.tune_rounded)
                  : IconButton(
                      onPressed: () {
                        _searchController.clear();
                        setState(() {});
                      },
                      icon: const Icon(Icons.close_rounded),
                    ),
            ),
          ),
          const SizedBox(height: 16),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: <String>['All', 'Expenses', 'Income', 'Subscriptions']
                  .map(
                    (String filter) => Padding(
                      padding: const EdgeInsets.only(right: 12),
                      child: FilterChip(
                        selected: _filter == filter,
                        label: Text(filter),
                        onSelected: (_) {
                          setState(() {
                            _filter = filter;
                          });
                        },
                        selectedColor: AppTheme.tertiary.withValues(alpha: 0.25),
                      ),
                    ),
                  )
                  .toList(growable: false),
            ),
          ),
          const SizedBox(height: 18),
          Expanded(
            child: filtered.isEmpty
                ? const Center(
                    child: Text(
                      'No transactions match the current filter.',
                      style: TextStyle(color: AppTheme.onSurfaceVariant),
                    ),
                  )
                : ListView.separated(
                    itemCount: filtered.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 12),
                    itemBuilder: (BuildContext context, int index) {
                      return TransactionTile(
                        transaction: filtered[index],
                        currencyCode: widget.currencyCode,
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
