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

- Auth: Email/password + Unique Username
- Database: Firestore
- Collections:
  - `users/{uid}`: Profile data, role, currency, budget.
  - `users/{uid}/transactions`: Subcollection for user expenses/income.
  - `usernames/{username}`: Mapping to UID for uniqueness enforcement.

### Recommended Firestore rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper function to check if the user is an admin
    function isAdmin() {
      return request.auth != null && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Allow users to access their own data, OR admins to access everything
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && (request.auth.uid == userId || isAdmin());
    }
    
    // Username uniqueness handling
    match /usernames/{username} {
      allow read: if true;
      allow create: if request.auth != null;
      allow delete: if isAdmin();
    }
  }
}
```

## Admin Console
Spendrixa includes a powerful administrative dashboard for developers to manage the entire platform.

### Accessing the Console
1. Set the `role` field to `"admin"` in your document within the `users` collection.
2. **Web**: Log in and click "Admin Console" in the sidebar.
3. **Android**: A new "Admin" tab will appear in the bottom navigation.

### Admin Features
- **Global Overview**: Real-time stats on total users, transactions, and revenue.
- **User Management**: Search, edit, or delete any platform user.
- **Transaction Logs**: Audit every transaction recorded on the platform, formatted in the user's local currency.
- **User Detail View**: Deep-dive into specific user analytics and ledgers.


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
