import 'package:cloud_firestore/cloud_firestore.dart';

class AppTransaction {
  const AppTransaction({
    required this.id,
    required this.amount,
    required this.category,
    required this.type,
    required this.note,
    required this.merchant,
    required this.date,
    required this.paymentMethod,
    required this.isVerified,
    required this.points,
    required this.status,
  });

  final String id;
  final double amount;
  final String category;
  final String type;
  final String note;
  final String merchant;
  final DateTime date;
  final String paymentMethod;
  final bool isVerified;
  final int points;
  final String status;

  static const String expenseType = 'EXPENSE';
  static const String incomeType = 'INCOME';
  static const List<String> expenseCategories = <String>[
    'FOOD',
    'TRANSPORT',
    'SHOPPING',
    'GAMING',
    'GYM',
    'ENTERTAINMENT',
    'TECHNOLOGY',
    'LIFESTYLE',
    'HEALTH',
    'TRAVEL',
    'SUBSCRIPTION',
    'HOUSING',
    'OTHER',
  ];

  factory AppTransaction.fromFirestore(
    QueryDocumentSnapshot<Map<String, dynamic>> doc,
  ) {
    final Map<String, dynamic> data = doc.data();
    return AppTransaction(
      id: doc.id,
      amount: (data['amount'] as num?)?.toDouble() ?? 0,
      category: (data['category'] as String?)?.toUpperCase() ?? 'OTHER',
      type: (data['type'] as String?)?.toUpperCase() ?? expenseType,
      note: data['note'] as String? ?? '',
      merchant: data['merchant'] as String? ?? '',
      date: _parseDate(data['date']),
      paymentMethod: data['paymentMethod'] as String? ?? '',
      isVerified: data['isVerified'] as bool? ?? false,
      points: (data['points'] as num?)?.toInt() ?? 0,
      status: (data['status'] as String?)?.toUpperCase() ?? 'COMPLETED',
    );
  }

  Map<String, dynamic> toFirestore() {
    return <String, dynamic>{
      'amount': amount,
      'category': category,
      'type': type,
      'note': note,
      'merchant': merchant,
      'date': date.millisecondsSinceEpoch,
      'paymentMethod': paymentMethod,
      'isVerified': isVerified,
      'points': points,
      'status': status,
    };
  }

  String get displayTitle => merchant.isNotEmpty ? merchant : prettifyLabel(category);

  String get signedAmountLabel {
    final String prefix = type == incomeType ? '+' : '-';
    return '$prefix$amount';
  }

  static DateTime _parseDate(Object? value) {
    if (value is Timestamp) {
      return value.toDate();
    }
    if (value is int) {
      return DateTime.fromMillisecondsSinceEpoch(value);
    }
    if (value is double) {
      return DateTime.fromMillisecondsSinceEpoch(value.toInt());
    }
    return DateTime.now();
  }
}

String prettifyLabel(String raw) {
  final String lower = raw.toLowerCase().replaceAll('_', ' ');
  if (lower.isEmpty) {
    return 'Other';
  }
  return lower
      .split(' ')
      .map((String word) => word.isEmpty ? word : '${word[0].toUpperCase()}${word.substring(1)}')
      .join(' ');
}
