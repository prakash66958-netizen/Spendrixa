# Spendrixa Flutter

This folder contains the Flutter migration of the original Spendrixa app.

## What Is Included

- Firebase Auth sign in / sign up
- Firestore-backed dashboard, history, and insights
- Add transaction flow
- Shared dark theme inspired by the original Android app
- Android and web Flutter targets

## Run It

From this folder:

```bash
C:\src\flutter\bin\flutter.bat pub get
C:\src\flutter\bin\flutter.bat run -d chrome
```

To build Android:

```bash
C:\src\flutter\bin\flutter.bat build apk
```

The generated APK is written to:

`build/app/outputs/flutter-apk/app-release.apk`

## Notes

- `android/app/google-services.json` is copied from the original Android project so the Flutter Android app points at the same Firebase project.
- Web Firebase configuration is provided in `lib/firebase_options.dart`.
- No Android emulator is configured on this machine yet. If you create one in Android Studio, you can run:

```bash
C:\src\flutter\bin\flutter.bat run
```
