import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

import '../models/app_transaction.dart';
import '../services/notification_parser.dart';
import '../services/sms_parser.dart';
import '../theme/app_theme.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({
    super.key,
    required this.onSmsToggle,
    required this.onNotificationToggle,
    required this.smsEnabled,
    required this.notificationEnabled,
    required this.autoDetectedCount,
  });

  final bool smsEnabled;
  final bool notificationEnabled;
  final int autoDetectedCount;
  final ValueChanged<bool> onSmsToggle;
  final ValueChanged<bool> onNotificationToggle;

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool _smsGranted = false;
  bool _notifGranted = false;

  @override
  void initState() {
    super.initState();
    _checkPermissions();
  }

  Future<void> _checkPermissions() async {
    final bool sms = await Permission.sms.isGranted;
    final bool notif = await NotificationParser.isPermissionGranted();
    if (!mounted) return;
    setState(() {
      _smsGranted = sms;
      _notifGranted = notif;
    });
  }

  Future<void> _requestSms() async {
    final PermissionStatus status = await Permission.sms.request();
    if (!mounted) return;
    setState(() {
      _smsGranted = status.isGranted;
    });
    if (status.isGranted && !widget.smsEnabled) {
      widget.onSmsToggle(true);
    }
  }

  Future<void> _requestNotification() async {
    await NotificationParser.requestPermission();
    await Future<void>.delayed(const Duration(seconds: 2));
    await _checkPermissions();
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.fromLTRB(24, 8, 24, 120),
      children: <Widget>[
        Text(
          'Control how transactions are tracked automatically.',
          style: theme.textTheme.bodyMedium,
        ),
        const SizedBox(height: 20),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    const Icon(Icons.sms_rounded, color: AppTheme.tertiary),
                    const SizedBox(width: 12),
                    Text('SMS Auto-Tracking', style: theme.textTheme.titleMedium),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  'Automatically detect bank transactions from SMS messages (SBI, HDFC, ICICI, Axis, and more).',
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(height: 12),
                Row(
                  children: <Widget>[
                    _StatusChip(granted: _smsGranted, label: 'SMS Permission'),
                    const Spacer(),
                    if (!_smsGranted)
                      TextButton(
                        onPressed: _requestSms,
                        child: const Text('Grant'),
                      )
                    else
                      Switch(
                        value: widget.smsEnabled,
                        onChanged: widget.onSmsToggle,
                        activeThumbColor: AppTheme.tertiary,
                      ),
                  ],
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    const Icon(Icons.notifications_active_rounded, color: AppTheme.tertiary),
                    const SizedBox(width: 12),
                    Text('Notification Tracking', style: theme.textTheme.titleMedium),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  'Capture transactions from Google Pay, PhonePe, Paytm, and other payment apps.',
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(height: 12),
                Row(
                  children: <Widget>[
                    _StatusChip(granted: _notifGranted, label: 'Notification Access'),
                    const Spacer(),
                    if (!_notifGranted)
                      TextButton(
                        onPressed: _requestNotification,
                        child: const Text('Grant'),
                      )
                    else
                      Switch(
                        value: widget.notificationEnabled,
                        onChanged: widget.onNotificationToggle,
                        activeThumbColor: AppTheme.tertiary,
                      ),
                  ],
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        Card(
          color: AppTheme.surfaceLow,
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Row(
              children: <Widget>[
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: AppTheme.surfaceHighest,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: const Icon(Icons.auto_awesome_rounded, color: AppTheme.primary),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        '${widget.autoDetectedCount} auto-detected',
                        style: theme.textTheme.titleMedium,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Transactions captured automatically',
                        style: theme.textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    const Icon(Icons.widgets_rounded, color: AppTheme.tertiary),
                    const SizedBox(width: 12),
                    Text('Home Screen Widget', style: theme.textTheme.titleMedium),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  'Add the Spendrixa widget to your home screen for quick expense entry. Long-press your home screen → Widgets → Spendrixa.',
                  style: theme.textTheme.bodyMedium,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    const Icon(Icons.bug_report_rounded, color: AppTheme.tertiary),
                    const SizedBox(width: 12),
                    Text('Test SMS Parsing', style: theme.textTheme.titleMedium),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  'Simulate an incoming SMS to verify how the app reads it.',
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(height: 16),
                Row(
                  children: <Widget>[
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => _simulateSms(
                            context,
                            "Rs.450.00 debited from a/c **1234 on 29-04-26 to Zomato. Available balance Rs.10000.00"),
                        child: const Text('Test Expense'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => _simulateSms(
                            context,
                            "Your a/c **1234 is credited with Rs.15,000.00 on 29-Apr by Salary. Available balance Rs.25000.00"),
                        child: const Text('Test Income'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Row(
                  children: <Widget>[
                    Expanded(
                      child: FilledButton.tonal(
                        onPressed: () => _simulateNotification(
                            context,
                            "Paid ₹125 to Starbucks",
                            "Using HDFC Bank ending in 1234",
                            "com.google.android.apps.nbu.paisa.user"), // GPay
                        child: const Text('Test GPay'),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _simulateNotification(BuildContext context, String title, String text, String pkg) async {
    final String combined = '$title $text';
    final ParsedNotification? parsed = NotificationParser.parseRaw(combined);
    if (parsed == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Failed to parse UPI Notification')),
      );
      return;
    }

    final User? user = FirebaseAuth.instance.currentUser;
    if (user == null) return;

    final AppTransaction txn = AppTransaction(
      id: '',
      amount: parsed.amount,
      category: parsed.category,
      type: parsed.isExpense ? AppTransaction.expenseType : AppTransaction.incomeType,
      note: 'Simulated Notification',
      merchant: parsed.merchant,
      date: DateTime.now(),
      paymentMethod: 'UPI Auto-detect',
      isVerified: false,
      points: 0,
      status: 'AUTO_DETECTED',
    );

    await FirebaseFirestore.instance
        .collection('users')
        .doc(user.uid)
        .collection('transactions')
        .add(txn.toFirestore());

    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
              'Parsed as ${parsed.isExpense ? "Expense" : "Income"}: ₹${parsed.amount} for ${parsed.merchant.isEmpty ? parsed.category : parsed.merchant}'),
          backgroundColor: parsed.isExpense ? AppTheme.primary : AppTheme.tertiary,
        ),
      );
    }
  }

  Future<void> _simulateSms(BuildContext context, String body) async {
    final ParsedTransaction? parsed = SmsParser.parse(body, sender: 'HDFCBK');
    if (parsed == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Failed to parse SMS')),
      );
      return;
    }

    final User? user = FirebaseAuth.instance.currentUser;
    if (user == null) return;

    final AppTransaction txn = AppTransaction(
      id: '',
      amount: parsed.amount,
      category: parsed.category,
      type: parsed.isExpense ? AppTransaction.expenseType : AppTransaction.incomeType,
      note: 'Simulated SMS',
      merchant: parsed.merchant,
      date: DateTime.now(),
      paymentMethod: 'SMS Auto-detect',
      isVerified: false,
      points: 0,
      status: 'AUTO_DETECTED',
    );

    await FirebaseFirestore.instance
        .collection('users')
        .doc(user.uid)
        .collection('transactions')
        .add(txn.toFirestore());

    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
              'Parsed as ${parsed.isExpense ? "Expense" : "Income"}: ₹${parsed.amount} for ${parsed.merchant.isEmpty ? parsed.category : parsed.merchant}'),
          backgroundColor: parsed.isExpense ? AppTheme.primary : AppTheme.tertiary,
        ),
      );
    }
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({required this.granted, required this.label});

  final bool granted;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: granted
            ? AppTheme.tertiary.withValues(alpha: 0.15)
            : AppTheme.surfaceHighest,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Icon(
            granted ? Icons.check_circle_rounded : Icons.cancel_rounded,
            size: 14,
            color: granted ? AppTheme.tertiary : AppTheme.onSurfaceVariant,
          ),
          const SizedBox(width: 6),
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: granted ? AppTheme.tertiary : AppTheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}
