package iad1tya.echo.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Premium (Theme 2) color palette & typography.
 *
 * Dark, minimal, cinematic, with a vibrant red/pink accent — inspired by the
 * reference image. Kept fully separate from the original theme so Theme 1 is
 * never touched.
 */
object PremiumColors {
    val Background = Color(0xFF0A0A0C)
    val Surface = Color(0xFF141417)
    val SurfaceHigh = Color(0xFF1C1C21)
    val SurfaceVariant = Color(0xFF232329)
    val OnBackground = Color(0xFFF6F6F8)
    val OnSurface = Color(0xFFF6F6F8)
    val OnSurfaceVariant = Color(0xFF9B9BA4)
    val Primary = Color(0xFFFF3B5C)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFF3A121C)
    val OnPrimaryContainer = Color(0xFFFFD9DE)
    val Secondary = Color(0xFF2B2B31)
    val OnSecondary = Color(0xFFF6F6F8)
    val Outline = Color(0xFF2E2E35)
    val OutlineVariant = Color(0xFF232329)

    // "Now Playing" hero card gradient (vibrant red → deep rose)
    val PlayerGradientStart = Color(0xFFFF3B5C)
    val PlayerGradientEnd = Color(0xFFB3123A)
}

val PremiumColorScheme = darkColorScheme(
    primary = PremiumColors.Primary,
    onPrimary = PremiumColors.OnPrimary,
    primaryContainer = PremiumColors.PrimaryContainer,
    onPrimaryContainer = PremiumColors.OnPrimaryContainer,
    secondary = PremiumColors.Secondary,
    onSecondary = PremiumColors.OnSecondary,
    background = PremiumColors.Background,
    onBackground = PremiumColors.OnBackground,
    surface = PremiumColors.Surface,
    onSurface = PremiumColors.OnSurface,
    surfaceVariant = PremiumColors.SurfaceVariant,
    onSurfaceVariant = PremiumColors.OnSurfaceVariant,
    outline = PremiumColors.Outline,
    outlineVariant = PremiumColors.OutlineVariant,
)

val PremiumTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, letterSpacing = 0.1.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, letterSpacing = 0.1.sp),
    bodySmall = TextStyle(fontSize = 12.sp, letterSpacing = 0.2.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.4.sp),
)

/**
 * Applies the premium color scheme + typography for Theme 2 components.
 *
 * Wrap Theme 2 presentation layers in this so Material3 primitives
 * (Sliders, Cards, etc.) pick up the premium palette.
 */
@Composable
fun PremiumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PremiumColorScheme,
        typography = PremiumTypography,
        content = content,
    )
}
