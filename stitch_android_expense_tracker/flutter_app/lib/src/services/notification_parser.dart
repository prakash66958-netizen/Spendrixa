import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:notification_listener_service/notification_event.dart';
import 'package:notification_listener_service/notification_listener_service.dart';

import '../models/app_transaction.dart';
import 'sms_parser.dart';

class NotificationParser {
  NotificationParser({
    required String userId,
    FirebaseFirestore? firestore,
  })  : _userId = userId,
        _firestore = firestore ?? FirebaseFirestore.instance;

  final String _userId;
  final FirebaseFirestore _firestore;
  StreamSubscription<ServiceNotificationEvent>? _subscription;

  static const Set<String> _paymentApps = <String>{
    'com.google.android.apps.nbu.paisa.user',
    'com.phonepe.app',
    'net.one97.paytm',
    'in.amazon.mShop.android.shopping',
    'com.whatsapp',
    'in.org.npci.upiapp',
    'com.csam.icici.bank.imobile',
    'com.sbi.SBIFreedomPlus',
    'com.axis.mobile',
    'com.kotak811',
  };

  static final RegExp _paidPattern = RegExp(
    r'(?:paid|sent|debited|spent|charged)\s*(?:Rs\.?|₹|INR)?\s*([\d,]+(?:\.\d{1,2})?)',
    caseSensitive: false,
  );

  static final RegExp _receivedPattern = RegExp(
    r'(?:received|credited|got|cashback)\s*(?:Rs\.?|₹|INR)?\s*([\d,]+(?:\.\d{1,2})?)',
    caseSensitive: false,
  );

  static final RegExp _amountFirst = RegExp(
    r'(?:Rs\.?|₹|INR)\s*([\d,]+(?:\.\d{1,2})?)\s*(?:paid|sent|debited|to|from|received|credited)',
    caseSensitive: false,
  );

  static final RegExp _toMerchant = RegExp(
    r'(?:to|from|at)\s+([A-Za-z][A-Za-z0-9\s\-&.]+?)(?:\s+(?:on|via|using|for|$)|\.|$)',
    caseSensitive: false,
  );

  static Future<bool> isPermissionGranted() {
    return NotificationListenerService.isPermissionGranted();
  }

  static Future<bool> requestPermission() {
    return NotificationListenerService.requestPermission();
  }

  Future<void> startListening() async {
    print('--- NOTIF PARSER: startListening called ---');
    _subscription?.cancel();
    _subscription = NotificationListenerService.notificationsStream.listen(
      _handleNotification,
    );

    // Also scan existing notifications that might have arrived while app was dead
    try {
      final List<ServiceNotificationEvent> active =
          await NotificationListenerService.getActiveNotifications();
      print('--- NOTIF PARSER: Found ${active.length} active notifications ---');
      for (final ServiceNotificationEvent event in active) {
        if (_paymentApps.contains(event.packageName)) {
           print('Found UPI App Notif in Tray: ${event.packageName}');
        }
        await _handleNotification(event);
      }
    } catch (e) {
      print('--- NOTIF PARSER ERROR: $e ---');
    }
  }

  void stopListening() {
    _subscription?.cancel();
    _subscription = null;
  }

