import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/widgets.dart';
import 'package:home_widget/home_widget.dart';

import '../../firebase_options.dart';
import '../models/app_transaction.dart';

@pragma('vm:entry-point')
Future<void> backgroundCallback(Uri? uri) async {
  try {
    WidgetsFlutterBinding.ensureInitialized();
    if (Firebase.apps.isEmpty) {
      await Firebase.initializeApp(
        options: DefaultFirebaseOptions.currentPlatform,
      );
    }

    if (uri == null || uri.scheme != 'quickadd') return;

    // Handle amount selection
    if (uri.host == 'amount') {
      final String amount = uri.queryParameters['value'] ?? '100';
      await HomeWidget.saveWidgetData<String>('selected_amount', amount);
      await HomeWidget.saveWidgetData<String>(
        'last_txn',
        '₹$amount selected — now tap a category',
      );
      await HomeWidget.updateWidget(
        name: 'QuickAddWidgetProvider',
        androidName: 'QuickAddWidgetProvider',
      );
      return;
    }

    // Handle adding a transaction
    if (uri.host != 'add' && uri.host != 'add_custom') return;

    final String category = uri.queryParameters['category'] ?? 'OTHER';
    final String amountStr = uri.queryParameters['amount'] ?? '100';
    final String type = uri.queryParameters['type'] ?? AppTransaction.expenseType;
    final double amount = double.tryParse(amountStr) ?? 100;

    User? user = FirebaseAuth.instance.currentUser;
    if (user == null) {
      user = await FirebaseAuth.instance.authStateChanges().first;
    }

    if (user == null) {
      await HomeWidget.saveWidgetData<String>(
        'last_txn',
        'Please sign in first',
      );
      await HomeWidget.updateWidget(
        name: 'QuickAddWidgetProvider',
        androidName: 'QuickAddWidgetProvider',
      );
      return;
    }

    final AppTransaction txn = AppTransaction(
      id: '',
      amount: amount,
      category: category,
      type: type,
      note: 'Quick add from widget',
      merchant: '',
      date: DateTime.now(),
      paymentMethod: 'Widget Quick-add',
      isVerified: false,
      points: 0,
      status: 'COMPLETED',
    );

    await FirebaseFirestore.instance
        .collection('users')
        .doc(user.uid)
        .collection('transactions')
        .add(txn.toFirestore());

    await HomeWidget.saveWidgetData<String>(
      'last_txn',
      '✅ ₹$amountStr ${prettifyLabel(category)} added!',
    );
    await HomeWidget.updateWidget(
      name: 'QuickAddWidgetProvider',
      androidName: 'QuickAddWidgetProvider',
    );
  } catch (e, stack) {
    await HomeWidget.saveWidgetData<String>(
      'last_txn',
      'Error: ${e.toString().split('\n')[0]}',
    );
    await HomeWidget.updateWidget(
      name: 'QuickAddWidgetProvider',
      androidName: 'QuickAddWidgetProvider',
    );
  }
}

class WidgetHandler {
  static const String _appGroupId = 'com.spendrixa.app';

  static Future<void> initialize() async {
    await HomeWidget.setAppGroupId(_appGroupId);
    await HomeWidget.registerInteractivityCallback(backgroundCallback);
  }

  static Future<void> updateLastTransaction(String text) async {
    await HomeWidget.saveWidgetData<String>('last_txn', text);
    await HomeWidget.updateWidget(
      name: 'QuickAddWidgetProvider',
      androidName: 'QuickAddWidgetProvider',
    );
  }
}
