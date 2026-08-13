package iad1tya.echo.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.R
import iad1tya.echo.music.constants.AppIconKey
import iad1tya.echo.music.constants.AppUiThemeKey
import iad1tya.echo.music.constants.DataSaverEnabledKey
import iad1tya.echo.music.constants.EnableListenTogetherKey
import iad1tya.echo.music.constants.LosslessEnabledKey
import iad1tya.echo.music.constants.MusicPopupAutoHideKey
import iad1tya.echo.music.constants.MusicPopupEnabledKey
import iad1tya.echo.music.constants.MusicPopupSideKey
import iad1tya.echo.music.constants.MusicPopupSizeKey
import iad1tya.echo.music.popup.MusicPopupSide
import iad1tya.echo.music.popup.MusicPopupSize
import iad1tya.echo.music.ui.uitheme.UiThemeMode
import iad1tya.echo.music.ui.component.IconButton
import iad1tya.echo.music.ui.component.Material3SettingsGroup
import iad1tya.echo.music.ui.component.Material3SettingsItem
import iad1tya.echo.music.ui.utils.backToMain
import iad1tya.echo.music.utils.AppIcon
import iad1tya.echo.music.utils.IconUtils
import iad1tya.echo.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFeaturesSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    highlightKey: String? = null,
) {
    val scrollState = rememberScrollState()

    val (dataSaverEnabled, onDataSaverEnabledChange) = rememberPreference(DataSaverEnabledKey, false)
    val (listenTogetherEnabled, onListenTogetherEnabledChange) = rememberPreference(EnableListenTogetherKey, false)
    val (losslessEnabled, onLosslessEnabledChange) = rememberPreference(LosslessEnabledKey, false)
    val (uiTheme, onUiThemeChange) = rememberPreference(AppUiThemeKey, UiThemeMode.ORIGINAL.storageValue)
    val (appIcon, onAppIconChange) = rememberPreference(AppIconKey, AppIcon.CLASSIC.storageValue)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_features)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
                )
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.new_features_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.app_ui_theme),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            AppUiThemeSelector(current = uiTheme, onSelect = onUiThemeChange)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_icon),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            AppIconSelector(
                current = AppIcon.fromStorage(appIcon),
                onSelect = { selected ->
                    onAppIconChange(selected.storageValue)
                    IconUtils.applyAppIcon(context, selected)
                },
            )
            Spacer(modifier = Modifier.height(24.dp))

            MusicPopupSection(scrollState = scrollState)

            Spacer(modifier = Modifier.height(24.dp))

            Material3SettingsGroup(
                scrollState = scrollState,
                title = stringResource(R.string.new_features),
                items = listOf(
                    Material3SettingsItem(
                        isHighlighted = (highlightKey == stringResource(R.string.new_features_data_saver)),
                        icon = painterResource(R.drawable.speed),
                        title = { Text(stringResource(R.string.new_features_data_saver)) },
                        description = { Text(stringResource(R.string.new_features_data_saver_desc)) },
                        trailingContent = {
                            Switch(
                                checked = dataSaverEnabled,
                                onCheckedChange = onDataSaverEnabledChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (dataSaverEnabled) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            )
                        },
                        onClick = { onDataSaverEnabledChange(!dataSaverEnabled) }
                    ),
                    Material3SettingsItem(
                        isHighlighted = (highlightKey == stringResource(R.string.new_features_listen_together)),
                        icon = painterResource(R.drawable.group),
                        title = { Text(stringResource(R.string.new_features_listen_together)) },
                        description = { Text(stringResource(R.string.new_features_listen_together_desc)) },
                        trailingContent = {
                            Switch(
                                checked = listenTogetherEnabled,
                                onCheckedChange = onListenTogetherEnabledChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (listenTogetherEnabled) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            )
                        },
                        onClick = { onListenTogetherEnabledChange(!listenTogetherEnabled) }
                    ),
                    Material3SettingsItem(
                        isHighlighted = (highlightKey == stringResource(R.string.new_features_lossless)),
                        icon = painterResource(R.drawable.music_note),
                        title = { Text(stringResource(R.string.new_features_lossless)) },
                        description = { Text(stringResource(R.string.new_features_lossless_desc)) },
                        trailingContent = {
                            Switch(
                                checked = losslessEnabled,
                                onCheckedChange = onLosslessEnabledChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (losslessEnabled) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            )
                        },
                        onClick = { onLosslessEnabledChange(!losslessEnabled) }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(
                Modifier.windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
                )
            )
        }
    }
}

@Composable
private fun AppUiThemeSelector(
    current: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(4.dp),
    ) {
        SegmentedOption(
            label = stringResource(R.string.app_ui_theme_original),
            selected = current == UiThemeMode.ORIGINAL.storageValue,
            onClick = { onSelect(UiThemeMode.ORIGINAL.storageValue) },
        )
        SegmentedOption(
            label = stringResource(R.string.app_ui_theme_premium),
            selected = current == UiThemeMode.PREMIUM.storageValue,
            onClick = { onSelect(UiThemeMode.PREMIUM.storageValue) },
        )
    }
}

