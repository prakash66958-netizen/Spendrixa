import 'dart:async';

import 'package:another_telephony/telephony.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/app_transaction.dart';
import 'sms_parser.dart';

@pragma('vm:entry-point')
Future<void> _backgroundMessageHandler(SmsMessage sms) async {
  await Firebase.initializeApp();

  final SharedPreferences prefs = await SharedPreferences.getInstance();
  final bool isEnabled = prefs.getBool('auto_sms_enabled') ?? false;
  if (!isEnabled) return;

  final String body = sms.body ?? '';
  final String sender = sms.address ?? '';
  if (body.isEmpty || !SmsParser.isBankSms(sender, body)) return;

  final String? userId = prefs.getString('current_user_id');
  if (userId == null || userId.isEmpty) return;

  final ParsedTransaction? parsed = SmsParser.parse(body, sender: sender);
  if (parsed == null) return;

  final DateTime date = sms.date != null
      ? DateTime.fromMillisecondsSinceEpoch(sms.date!)
      : DateTime.now();

  final FirebaseFirestore firestore = FirebaseFirestore.instance;
  final String hash =
      '${sms.date ?? DateTime.now().millisecondsSinceEpoch}_${body.hashCode}';

  final DocumentSnapshot<Map<String, dynamic>> doc = await firestore
      .collection('users')
      .doc(userId)
      .collection('meta')
      .doc('processedSms')
      .get();
  final List<dynamic>? hashes = doc.data()?['hashes'] as List<dynamic>?;
  if (hashes != null && hashes.contains(hash)) return;

  final AppTransaction txn = AppTransaction(
    id: '',
    amount: parsed.amount,
    category: parsed.category,
    type: parsed.isExpense
        ? AppTransaction.expenseType
        : AppTransaction.incomeType,
    note: parsed.refNumber.isNotEmpty ? 'Ref: ${parsed.refNumber}' : '',
    merchant: parsed.merchant,
    date: date,
    paymentMethod: 'SMS Auto-detect',
    isVerified: false,
    points: 0,
    status: 'AUTO_DETECTED',
  );

  await firestore
      .collection('users')
      .doc(userId)
      .collection('transactions')
      .add(txn.toFirestore());

  await firestore
      .collection('users')
      .doc(userId)
      .collection('meta')
      .doc('processedSms')
      .set(
    <String, dynamic>{
      'hashes': FieldValue.arrayUnion(<String>[hash]),
    },
    SetOptions(merge: true),
  );
}

class SmsTracker {
  SmsTracker({
    required String userId,
    FirebaseFirestore? firestore,
  })  : _userId = userId,
        _firestore = firestore ?? FirebaseFirestore.instance;

  final String _userId;
  final FirebaseFirestore _firestore;
  final Telephony _telephony = Telephony.instance;

  static const String _lastScanKey = 'sms_last_scan_ms';
  static const int _scanWindowDays = 7;

  Future<bool> requestPermission() async {
    final bool? granted = await _telephony.requestPhoneAndSmsPermissions;
    return granted ?? false;
  }

  Future<int> scanInbox() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final int lastScan = prefs.getInt(_lastScanKey) ??
        DateTime.now()
            .subtract(const Duration(days: _scanWindowDays))
            .millisecondsSinceEpoch;

    final List<SmsMessage> messages = await _telephony.getInboxSms(
      columns: <SmsColumn>[
        SmsColumn.ADDRESS,
        SmsColumn.BODY,
        SmsColumn.DATE,
      ],
      filter: SmsFilter.where(SmsColumn.DATE)
          .greaterThan(lastScan.toString()),
      sortOrder: <OrderBy>[
        OrderBy(SmsColumn.DATE, sort: Sort.DESC),
      ],
    );

    final Set<String> processed = await _loadProcessedHashes();
    int count = 0;

    for (final SmsMessage sms in messages) {
      final String body = sms.body ?? '';
      final String sender = sms.address ?? '';
      if (body.isEmpty) continue;
      if (!SmsParser.isBankSms(sender, body)) continue;

      final String hash = '${sms.date ?? 0}_${body.hashCode}';
      if (processed.contains(hash)) continue;

      final ParsedTransaction? parsed = SmsParser.parse(body, sender: sender);
      if (parsed == null) continue;

      await _saveTransaction(parsed, sms.date);
      await _markProcessed(hash);
      count++;
    }

    await prefs.setInt(_lastScanKey, DateTime.now().millisecondsSinceEpoch);
    return count;
  }

  Future<void> startListening() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setString('current_user_id', _userId);

    _telephony.listenIncomingSms(
      onNewMessage: _handleIncoming,
      listenInBackground: true,
      onBackgroundMessage: _backgroundMessageHandler,
    );
  }

  Future<void> _handleIncoming(SmsMessage sms) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final bool isEnabled = prefs.getBool('auto_sms_enabled') ?? false;
    if (!isEnabled) return;

    final String body = sms.body ?? '';
    final String sender = sms.address ?? '';
    if (body.isEmpty || !SmsParser.isBankSms(sender, body)) return;

    final String hash = '${sms.date ?? DateTime.now().millisecondsSinceEpoch}_${body.hashCode}';
    final Set<String> processed = await _loadProcessedHashes();
    if (processed.contains(hash)) return;

    final ParsedTransaction? parsed = SmsParser.parse(body, sender: sender);
    if (parsed == null) return;

    await _saveTransaction(parsed, sms.date);
    await _markProcessed(hash);
  }

  Future<void> _saveTransaction(ParsedTransaction parsed, int? dateMs) async {
    final DateTime date = dateMs != null
        ? DateTime.fromMillisecondsSinceEpoch(dateMs)
        : DateTime.now();

    final AppTransaction txn = AppTransaction(
      id: '',
      amount: parsed.amount,
      category: parsed.category,
      type: parsed.isExpense
          ? AppTransaction.expenseType
          : AppTransaction.incomeType,
      note: parsed.refNumber.isNotEmpty ? 'Ref: ${parsed.refNumber}' : '',
      merchant: parsed.merchant,
      date: date,
      paymentMethod: 'SMS Auto-detect',
      isVerified: false,
      points: 0,
      status: 'AUTO_DETECTED',
    );

    await _firestore
        .collection('users')
        .doc(_userId)
        .collection('transactions')
        .add(txn.toFirestore());
  }

  Future<Set<String>> _loadProcessedHashes() async {
    final DocumentSnapshot<Map<String, dynamic>> doc = await _firestore
        .collection('users')
        .doc(_userId)
        .collection('meta')
        .doc('processedSms')
        .get();
    final List<dynamic>? hashes = doc.data()?['hashes'] as List<dynamic>?;
    return hashes?.map((dynamic e) => e.toString()).toSet() ?? <String>{};
  }

  Future<void> _markProcessed(String hash) async {
    await _firestore
        .collection('users')
        .doc(_userId)
        .collection('meta')
        .doc('processedSms')
        .set(
      <String, dynamic>{
        'hashes': FieldValue.arrayUnion(<String>[hash]),
      },
      SetOptions(merge: true),
    );
  }
}


