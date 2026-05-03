import 'package:flutter/material.dart';
import '../../services/transaction_repository.dart';
import '../../theme/app_theme.dart';
import 'admin_user_management_page.dart';
import 'admin_transaction_logs_page.dart';

class AdminDashboardPage extends StatefulWidget {
  const AdminDashboardPage({super.key, required this.repository});

  final TransactionRepository repository;

  @override
  State<AdminDashboardPage> createState() => _AdminDashboardPageState();
}

class _AdminDashboardPageState extends State<AdminDashboardPage> {
  Map<String, dynamic>? _stats;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadStats();
  }

  Future<void> _loadStats() async {
    try {
      final stats = await widget.repository.getGlobalStats();
      if (mounted) {
        setState(() {
          _stats = stats;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _loading = false;
        });
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error loading stats: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        Text(
          'System Overview',
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 16),
        if (_loading)
          const Center(child: CircularProgressIndicator())
        else if (_stats == null)
          const Text('Failed to load stats.')
        else ...[
          Row(
            children: [
              Expanded(
                child: _StatCard(
                  title: 'Users',
                  value: _stats!['totalUsers'].toString(),
                  icon: Icons.group_rounded,
                  color: AppTheme.primary,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'Transactions',
                  value: _stats!['totalTransactions'].toString(),
                  icon: Icons.receipt_long_rounded,
                  color: AppTheme.tertiary,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _StatCard(
            title: 'Total Revenue',
            value: TransactionRepository.currencyFormat('USD')
                .format(_stats!['totalRevenue']),
            icon: Icons.payments_rounded,
            color: AppTheme.secondaryContainer,
            isFullWidth: true,
          ),
        ],
        const SizedBox(height: 32),
        Text(
          'Management',
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 16),
        _AdminMenuButton(
          title: 'User Management',
          subtitle: 'Manage and audit users',
          icon: Icons.people_rounded,
          onClick: () {
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (context) => AdminUserManagementPage(
                  repository: widget.repository,
                ),
              ),
            );
          },
        ),
        const SizedBox(height: 12),
        _AdminMenuButton(
          title: 'Transaction Logs',
          subtitle: 'Global financial audit',
          icon: Icons.history_edu_rounded,
          onClick: () {
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (context) => AdminTransactionLogsPage(
                  repository: widget.repository,
                ),
              ),
            );
          },
        ),
      ],
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({
    required this.title,
    required this.value,
    required this.icon,
    required this.color,
    this.isFullWidth = false,
  });

  final String title;
  final String value;
  final IconData icon;
  final Color color;
  final bool isFullWidth;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: AppTheme.surfaceLow,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: color.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(icon, color: color, size: 28),
            ),
            const SizedBox(width: 20),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppTheme.onSurfaceVariant,
                    ),
                  ),
                  Text(
                    value,
                    style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: AppTheme.onSurface,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _AdminMenuButton extends StatelessWidget {
  const _AdminMenuButton({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.onClick,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final VoidCallback onClick;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: AppTheme.surfaceLow,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: onClick,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppTheme.primary.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.admin_panel_settings, color: AppTheme.primary),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                        color: AppTheme.onSurface,
                      ),
                    ),
                    Text(
                      subtitle,
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppTheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              const Icon(
                Icons.chevron_right_rounded,
                color: AppTheme.onSurfaceVariant,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
