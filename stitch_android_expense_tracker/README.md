# Spendrixa - Expense Tracker

Spendrixa now includes:

- The original Android app in `android/`
- The original static web app in `web/`
- A new Flutter migration in `flutter_app/`

## Project Layout

```text
stitch_android_expense_tracker/
|-- android/       Original Jetpack Compose app
|-- flutter_app/   New Flutter app (Android + Web)
`-- web/           Original static Firebase web dashboard
```

## Firebase

All app variants use the same Firebase project:

- Auth: Email/password
- Database: Firestore
- User data path: `users/{uid}`
- Transactions path: `users/{uid}/transactions`

Recommended Firestore rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Run The Flutter App

The Flutter migration lives in `flutter_app/`.

Run it in Chrome:

```bash
cd flutter_app
C:\src\flutter\bin\flutter.bat pub get
C:\src\flutter\bin\flutter.bat run -d chrome
```

Build Android APK:

```bash
cd flutter_app
C:\src\flutter\bin\flutter.bat build apk
```

APK output:

```text
flutter_app/build/app/outputs/flutter-apk/app-release.apk
```

## Run The Original Android App

```bash
cd android
./gradlew assembleDebug
```

## Notes

- `flutter_app/android/app/google-services.json` is copied from the original Android app.
- The Flutter app is wired to the same Firebase Auth + Firestore backend as the original implementation.
- No Android emulator is configured on this machine yet, so Chrome is the immediate runnable Flutter target.
