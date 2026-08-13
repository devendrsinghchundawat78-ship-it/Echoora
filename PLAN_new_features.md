# Plan — "New Features" Settings Section

## Goal
Add a new settings section named **"New Features"** placed **below Account** in the main
Settings screen. Inside it, all new/experimental features are listed as **on/off toggles**
so the user can enable/disable each feature from one place.

---

## How Echoora settings are built (context found in code)

| Concern | File | What I found |
|---|---|---|
| Main settings list | `app/.../ui/screens/settings/SettingsScreen.kt` | Flat `buildList` of `Material3SettingsItem`; Account item navigates to `settings/account` |
| Example toggle screen | `app/.../ui/screens/settings/AccountSettingsScreen.kt` | `Scaffold` + `TopAppBar` + scrollable `Column` + `Material3SettingsGroup` with `Switch` items |
| Preference storage | `app/.../constants/PreferenceKeys.kt` | DataStore `booleanPreferencesKey(...)` values, read via `rememberPreference(key, default)` |
| Navigation routes | `app/.../ui/screens/NavigationBuilder.kt` | `composable("settings/xxx") { ... }` |
| UI strings | `app/.../res/values/echo_strings.xml` | Echo-specific strings go here (NOT upstream `strings.xml`) |
| Search index | `app/.../ui/screens/settings/SearchableSettings.kt` | `Triple(title, parent, route)` list for the settings search bar |

Package convention note: files live under `com/music/echo/` but declare package
`iad1tya.echo.music.ui.screens.settings` — new files must follow the same pattern.

---

## Steps

1. **Add strings** → `echo_strings.xml`
   - `new_features` = "New Features"
   - one `title` (+ optional `desc`) string per feature toggle

2. **Add preference keys** → `PreferenceKeys.kt`
   - Reuse existing keys where they already exist (see mapping below)
   - Add new `booleanPreferencesKey` for features that have none yet

3. **Create screen** → new file `NewFeaturesSettings.kt` (mirror `AccountSettingsScreen.kt`):
   - `@Composable fun NewFeaturesSettings(navController, scrollBehavior, highlightKey = null)`
   - `Scaffold` + `TopAppBar` ("New Features", back button) + scrollable `Column`
   - `Material3SettingsGroup(title = "New Features", items = ...)` — one `Material3SettingsItem`
     per feature with a `Switch` (thumb `check`/`close` icons, same as Account screen)

4. **Register route** → `NavigationBuilder.kt`:
   - `composable("settings/new_features") { NewFeaturesSettings(navController, scrollBehavior) }`

5. **Add entry below Account** → `SettingsScreen.kt`:
   - `val newFeaturesText = stringResource(R.string.new_features)`
   - insert `Material3SettingsItem` immediately after the Account item,
     `onClick = { navController.navigate("settings/new_features") }`

6. **Add to settings search** → `SearchableSettings.kt`:
   - `Triple(stringResource(R.string.new_features), "Settings", "settings/new_features")`
   - + one `Triple` per feature toggle

7. **(Functional wiring — larger scope)** Make each toggle actually gate the feature
   (read the new key in the feature's own code). Can be done incrementally per feature.

---

## Proposed feature list (from README "What's New")

| Feature | Existing pref key | Action needed |
|---|---|---|
| Lossless Music Hub | `LOSSLESS_ENABLED` (const, not toggleable) | new key + wire |
| Data Saver Mode (Beta) | `DataSaverEnabledKey` ✅ | reuse |
| Listen Together | `EnableListenTogetherKey` ✅ | reuse |
| Import from Spotify | Spotify connect prefs (not a toggle) | new key + wire |
| Podcast Support | none | new key + wire |
| Local Media Support | none | new key + wire |
| Dynamic Island Support | none | new key + wire |
| Echo Find (recognition) | none | new key + wire |
| Settings Search Index | none | new key + wire |

---

## Open decisions (need your input)
1. **Which features** should appear in the list? (proposal above, or your own list)
2. **Phase 1 vs full**: toggles only (UI + saved pref) first, or also wire them to
   actually enable/disable each feature now?
