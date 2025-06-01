package com.noobdev.numlexambuddy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = TextPrimaryDark,
    secondary = SecondaryDark,
    onSecondary = TextPrimaryDark,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = AccentTeal,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    error = Error
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = TextPrimaryDark,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = TextPrimaryDark,
    secondary = SecondaryLight,
    onSecondary = TextPrimaryDark,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = AccentTeal,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    error = Error
)

@Composable
fun NumlExamBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled dynamic colors for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Theme utility extensions for easier access to custom colors
object NumlTheme {
    val accentAmber: Color @Composable get() = AccentAmber
    val accentBlue: Color @Composable get() = AccentBlue
    val accentTeal: Color @Composable get() = AccentTeal
    val success: Color @Composable get() = Success
    val warning: Color @Composable get() = Warning
    val info: Color @Composable get() = Info
}

// Extension properties for MaterialTheme to access custom colors
val MaterialTheme.customColors: NumlCustomColors
    @Composable
    get() = NumlCustomColors(
        accentAmber = AccentAmber,
        accentBlue = AccentBlue,
        accentTeal = AccentTeal,
        success = Success,
        warning = Warning,
        info = Info
    )

// Data class to hold custom colors not in Material 3 spec
data class NumlCustomColors(
    val accentAmber: Color,
    val accentBlue: Color,
    val accentTeal: Color,
    val success: Color,
    val warning: Color,
    val info: Color
)