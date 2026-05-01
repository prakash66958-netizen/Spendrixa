import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:intl/intl.dart';

import '../models/app_transaction.dart';

class TransactionRepository {
  TransactionRepository({
    FirebaseFirestore? firestore,
  }) : _firestore = firestore ?? FirebaseFirestore.instance;

  final FirebaseFirestore _firestore;

  static const double defaultMonthlyBudget = 0;
  static const String defaultCurrency = 'USD';

  static const List<String> supportedCurrencies = <String>[
    'USD', 'EUR', 'GBP', 'INR', 'JPY', 'AUD', 'CAD',
  ];

  static const Map<String, String> _currencySymbols = <String, String>{
    'USD': '\$', 'EUR': '€', 'GBP': '£', 'INR': '₹',
    'JPY': '¥', 'AUD': 'A\$', 'CAD': 'C\$',
  };

  static NumberFormat currencyFormat(String code) {
    return NumberFormat.currency(
      symbol: _currencySymbols[code] ?? '\$',
      decimalDigits: code == 'JPY' ? 0 : 2,
    );
  }

  Stream<List<AppTransaction>> watchTransactions(String userId) {
    return _firestore
        .collection('users')
        .doc(userId)
        .collection('transactions')
        .orderBy('date', descending: true)
        .snapshots()
        .map(
          (QuerySnapshot<Map<String, dynamic>> snapshot) => snapshot.docs
              .map(AppTransaction.fromFirestore)
              .toList(growable: false),
        );
  }

  Stream<double> watchMonthlyBudget(String userId) {
    return _firestore.collection('users').doc(userId).snapshots().map(
      (DocumentSnapshot<Map<String, dynamic>> snapshot) {
        return (snapshot.data()?['monthlyBudget'] as num?)?.toDouble() ??
            defaultMonthlyBudget;
      },
    );
  }

  Stream<String> watchCurrency(String userId) {
    return _firestore.collection('users').doc(userId).snapshots().map(
      (DocumentSnapshot<Map<String, dynamic>> snapshot) {
        final String? code = snapshot.data()?['currency'] as String?;
        return (code != null && supportedCurrencies.contains(code))
            ? code
            : defaultCurrency;
      },
    );
  }

  Future<void> updateCurrency(String userId, String currency) {
    return _firestore.collection('users').doc(userId).set(
      <String, dynamic>{'currency': currency},
      SetOptions(merge: true),
    );
  }

  Future<void> ensureUserProfile(User user) async {
    final DocumentReference<Map<String, dynamic>> ref =
        _firestore.collection('users').doc(user.uid);
    final DocumentSnapshot<Map<String, dynamic>> snapshot = await ref.get();

    if (snapshot.exists) {
      // Only update display info, don't touch budget
      await ref.set(
        <String, dynamic>{
          'email': user.email ?? '',
          'name': user.displayName ?? '',
        },
        SetOptions(merge: true),
      );
    } else {
      // New user — set budget to 0 so they configure their own
      await ref.set(<String, dynamic>{
        'email': user.email ?? '',
        'name': user.displayName ?? '',
        'monthlyBudget': defaultMonthlyBudget,
      });
    }
  }

  Future<void> addTransaction(String userId, AppTransaction transaction) {
    return _firestore
        .collection('users')
        .doc(userId)
        .collection('transactions')
        .add(transaction.toFirestore());
  }

  Future<void> updateMonthlyBudget(String userId, double amount) {
    return _firestore.collection('users').doc(userId).set(
      <String, dynamic>{'monthlyBudget': amount},
      SetOptions(merge: true),
    );
  }
}
