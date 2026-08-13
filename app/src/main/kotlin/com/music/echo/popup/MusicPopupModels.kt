package iad1tya.echo.music.popup

/**
 * Configuration models for the floating Music Popup feature.
 *
 * These are presentation-layer options only — the popup always controls the
 * SAME player instance used by the main app (single source of truth).
 */

/** Collapsed bubble size. */
enum class MusicPopupSize(val storageValue: String, val sizeDp: Float) {
    SMALL("small", 48f),
    MEDIUM("medium", 60f),
    LARGE("large", 72f);

    companion object {
        fun fromStorage(value: String?): MusicPopupSize =
            entries.firstOrNull { it.storageValue == value } ?: MEDIUM
    }
}

/** Preferred screen edge for the popup. */
enum class MusicPopupSide(val storageValue: String) {
    LEFT("left"),
    RIGHT("right");

    companion object {
        fun fromStorage(value: String?): MusicPopupSide =
            entries.firstOrNull { it.storageValue == value } ?: RIGHT
    }
}
