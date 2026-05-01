# Rebranding Plan: Stitch/Vault Lumina/WealthMetric to **Spendrixa**

This plan outlines the steps to rebrand the entire project (Web, Native Android, and Flutter) to the new name "Spendrixa" and integrate the new logo (`screen.png`).

## Context
The project currently uses multiple names: "WealthMetric" for the website, "Vault Lumina" for the apps, and "Stitch" in folder and configuration names. The user wants a unified brand name, "Spendrixa", and to use `screen.png` as the logo.

## Proposed Changes

### 1. Web Rebranding
- **File**: `stitch_android_expense_tracker/web/index.html`
  - Replace "WealthMetric" with "Spendrixa".
  - Add `screen.png` as a logo in the navigation bar.
  - Update footer copyright and text.
- **File**: `stitch_android_expense_tracker/web/dashboard.html`
  - Replace "WealthMetric" with "Spendrixa".
- **File**: `stitch_android_expense_tracker/modern_dashboard/code.html`
  - Replace "Vault Lumina" and "WealthMetric" with "Spendrixa".

### 2. Android Native App Rebranding
- **File**: `stitch_android_expense_tracker/android/app/src/main/res/values/strings.xml`
  - Change `app_name` to "Spendrixa".
- **File**: `stitch_android_expense_tracker/android/settings.gradle.kts`
  - Change `rootProject.name` to "Spendrixa".
- **File**: `stitch_android_expense_tracker/android/app/src/main/res/values/themes.xml`
  - Rename `Theme.VaultLumina` to `Theme.Spendrixa`.
- **File**: `stitch_android_expense_tracker/android/app/src/main/AndroidManifest.xml`
  - Update `android:theme` references.
- **File**: `stitch_android_expense_tracker/android/app/src/main/java/com/vault/lumina/MainActivity.kt`
  - Rename `VaultLuminaApp` composable to `SpendrixaApp`.
- **File**: `stitch_android_expense_tracker/android/app/src/main/java/com/vault/lumina/ui/theme/Theme.kt`
  - Rename `VaultLuminaTheme` to `SpendrixaTheme`.
- **File**: `stitch_android_expense_tracker/android/app/build.gradle.kts`
  - (Optional but recommended) Update `namespace` and `applicationId` if requested, but for now we'll stick to UI-visible changes unless deeper refactoring is needed.

### 3. Flutter App Rebranding
- **File**: `stitch_android_expense_tracker/flutter_app/lib/src/app.dart`
  - Rename `VaultLuminaApp` class to `SpendrixaApp`.
  - Update `MaterialApp` title to "Spendrixa".
- **File**: `stitch_android_expense_tracker/flutter_app/lib/main.dart`
  - Update `runApp(const VaultLuminaApp())` to `runApp(const SpendrixaApp())`.
- **File**: `stitch_android_expense_tracker/flutter_app/android/app/src/main/res/values/strings.xml`
  - Change `app_name` to "Spendrixa".
- **File**: `stitch_android_expense_tracker/flutter_app/lib/src/screens/settings_page.dart`
  - Replace "WealthMetric" and "Vault Lumina" mentions with "Spendrixa".

### 4. General Project Updates
- **File**: `stitch_android_expense_tracker/README.md`
  - Update all branding to "Spendrixa".
- **File**: `.claude/settings.local.json`
  - Update copy commands and any internal references.

### 5. Logo Integration
- Copy `screen.png` from root to `stitch_android_expense_tracker/web/screen.png`.
- Update `index.html` to display the logo.

## Verification Plan
1. **Web**: Open `web/index.html` and verify the title, navigation, logo, and footer.
2. **Android**: Verify `strings.xml` and `AndroidManifest.xml` have the new name and theme.
3. **Flutter**: Run `grep` to ensure no "Vault Lumina" or "WealthMetric" remains in `flutter_app/lib/`.
4. **General**: Check `README.md` for consistency.
