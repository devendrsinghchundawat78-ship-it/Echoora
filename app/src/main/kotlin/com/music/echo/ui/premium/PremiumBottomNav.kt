package iad1tya.echo.music.ui.premium

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.ui.component.floatingtabbar.FloatingTabBarScrollConnection
import iad1tya.echo.music.ui.screens.Screens
import iad1tya.echo.music.ui.theme.PremiumColors

/**
 * Theme 2 floating bottom navigation — dark, rounded, with a red accent active
 * state and subtle scale/opacity press feedback.
 *
 * Shares the exact same signature and routes as [iad1tya.echo.music.ui.component.AppFloatingNavBar],
 * so it can be swapped in via [iad1tya.echo.music.ui.uitheme.UiThemeSelector] without touching
 * navigation logic.
 */
@Composable
fun PremiumFloatingNavBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    scrollConnection: FloatingTabBarScrollConnection,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    showPlayerAccessory: Boolean = false,
    onAccessoryClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(PremiumColors.SurfaceHigh)
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigationItems.forEach { screen ->
            val isSelected = remember(currentRoute, screen.route) {
                currentRoute == screen.route || currentRoute?.startsWith("${screen.route}/") == true
            }
            PremiumNavItem(
                screen = screen,
                isSelected = isSelected,
                onClick = { onItemClick(screen, isSelected) },
            )
        }
    }
}

@Composable
private fun RowScope.PremiumNavItem(
    screen: Screens,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PremiumColors.Primary else PremiumColors.OnSurfaceVariant,
        animationSpec = tween(200),
        label = "premiumNavContentColor",
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "premiumNavScale",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (pressed) 0.92f else 1f

    Column(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer {
                scaleX = scale * pressScale
                scaleY = scale * pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(screen.titleId),
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = stringResource(screen.titleId),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
