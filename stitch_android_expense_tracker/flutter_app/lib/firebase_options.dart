import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart' show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
      case TargetPlatform.macOS:
      case TargetPlatform.windows:
      case TargetPlatform.linux:
      case TargetPlatform.fuchsia:
        throw UnsupportedError(
          'DefaultFirebaseOptions are only configured for Android and web in this project.',
        );
    }
  }

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'AIzaSyBTgZYkxPMwRbASCAa-ThDSvL3CDUadKzQ',
    appId: '1:45467822444:web:a70d772424996e833e6b49',
    messagingSenderId: '45467822444',
    projectId: 'expense-tracker-1b22d',
    authDomain: 'expense-tracker-1b22d.firebaseapp.com',
    storageBucket: 'expense-tracker-1b22d.firebasestorage.app',
    measurementId: 'G-7RMFCJ198W',
  );

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyCGTVu6BuvLl2C_ztGgcM3lc0I5_yIAFUI',
    appId: '1:45467822444:android:b52140c80f5002563e6b49',
    messagingSenderId: '45467822444',
    projectId: 'expense-tracker-1b22d',
    storageBucket: 'expense-tracker-1b22d.firebasestorage.app',
  );
}
