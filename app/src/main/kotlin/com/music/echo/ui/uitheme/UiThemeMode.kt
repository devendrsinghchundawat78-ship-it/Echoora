package iad1tya.echo.music.ui.uitheme

/**
 * UI theme modes for Echoora.
 *
 * [ORIGINAL] = Theme 1 — the current/original UI. Never modified.
 * [PREMIUM]  = Theme 2 — the new premium music UI.
 *
 * To add a Theme 3 / Theme 4, add a new enum entry here and provide a new
 * presentation layer (see [UiThemeSelector]) — no application logic rewrite.
 */
enum class UiThemeMode(val storageValue: String) {
    ORIGINAL("original"),
    PREMIUM("premium");

    companion object {
        fun fromStorage(value: String?): UiThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: ORIGINAL
    }
}
