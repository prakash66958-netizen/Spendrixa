import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/app_transaction.dart';
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

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // No background services to resume
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
                  SettingsPage(),
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
