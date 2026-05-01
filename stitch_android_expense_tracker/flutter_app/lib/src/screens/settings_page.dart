import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.fromLTRB(24, 8, 24, 120),
      children: <Widget>[
        Text(
          'Manage your app settings and preferences.',
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
                    const Icon(Icons.person_outline_rounded, color: AppTheme.tertiary),
                    const SizedBox(width: 12),
                    Text('Account', style: theme.textTheme.titleMedium),
                  ],
                ),
                const SizedBox(height: 16),
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Email'),
                  subtitle: Text(FirebaseAuth.instance.currentUser?.email ?? 'Not logged in'),
                  trailing: const Icon(Icons.chevron_right_rounded),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