@Composable
private fun RowScope.SegmentedOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun MusicPopupSection(scrollState: ScrollState) {
    val context = LocalContext.current

    val (popupEnabled, onPopupEnabledChange) = rememberPreference(MusicPopupEnabledKey, false)
    val (popupSize, onPopupSizeChange) = rememberPreference(MusicPopupSizeKey, MusicPopupSize.MEDIUM.storageValue)
    val (popupSide, onPopupSideChange) = rememberPreference(MusicPopupSideKey, MusicPopupSide.RIGHT.storageValue)
    val (autoHide, onAutoHideChange) = rememberPreference(MusicPopupAutoHideKey, true)

    val hasOverlayPermission = Settings.canDrawOverlays(context)

    Text(
        text = stringResource(R.string.music_popup),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )

    Material3SettingsGroup(
        scrollState = scrollState,
        items = listOf(
            Material3SettingsItem(
                isHighlighted = false,
                icon = painterResource(R.drawable.music_note),
                title = { Text(stringResource(R.string.music_popup)) },
                description = { Text(stringResource(R.string.music_popup_desc)) },
                trailingContent = {
                    Switch(
                        checked = popupEnabled,
                        onCheckedChange = onPopupEnabledChange,
                        thumbContent = {
                            Icon(
                                painter = painterResource(
                                    id = if (popupEnabled) R.drawable.check else R.drawable.close
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    )
                },
                onClick = { onPopupEnabledChange(!popupEnabled) }
            ),
            Material3SettingsItem(
                isHighlighted = false,
                icon = painterResource(R.drawable.security),
                title = { Text(stringResource(R.string.music_popup_auto_hide)) },
                description = { Text(stringResource(R.string.music_popup_auto_hide_desc)) },
                trailingContent = {
                    Switch(
                        checked = autoHide,
                        onCheckedChange = onAutoHideChange,
                        thumbContent = {
                            Icon(
                                painter = painterResource(
                                    id = if (autoHide) R.drawable.check else R.drawable.close
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    )
                },
                onClick = { onAutoHideChange(!autoHide) }
            )
        )
    )

    if (popupEnabled && !hasOverlayPermission) {
        Spacer(modifier = Modifier.height(10.dp))
        Material3SettingsGroup(
            scrollState = scrollState,
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.security),
                    title = { Text(stringResource(R.string.music_popup_permission_title)) },
                    description = { Text(stringResource(R.string.music_popup_permission_desc)) },
                    onClick = {
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(fallback)
                        }
                    }
                )
            )
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = stringResource(R.string.music_popup_size),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(4.dp),
    ) {
        SegmentedOption(
            label = stringResource(R.string.music_popup_size_small),
            selected = popupSize == MusicPopupSize.SMALL.storageValue,
            onClick = { onPopupSizeChange(MusicPopupSize.SMALL.storageValue) },
        )
        SegmentedOption(
            label = stringResource(R.string.music_popup_size_medium),
            selected = popupSize == MusicPopupSize.MEDIUM.storageValue,
            onClick = { onPopupSizeChange(MusicPopupSize.MEDIUM.storageValue) },
        )
        SegmentedOption(
            label = stringResource(R.string.music_popup_size_large),
            selected = popupSize == MusicPopupSize.LARGE.storageValue,
            onClick = { onPopupSizeChange(MusicPopupSize.LARGE.storageValue) },
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = stringResource(R.string.music_popup_position),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(4.dp),
    ) {
        SegmentedOption(
            label = stringResource(R.string.music_popup_position_left),
            selected = popupSide == MusicPopupSide.LEFT.storageValue,
            onClick = { onPopupSideChange(MusicPopupSide.LEFT.storageValue) },
        )
        SegmentedOption(
            label = stringResource(R.string.music_popup_position_right),
            selected = popupSide == MusicPopupSide.RIGHT.storageValue,
            onClick = { onPopupSideChange(MusicPopupSide.RIGHT.storageValue) },
        )
    }
}

private data class AppIconOption(
    val icon: AppIcon,
    val labelRes: Int,
    val previewRes: Int,
)

@Composable
private fun AppIconSelector(
    current: AppIcon,
    onSelect: (AppIcon) -> Unit,
) {
    val options = listOf(
        AppIconOption(AppIcon.CLASSIC, R.string.app_icon_classic, R.drawable.icon_preview_classic),
        AppIconOption(AppIcon.MONO, R.string.app_icon_mono, R.drawable.icon_preview_mono),
        AppIconOption(AppIcon.CRIMSON, R.string.app_icon_crimson, R.drawable.icon_preview_crimson),
        AppIconOption(AppIcon.NOIR, R.string.app_icon_noir, R.drawable.icon_preview_noir),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val selected = current == option.icon
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(option.icon) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(option.previewRes),
                        contentDescription = stringResource(option.labelRes),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(option.labelRes),
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
