package com.julio.carrerasatletismo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EsquemaClaro = lightColorScheme(
    primary = Tartan40,
    onPrimary = Fondo99,
    primaryContainer = Tartan90,
    onPrimaryContainer = Tartan10,
    secondary = Marino40,
    onSecondary = Fondo99,
    secondaryContainer = Marino90,
    onSecondaryContainer = Marino10,
    tertiary = Velocidad40,
    tertiaryContainer = Velocidad90,
    background = Fondo99,
    onBackground = Texto10,
    surface = Fondo99,
    onSurface = Texto10,
    surfaceVariant = Variante90,
    onSurfaceVariant = Variante30
)

private val EsquemaOscuro = darkColorScheme(
    primary = Tartan80,
    onPrimary = Tartan20,
    primaryContainer = Tartan30,
    onPrimaryContainer = Tartan90,
    secondary = Marino80,
    onSecondary = Marino30,
    secondaryContainer = Marino40,
    onSecondaryContainer = Marino90,
    tertiary = Velocidad80,
    background = Fondo10,
    onBackground = Texto90,
    surface = Fondo10,
    onSurface = Texto90,
    surfaceVariant = Variante30,
    onSurfaceVariant = Variante80
)

@Composable
fun CarrerasAtletismoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> EsquemaOscuro
        else -> EsquemaClaro
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
