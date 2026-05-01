import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/app_transaction.dart';
import '../services/notification_parser.dart';
import '../services/sms_tracker.dart';
import '../services/transaction_repository.dart';
import '../theme/app_theme.dart';
import 'add_transaction_screen.dart';
import 'dashboard_page.dart';
import 'history_page.dart';
import 'insights_page.dart';
import 'settings_page.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({
    super.key,
    required this.user,
    required this.repository,
  });

  final User user;
  final TransactionRepository repository;

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> with WidgetsBindingObserver {
  int _currentIndex = 0;
  bool _smsEnabled = false;
  bool _notifEnabled = false;
  SmsTracker? _smsTracker;
  NotificationParser? _notifParser;

  static const String _smsEnabledKey = 'auto_sms_enabled';
  static const String _notifEnabledKey = 'auto_notif_enabled';
  static const String _onboardingCompleteKey = 'onboarding_complete';
  bool _isCheckingOnboarding = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _loadPrefs();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkOnboarding();
    });
  }

  Future<void> _checkOnboarding() async {
    if (_isCheckingOnboarding) return;
    _isCheckingOnboarding = true;

    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final bool isComplete = prefs.getBool(_onboardingCompleteKey) ?? false;
    
    if (!isComplete && mounted) {
      await _showOnboardingDialog();
    }
    _isCheckingOnboarding = false;
  }

  Future<void> _showOnboardingDialog() async {
    await showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Auto-Track Expenses'),
          content: const Text(
            'Would you like to automatically track your expenses by scanning your SMS and payment app notifications?\n\n'
            'You will be prompted to grant SMS and Notification permissions.',
          ),
          actions: <Widget>[
            TextButton(
              onPressed: () async {
                final SharedPreferences prefs = await SharedPreferences.getInstance();
                await prefs.setBool(_onboardingCompleteKey, true);
                if (context.mounted) Navigator.of(context).pop();
              },
              child: const Text('Skip (Manual)'),
            ),
            FilledButton(
              onPressed: () async {
                final SharedPreferences prefs = await SharedPreferences.getInstance();
                await prefs.setBool(_onboardingCompleteKey, true);
                if (context.mounted) Navigator.of(context).pop();
                await _requestTrackingPermissions();
              },
              child: const Text('Allow Tracking'),
            ),
          ],
        );
      },
    );
  }

  Future<void> _requestTrackingPermissions() async {
    if (!mounted) return;

    final PermissionStatus smsStatus = await Permission.sms.request();
    if (!mounted) return;
    if (smsStatus.isGranted) {
      await _toggleSms(true);
    }

    final bool notifGranted = await NotificationParser.isPermissionGranted();
    if (!notifGranted) {
      await NotificationParser.requestPermission();
    }
    if (!mounted) return;
    final bool notifNowGranted = await NotificationParser.isPermissionGranted();
    if (notifNowGranted) {
      await _toggleNotif(true);
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _notifParser?.stopListening();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      if (_smsEnabled) {
        _smsTracker?.scanInbox();
        _smsTracker?.startListening();
      }
      if (_notifEnabled) {
        _notifParser?.startListening();
      }
    }
  }

  Future<void> _loadPrefs() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final bool sms = prefs.getBool(_smsEnabledKey) ?? false;
    final bool notif = prefs.getBool(_notifEnabledKey) ?? false;
    if (!mounted) return;
    setState(() {
      _smsEnabled = sms;
      _notifEnabled = notif;
    });
    if (sms) _startSmsTracking();
    if (notif) _startNotifTracking();
  }

  void _startSmsTracking() {
    if (_smsTracker != null) return;
    _smsTracker = SmsTracker(userId: widget.user.uid);
    _smsTracker!.scanInbox();
    _smsTracker!.startListening();
  }

  void _startNotifTracking() {
    if (_notifParser != null) return;
    _notifParser = NotificationParser(userId: widget.user.uid);
    _notifParser!.startListening();
  }

  Future<void> _toggleSms(bool enabled) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_smsEnabledKey, enabled);
    if (!mounted) return;
    setState(() {
      _smsEnabled = enabled;
    });
    if (enabled) {
      _startSmsTracking();
    }
  }

  Future<void> _toggleNotif(bool enabled) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_notifEnabledKey, enabled);
    if (!mounted) return;
    setState(() {
      _notifEnabled = enabled;
    });
    if (enabled) {
      _startNotifTracking();
    } else {
      _notifParser?.stopListening();
    }
  }

  Future<void> _openAddTransaction(String currencyCode) async {
    await Navigator.of(context).push<void>(
      MaterialPageRoute<void>(
        builder: (BuildContext context) => AddTransactionScreen(
          userId: widget.user.uid,
          repository: widget.repository,
          currencyCode: currencyCode,
        ),
      ),
    );
  }

  Future<void> _signOut() {
    return FirebaseAuth.instance.signOut();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<String>(
      stream: widget.repository.watchCurrency(widget.user.uid),
      initialData: TransactionRepository.defaultCurrency,
      builder: (BuildContext context, AsyncSnapshot<String> currencySnapshot) {
        final String currencyCode =
            currencySnapshot.data ?? TransactionRepository.defaultCurrency;

        return StreamBuilder<double>(
          stream: widget.repository.watchMonthlyBudget(widget.user.uid),
          initialData: TransactionRepository.defaultMonthlyBudget,
          builder: (BuildContext context, AsyncSnapshot<double> budgetSnapshot) {
            return StreamBuilder<List<AppTransaction>>(
              stream: widget.repository.watchTransactions(widget.user.uid),
              initialData: const <AppTransaction>[],
              builder: (
                BuildContext context,
                AsyncSnapshot<List<AppTransaction>> transactionSnapshot,
              ) {
                final List<AppTransaction> transactions =
                    transactionSnapshot.data ?? const <AppTransaction>[];
                final double monthlyBudget =
                    budgetSnapshot.data ?? TransactionRepository.defaultMonthlyBudget;
                final int autoCount = transactions
                    .where((AppTransaction t) => t.status == 'AUTO_DETECTED')
                    .length;

                final List<Widget> pages = <Widget>[
                  DashboardPage(
                    transactions: transactions,
                    monthlyBudget: monthlyBudget,
                    currencyCode: currencyCode,
                    onUpdateBudget: (double amount) {
                      return widget.repository.updateMonthlyBudget(
                        widget.user.uid,
                        amount,
                      );
                    },
                  ),
                  HistoryPage(
                    transactions: transactions,
                    currencyCode: currencyCode,
                  ),
                  InsightsPage(
                    transactions: transactions,
                    currencyCode: currencyCode,
                  ),
                  SettingsPage(
                    smsEnabled: _smsEnabled,
                    notificationEnabled: _notifEnabled,
                    autoDetectedCount: autoCount,
                    onSmsToggle: _toggleSms,
                    onNotificationToggle: _toggleNotif,
                  ),
                ];

                return Scaffold(
                  appBar: AppBar(
                    title: Text(_titleForIndex()),
                    actions: <Widget>[
                      PopupMenuButton<String>(
                        initialValue: currencyCode,
                        tooltip: 'Currency',
                        onSelected: (String code) {
                          widget.repository.updateCurrency(
                            widget.user.uid,
                            code,
                          );
                        },
                        itemBuilder: (BuildContext context) {
                          return TransactionRepository.supportedCurrencies
                              .map(
                                (String code) => PopupMenuItem<String>(
                                  value: code,
                                  child: Text(code),
                                ),
                              )
                              .toList(growable: false);
                        },
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 8),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: <Widget>[
                              const Icon(Icons.currency_exchange_rounded, size: 20),
                              const SizedBox(width: 4),
                              Text(
                                currencyCode,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w600,
                                  fontSize: 13,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      IconButton(
                        onPressed: _signOut,
                        icon: const Icon(Icons.logout_rounded),
                        tooltip: 'Sign out',
                      ),
                    ],
                  ),
                  body: IndexedStack(
                    index: _currentIndex,
                    children: pages,
                  ),
                  floatingActionButton: _currentIndex < 3
                      ? FloatingActionButton(
                          onPressed: () => _openAddTransaction(currencyCode),
                          child: const Icon(Icons.add_rounded),
                        )
                      : null,
                  bottomNavigationBar: NavigationBar(
                    backgroundColor: AppTheme.surface,
                    selectedIndex: _currentIndex,
                    onDestinationSelected: (int index) {
                      setState(() {
                        _currentIndex = index;
                      });
                    },
                    destinations: const <NavigationDestination>[
                      NavigationDestination(
                        icon: Icon(Icons.account_balance_wallet_outlined),
                        selectedIcon: Icon(Icons.account_balance_wallet_rounded),
                        label: 'Dashboard',
                      ),
                      NavigationDestination(
                        icon: Icon(Icons.history_outlined),
                        selectedIcon: Icon(Icons.history_rounded),
                        label: 'History',
                      ),
                      NavigationDestination(
                        icon: Icon(Icons.query_stats_outlined),
                        selectedIcon: Icon(Icons.query_stats_rounded),
                        label: 'Insights',
                      ),
                      NavigationDestination(
                        icon: Icon(Icons.settings_outlined),
                        selectedIcon: Icon(Icons.settings_rounded),
                        label: 'Settings',
                      ),
                    ],
                  ),
                );
              },
            );
          },
        );
      },
    );
  }

  String _titleForIndex() {
    switch (_currentIndex) {
      case 0:
        return 'Vault';
      case 1:
        return 'History';
      case 2:
        return 'Insights';
      case 3:
        return 'Settings';
      default:
        return 'Vault';
    }
  }
}
