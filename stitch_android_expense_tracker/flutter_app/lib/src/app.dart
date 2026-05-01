import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';

import 'screens/home_shell.dart';
import 'screens/login_screen.dart';
import 'screens/verify_email_screen.dart';
import 'services/transaction_repository.dart';
import 'theme/app_theme.dart';

class SpendrixaApp extends StatelessWidget {
  const SpendrixaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Spendrixa',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme(),
      home: const AuthGate(),
    );
  }
}

class AuthGate extends StatefulWidget {
  const AuthGate({super.key});

  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  final TransactionRepository _repository = TransactionRepository();

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<User?>(
      stream: FirebaseAuth.instance.authStateChanges(),
      builder: (BuildContext context, AsyncSnapshot<User?> authSnapshot) {
        if (authSnapshot.connectionState == ConnectionState.waiting) {
          return const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          );
        }

        final User? user = authSnapshot.data;
        if (user == null) {
          return LoginScreen(repository: _repository);
        }

        if (!user.emailVerified) {
          return const VerifyEmailScreen();
        }

        return FutureBuilder<void>(
          future: _repository.ensureUserProfile(user),
          builder: (BuildContext context, AsyncSnapshot<void> profileSnapshot) {
            if (profileSnapshot.connectionState == ConnectionState.waiting) {
              return const Scaffold(
                body: Center(child: CircularProgressIndicator()),
              );
            }

            return HomeShell(
              user: user,
              repository: _repository,
            );
          },
        );
      },
    );
  }
}
