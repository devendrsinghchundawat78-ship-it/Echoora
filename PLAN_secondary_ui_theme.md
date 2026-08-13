# Plan — Secondary UI Theme ("Premium Music UI") in New Features

## Goal
Add a **second complete UI style (Theme 2 — "Premium Music UI")** that coexists with the
current UI (**Theme 1 — "Original UI"**) and is switchable from Settings → **New Features**
(beta section). The original UI must stay byte-for-byte functional.

## Architecture (extensible, centralized)
New package `ui/uitheme/` + `ui/premium/` + `ui/theme/PremiumTheme.kt`:

```
ui/uitheme/            UiThemeMode.kt        enum ORIGINAL / PREMIUM
                       UiThemeContext.kt     LocalUiThemeMode + UiThemeProvider + UiThemeSelector
ui/theme/              PremiumTheme.kt       PremiumColors + PremiumColorScheme + PremiumTypography + PremiumTheme()
ui/premium/            PremiumHomeScreen.kt  Theme 2 home (greeting, categories, trending, now playing)
                       PremiumBottomNav.kt   Theme 2 floating bottom nav
```

- `UiThemeProvider` reads `AppUiThemeKey` from the existing DataStore (persists across restarts).
- `UiThemeSelector(original={...}, premium={...})` is the single switching point — no scattered `if(premium)` checks.
- Adding Theme 3/4 = new enum entry + new presentation files. No app-logic rewrite.

## Data / logic reuse (no duplication)
Theme 2 reuses the exact same:
- `PlayerConnection` (mediaMetadata, isPlaying, shuffle/repeat, togglePlayPause/seekToNext/seekToPrevious/seekTo/toggleLike)
- `HomeViewModel` (accountName, accountImageUrl, homePage → trending SongItems, quickPicks)
- Navigation routes (`Screens.*`, `navController.navigate(...)`)

## Files
| File | Change |
|---|---|
| `constants/PreferenceKeys.kt` | + `AppUiThemeKey` (stringPreferencesKey) |
| `res/values/echo_strings.xml` | + App UI selector strings |
| `ui/uitheme/UiThemeMode.kt` | NEW — enum |
| `ui/uitheme/UiThemeContext.kt` | NEW — provider + selector |
| `ui/theme/PremiumTheme.kt` | NEW — premium palette/typography |
| `ui/premium/PremiumHomeScreen.kt` | NEW — Theme 2 home |
| `ui/premium/PremiumBottomNav.kt` | NEW — Theme 2 bottom nav |
| `ui/screens/settings/NewFeaturesSettings.kt` | + "App UI" selector (segmented control) |
| `ui/screens/NavigationBuilder.kt` | home route → UiThemeSelector(premium home vs original) |
| `MainActivity.kt` | `AppThemeSwitch` + `UiThemeProvider` at theme root; premium bottom nav switch; hide top bar on premium home; force dark system bars in premium |

## Global theme layer (whole-app coverage)
`AppThemeSwitch` picks `PremiumTheme` (fixed premium dark palette + typography) vs the original
`echomusicTheme` for the ENTIRE app. Because every existing screen already uses
`MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*` tokens, selecting Premium instantly
restyles Search, Library, Player sheet, Mini player, Settings, and all dialogs — no per-screen
rewrites. The custom premium Home + bottom nav remain the flagship "redesign" components on top
of that global look.

## Known first-iteration scope (documented, non-breaking)
- Bottom-nav switch targets the floating nav bar (`useFloatingNavBar`) path; the legacy toolbar
  path remains Theme 1 (still gets the global premium palette).
- Per-screen *layout* redesigns (custom premium Search/Library layouts) are follow-ups; the
  global palette/typography swap already covers their look-and-feel.