  Future<void> _handleNotification(ServiceNotificationEvent event) async {
    final String? pkg = event.packageName;
    if (pkg == null || !_paymentApps.contains(pkg)) return;

    final String title = event.title ?? '';
    final String text = event.content ?? '';
    final String combined = '$title $text';

    print('--- RAW UPI NOTIFICATION ---');
    print('Package: $pkg');
    print('Title: $title');
    print('Text: $text');
    print('Combined: $combined');

    final ParsedNotification? parsed = _parse(combined);
    if (parsed == null) {
      print('❌ FAILED TO PARSE UPI NOTIFICATION');
      return;
    }

    print('✅ PARSED SUCCESSFULLY: ₹${parsed.amount} | Expense: ${parsed.isExpense}');

    final String hash = '${pkg}_${event.id}_${combined.hashCode}';
    final bool alreadySaved = await _isProcessed(hash);
    if (alreadySaved) return;

    final AppTransaction txn = AppTransaction(
      id: '',
      amount: parsed.amount,
      category: parsed.category,
      type: parsed.isExpense
          ? AppTransaction.expenseType
          : AppTransaction.incomeType,
      note: 'Via ${_appName(pkg)}',
      merchant: parsed.merchant,
      date: DateTime.now(),
      paymentMethod: 'Notification Auto-detect',
      isVerified: false,
      points: 0,
      status: 'AUTO_DETECTED',
    );

    await _firestore
        .collection('users')
        .doc(_userId)
        .collection('transactions')
        .add(txn.toFirestore());

    await _markProcessed(hash);
  }

  static ParsedNotification? parseRaw(String text) {
    return _parse(text);
  }

  static ParsedNotification? _parse(String text) {
    bool isExpense = true;
    double? amount;

    RegExpMatch? match = _paidPattern.firstMatch(text);
    if (match != null) {
      isExpense = true;
      amount = double.tryParse(match.group(1)!.replaceAll(',', ''));
    }

    match ??= _receivedPattern.firstMatch(text);
    if (match != null && amount == null) {
      isExpense = false;
      amount = double.tryParse(match.group(1)!.replaceAll(',', ''));
    }

    match ??= _amountFirst.firstMatch(text);
    if (match != null && amount == null) {
      amount = double.tryParse(match.group(1)!.replaceAll(',', ''));
      isExpense = text.toLowerCase().contains('paid') ||
          text.toLowerCase().contains('sent') ||
          text.toLowerCase().contains('debited');
    }

    if (amount == null || amount <= 0) return null;

    String merchant = '';
    final RegExpMatch? merchantMatch = _toMerchant.firstMatch(text);
    if (merchantMatch != null) {
      merchant = merchantMatch.group(1)!.trim();
    }

    final String category = SmsParser.parse(text)?.category ?? 'OTHER';

    return ParsedNotification(
      amount: amount,
      isExpense: isExpense,
      merchant: merchant,
      category: category,
    );
  }

  String _appName(String pkg) {
    const Map<String, String> names = <String, String>{
      'com.google.android.apps.nbu.paisa.user': 'Google Pay',
      'com.phonepe.app': 'PhonePe',
      'net.one97.paytm': 'Paytm',
      'in.amazon.mShop.android.shopping': 'Amazon',
      'com.whatsapp': 'WhatsApp Pay',
      'in.org.npci.upiapp': 'BHIM UPI',
      'com.csam.icici.bank.imobile': 'iMobile',
      'com.sbi.SBIFreedomPlus': 'SBI YONO',
      'com.axis.mobile': 'Axis Mobile',
      'com.kotak811': 'Kotak 811',
    };
    return names[pkg] ?? pkg;
  }

  Future<bool> _isProcessed(String hash) async {
    final DocumentSnapshot<Map<String, dynamic>> doc = await _firestore
        .collection('users')
        .doc(_userId)
        .collection('meta')
        .doc('processedNotifications')
        .get();
    final List<dynamic>? hashes = doc.data()?['hashes'] as List<dynamic>?;
    return hashes?.contains(hash) ?? false;
  }

  Future<void> _markProcessed(String hash) async {
    await _firestore
        .collection('users')
        .doc(_userId)
        .collection('meta')
        .doc('processedNotifications')
        .set(
      <String, dynamic>{
        'hashes': FieldValue.arrayUnion(<String>[hash]),
      },
      SetOptions(merge: true),
    );
  }
}

class ParsedNotification {
  const ParsedNotification({
    required this.amount,
    required this.isExpense,
    this.merchant = '',
    this.category = 'OTHER',
  });

  final double amount;
  final bool isExpense;
  final String merchant;
  final String category;
}
