import 'package:flutter/material.dart';

class AppTheme {
  static const Color background = Color(0xFF00161C);
  static const Color surface = Color(0xFF08232A);
  static const Color surfaceLow = Color(0xFF041F26);
  static const Color surfaceHigh = Color(0xFF142D35);
  static const Color surfaceHighest = Color(0xFF1F3840);
  static const Color primary = Color(0xFFA6CCDD);
  static const Color primaryContainer = Color(0xFF002D3A);
  static const Color tertiary = Color(0xFFB0D500);
  static const Color tertiaryContainer = Color(0xFF232C00);
  static const Color onSurface = Color(0xFFCCE7F1);
  static const Color onSurfaceVariant = Color(0xFFC1C7CB);
  static const Color outline = Color(0xFF8B9295);

  static ThemeData darkTheme() {
    const ColorScheme scheme = ColorScheme(
      brightness: Brightness.dark,
      primary: primary,
      onPrimary: Color(0xFF0B3542),
      secondary: Color(0xFFE6FEFF),
      onSecondary: Color(0xFF003739),
      error: Color(0xFFFFB4AB),
      onError: Color(0xFF690005),
      surface: surface,
      onSurface: onSurface,
      primaryContainer: primaryContainer,
      onPrimaryContainer: Color(0xFFC2E8F9),
      secondaryContainer: Color(0xFF00F4FE),
      onSecondaryContainer: Color(0xFF006C71),
      tertiary: tertiary,
      onTertiary: Color(0xFF2A3400),
      tertiaryContainer: tertiaryContainer,
      onTertiaryContainer: Color(0xFFCaf300),
      surfaceContainerHighest: surfaceHighest,
      outline: outline,
    );

    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: scheme,
      scaffoldBackgroundColor: background,
      appBarTheme: const AppBarTheme(
        backgroundColor: background,
        foregroundColor: onSurface,
        elevation: 0,
        centerTitle: false,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: surfaceLow,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: const BorderSide(color: primary, width: 1.2),
        ),
        labelStyle: const TextStyle(color: onSurfaceVariant),
      ),
      cardTheme: CardThemeData(
        color: surface,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: primary,
        foregroundColor: Color(0xFF0B3542),
      ),
      snackBarTheme: const SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
      ),
      textTheme: const TextTheme(
        headlineMedium: TextStyle(
          fontSize: 32,
          fontWeight: FontWeight.w800,
          color: onSurface,
        ),
        titleLarge: TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.w700,
          color: onSurface,
        ),
        titleMedium: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w600,
          color: onSurface,
        ),
        bodyLarge: TextStyle(fontSize: 16, color: onSurface),
        bodyMedium: TextStyle(fontSize: 14, color: onSurfaceVariant),
      ),
    );
  }
}
