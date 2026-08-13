package iad1tya.echo.music.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Selectable launcher app icons (the "App Icon" picker in New Features).
 *
 * Each entry maps to an activity-alias declared in the manifest. The original
 * icon (Classic) uses the existing [MainActivityAlias] so the default behavior
 * is untouched.
 */
enum class AppIcon(val storageValue: String) {
    CLASSIC("classic"),
    MONO("mono"),
    CRIMSON("crimson"),
    NOIR("noir");

    companion object {
        fun fromStorage(value: String?): AppIcon =
            entries.firstOrNull { it.storageValue == value } ?: CLASSIC
    }
}

object IconUtils {
    /**
     * Original icon toggle (used by the legacy-icon setting in Appearance).
     * Also disables the newer picker aliases so only one launcher icon shows.
     */
    fun setIcon(context: Context, isDynamic: Boolean, isLegacy: Boolean) {
        val pm = context.packageManager
        val dynamic = ComponentName(context, "iad1tya.echo.music.MainActivityAlias")
        val static = ComponentName(context, "iad1tya.echo.music.MainActivityStatic")
        val legacy = ComponentName(context, "iad1tya.echo.music.MainActivityLegacy")

        pm.setComponentEnabledSetting(
            dynamic,
            if (isDynamic && !isLegacy) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            static,
            if (!isDynamic && !isLegacy) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            legacy,
            if (isLegacy) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        disablePickerIcons(pm, context)
    }

    /**
     * Applies the selected icon from the "App Icon" picker. Exactly one launcher
     * alias is enabled; every other launcher alias is disabled to avoid
     * duplicate icons in the app drawer.
     */
    fun applyAppIcon(context: Context, icon: AppIcon) {
        val pm = context.packageManager
        val dynamic = ComponentName(context, "iad1tya.echo.music.MainActivityAlias")
        val static = ComponentName(context, "iad1tya.echo.music.MainActivityStatic")
        val legacy = ComponentName(context, "iad1tya.echo.music.MainActivityLegacy")
        val mono = ComponentName(context, "iad1tya.echo.music.MainActivityMono")
        val crimson = ComponentName(context, "iad1tya.echo.music.MainActivityCrimson")
        val noir = ComponentName(context, "iad1tya.echo.music.MainActivityNoir")

        fun enable(cn: ComponentName, on: Boolean) {
            pm.setComponentEnabledSetting(
                cn,
                if (on) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        enable(dynamic, icon == AppIcon.CLASSIC)
        enable(static, false)
        enable(legacy, false)
        enable(mono, icon == AppIcon.MONO)
        enable(crimson, icon == AppIcon.CRIMSON)
        enable(noir, icon == AppIcon.NOIR)
    }

    private fun disablePickerIcons(pm: PackageManager, context: Context) {
        val mono = ComponentName(context, "iad1tya.echo.music.MainActivityMono")
        val crimson = ComponentName(context, "iad1tya.echo.music.MainActivityCrimson")
        val noir = ComponentName(context, "iad1tya.echo.music.MainActivityNoir")
        pm.setComponentEnabledSetting(mono, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(crimson, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(noir, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
    }
}
