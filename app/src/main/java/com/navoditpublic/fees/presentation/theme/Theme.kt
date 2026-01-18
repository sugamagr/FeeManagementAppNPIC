package com.navoditpublic.fees.presentation.theme

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
import androidx.core.view.WindowInsetsControllerCompat

private val LightColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = TextOnSaffron,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = SaffronDark,
    
    secondary = Brown,
    onSecondary = Color.White,
    secondaryContainer = BrownLight,
    onSecondaryContainer = Color.White,
    
    tertiary = SaffronLight,
    onTertiary = BrownDark,
    
    background = Cream,
    onBackground = TextPrimary,
    
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SaffronContainer,
    onSurfaceVariant = TextSecondary,
    
    error = ErrorRed,
    onError = Color.White,
    errorContainer = DueChipBackground,
    onErrorContainer = ErrorRed,
    
    outline = DividerLight,
    outlineVariant = Color(0xFFCAC4D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronLight,
    onPrimary = BrownDark,
    primaryContainer = SaffronDark,
    onPrimaryContainer = SaffronContainer,
    
    secondary = BrownLight,
    onSecondary = Color.White,
    secondaryContainer = Brown,
    onSecondaryContainer = Color.White,
    
    tertiary = Saffron,
    onTertiary = Color.White,
    
    background = CreamDark,
    onBackground = TextPrimaryDark,
    
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorRedLight,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    outline = DividerDark,
    outlineVariant = Color(0xFF49454F)
)

@Composable
fun FeesAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to maintain saffron theme
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
            val insetsController = WindowInsetsControllerCompat(window, view)
            
            // Set status bar color using the modern approach
            @Suppress("DEPRECATION")
            window.statusBarColor = if (darkTheme) SaffronDark.toArgb() else Saffron.toArgb()
            
            // Set status bar icons to light (white) on our saffron background
            insetsController.isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

