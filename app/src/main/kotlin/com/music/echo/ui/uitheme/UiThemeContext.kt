package iad1tya.echo.music.ui.uitheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import iad1tya.echo.music.constants.AppUiThemeKey
import iad1tya.echo.music.utils.rememberPreference

/**
 * Centralized UI-mode composition local.
 *
 * Screens and components read [LocalUiThemeMode] (or, better, use
 * [UiThemeSelector]) instead of scattering `if (premium)` checks across the app.
 */
val LocalUiThemeMode = staticCompositionLocalOf { UiThemeMode.ORIGINAL }

/**
 * Provides the currently selected [UiThemeMode] to the composition.
 *
 * The selection is read and persisted through the existing DataStore preference
 * system, so the chosen UI remains active after the app restarts.
 */
@Composable
fun UiThemeProvider(content: @Composable () -> Unit) {
    val (stored) = rememberPreference(AppUiThemeKey, UiThemeMode.ORIGINAL.storageValue)
    val mode = remember(stored) { UiThemeMode.fromStorage(stored) }
    CompositionLocalProvider(LocalUiThemeMode provides mode) {
        content()
    }
}

/**
 * Renders exactly one of two presentation layers based on the active UI mode.
 *
 * This is the single, maintainable switching point used at the few places where
 * Theme 1 and Theme 2 differ (e.g. the home screen and the bottom navigation).
 */
@Composable
fun UiThemeSelector(
    original: @Composable () -> Unit,
    premium: @Composable () -> Unit,
) {
    when (LocalUiThemeMode.current) {
        UiThemeMode.ORIGINAL -> original()
        UiThemeMode.PREMIUM -> premium()
    }
}
