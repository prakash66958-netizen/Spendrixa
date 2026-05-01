import 'package:flutter/material.dart';

import '../models/app_transaction.dart';
import '../services/transaction_repository.dart';
import '../theme/app_theme.dart';

class AddTransactionScreen extends StatefulWidget {
  const AddTransactionScreen({
    super.key,
    required this.userId,
    required this.repository,
    required this.currencyCode,
  });

  final String userId;
  final TransactionRepository repository;
  final String currencyCode;

  @override
  State<AddTransactionScreen> createState() => _AddTransactionScreenState();
}

class _AddTransactionScreenState extends State<AddTransactionScreen> {
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  final TextEditingController _amountController = TextEditingController();
  final TextEditingController _merchantController = TextEditingController();
  final TextEditingController _noteController = TextEditingController();
  String _type = AppTransaction.expenseType;
  String _category = AppTransaction.expenseCategories.first;
  bool _isSaving = false;

  @override
  void dispose() {
    _amountController.dispose();
    _merchantController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _isSaving = true;
    });

    final double amount = double.parse(_amountController.text.trim());
    final String merchant = _merchantController.text.trim();
    final String note = _noteController.text.trim();

    final AppTransaction transaction = AppTransaction(
      id: '',
      amount: amount,
      category: _type == AppTransaction.incomeType ? 'INCOME' : _category,
      type: _type,
      note: note,
      merchant: merchant,
      date: DateTime.now(),
      paymentMethod: 'Manual entry',
      isVerified: _type == AppTransaction.incomeType,
      points: 0,
      status: _type == AppTransaction.incomeType ? 'VERIFIED' : 'COMPLETED',
    );

    try {
      await widget.repository.addTransaction(widget.userId, transaction);
      if (!mounted) {
        return;
      }
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Transaction saved.')),
      );
    } catch (_) {
      if (!mounted) {
        return;
      }
      setState(() {
        _isSaving = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Could not save transaction.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final List<String> categories = _type == AppTransaction.incomeType
        ? const <String>['INCOME']
        : AppTransaction.expenseCategories;

    if (!categories.contains(_category)) {
      _category = categories.first;
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Add Transaction'),
      ),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.all(24),
            children: <Widget>[
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text('Transaction Type', style: theme.textTheme.titleMedium),
                      const SizedBox(height: 12),
                      SegmentedButton<String>(
                        segments: const <ButtonSegment<String>>[
                          ButtonSegment<String>(
                            value: AppTransaction.expenseType,
                            label: Text('Expense'),
                            icon: Icon(Icons.arrow_upward_rounded),
                          ),
                          ButtonSegment<String>(
                            value: AppTransaction.incomeType,
                            label: Text('Income'),
                            icon: Icon(Icons.arrow_downward_rounded),
                          ),
                        ],
                        selected: <String>{_type},
                        onSelectionChanged: (Set<String> value) {
                          setState(() {
                            _type = value.first;
                            _category = _type == AppTransaction.incomeType
                                ? 'INCOME'
                                : AppTransaction.expenseCategories.first;
                          });
                        },
                      ),
                      const SizedBox(height: 20),
                      TextFormField(
                        controller: _amountController,
                        keyboardType: const TextInputType.numberWithOptions(decimal: true),
                        decoration: InputDecoration(
                          labelText: 'Amount (${widget.currencyCode})',
                          prefixIcon: const Icon(Icons.attach_money_rounded),
                        ),
                        validator: (String? value) {
                          final double? amount = double.tryParse((value ?? '').trim());
                          if (amount == null || amount <= 0) {
                            return 'Enter a valid amount.';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),
                      DropdownButtonFormField<String>(
                        key: ValueKey<String>(_type),
                        initialValue: _category,
                        decoration: const InputDecoration(
                          labelText: 'Category',
                          prefixIcon: Icon(Icons.category_rounded),
                        ),
                        items: categories
                            .map(
                              (String category) => DropdownMenuItem<String>(
                                value: category,
                                child: Text(prettifyLabel(category)),
                              ),
                            )
                            .toList(growable: false),
                        onChanged: (String? value) {
                          if (value == null) {
                            return;
                          }
                          setState(() {
                            _category = value;
                          });
                        },
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: _merchantController,
                        decoration: const InputDecoration(
                          labelText: 'Merchant or source',
                          prefixIcon: Icon(Icons.storefront_rounded),
                        ),
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: _noteController,
                        minLines: 2,
                        maxLines: 4,
                        decoration: const InputDecoration(
                          labelText: 'Note',
                          prefixIcon: Icon(Icons.notes_rounded),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 20),
              Card(
                color: AppTheme.primaryContainer,
                child: const Padding(
                  padding: EdgeInsets.all(16),
                  child: Text(
                    'This Flutter app writes to the same Firestore collections as your original Android build.',
                  ),
                ),
              ),
              const SizedBox(height: 20),
              FilledButton(
                onPressed: _isSaving ? null : _save,
                style: FilledButton.styleFrom(
                  backgroundColor: AppTheme.primary,
                  foregroundColor: const Color(0xFF0B3542),
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
                child: _isSaving
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Text('Save Transaction'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
