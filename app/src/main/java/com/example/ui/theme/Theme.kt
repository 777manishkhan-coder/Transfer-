package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val BolIndiaDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = SurfaceCardSub,
    onPrimaryContainer = PureWhite,
    secondary = TextSilver,
    onSecondary = PureBlack,
    secondaryContainer = SurfaceCardHover,
    onSecondaryContainer = PureWhite,
    tertiary = TextSlate,
    onTertiary = PureBlack,
    background = SurfacePitch,
    onBackground = PureWhite,
    surface = SurfaceCard,
    onSurface = PureWhite,
    surfaceVariant = SurfaceCardSub,
    onSurfaceVariant = TextSlate,
    outline = BorderMedium,
    outlineVariant = BorderSubtle,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorContainerDark,
    onErrorContainer = PureWhite
)

private val BolIndiaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun BolIndiaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BolIndiaDarkColorScheme,
        typography = BolIndiaTypography,
        shapes = BolIndiaShapes,
        content = content
    )
}
