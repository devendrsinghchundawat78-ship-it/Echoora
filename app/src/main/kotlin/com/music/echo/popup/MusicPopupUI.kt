package iad1tya.echo.music.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import iad1tya.echo.music.R
import iad1tya.echo.music.models.MediaMetadata
import iad1tya.echo.music.ui.theme.PremiumColors
import iad1tya.echo.music.ui.theme.PremiumTheme
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Compose content rendered inside the floating overlay window.
 *
 * Pure presentation: all playback state and control actions flow through
 * [MusicPopupManager], which delegates to the single shared player.
 */
@Composable
fun MusicPopupOverlay(manager: MusicPopupManager) {
    val metadata by manager.metadata.collectAsState()
    val isPlaying by manager.isPlaying.collectAsState()
    val expanded by manager.expanded.collectAsState()
    val peeked by manager.peeked.collectAsState()
    val popupSize by manager.popupSize.collectAsState()

    PremiumTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = scaleIn(spring(dampingRatio = 0.7f, stiffness = 400f)) + fadeIn(),
                exit = scaleOut(spring(dampingRatio = 0.7f, stiffness = 400f)) + fadeOut(),
            ) {
                ExpandedPanel(
                    metadata = metadata,
                    isPlaying = isPlaying,
                    manager = manager,
                )
            }

            AnimatedVisibility(
                visible = !expanded,
                enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = 400f)) + fadeIn(),
                exit = scaleOut(spring(dampingRatio = 0.6f, stiffness = 400f)) + fadeOut(),
            ) {
                CollapsedBubble(
                    metadata = metadata,
                    isPlaying = isPlaying,
                    peeked = peeked,
                    bubbleSizeDp = popupSize.sizeDp,
                    manager = manager,
                )
            }
        }
    }
}

@Composable
private fun CollapsedBubble(
    metadata: MediaMetadata?,
    isPlaying: Boolean,
    peeked: Boolean,
    bubbleSizeDp: Float,
    manager: MusicPopupManager,
) {
    var totalX by remember { mutableFloatStateOf(0f) }
    var totalY by remember { mutableFloatStateOf(0f) }

    val handleRelease = {
        if (abs(totalX) < TAP_SLOP && abs(totalY) < TAP_SLOP) {
            if (peeked) manager.reveal() else manager.setExpanded(true)
        } else {
            manager.onDragEnd()
        }
    }

    Box(
        modifier = Modifier
            .size(bubbleSizeDp.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { totalX = 0f; totalY = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalX += dragAmount.x
                        totalY += dragAmount.y
                        manager.moveBy(dragAmount.x, dragAmount.y)
                    },
                    // Note: a plain tap ends in onDragCancel (slop never exceeded),
                    // while a real drag ends in onDragEnd. Handle both.
                    onDragEnd = { handleRelease() },
                    onDragCancel = { handleRelease() },
                )
            }
            .clip(CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF1E1E24), Color(0xFF0A0A0C)),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (metadata?.thumbnailUrl != null) {
            AsyncImage(
                model = metadata.thumbnailUrl,
                contentDescription = metadata.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.music_note),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp),
            )
        }
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(PremiumColors.Primary),
            )
        }
    }
}

@Composable
private fun ExpandedPanel(
    metadata: MediaMetadata?,
    isPlaying: Boolean,
    manager: MusicPopupManager,
) {
    var position by remember { mutableLongStateOf(manager.currentPosition()) }
    var duration by remember { mutableLongStateOf(manager.duration()) }

    LaunchedEffect(Unit) {
        while (true) {
            position = manager.currentPosition()
            duration = manager.duration()
            delay(500)
        }
    }

    val title = metadata?.title ?: "Nothing playing"
    val artist = metadata?.artists?.joinToString { it.name }.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF191921), Color(0xFF0A0A0C)),
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PremiumColors.SurfaceHigh),
                contentAlignment = Alignment.Center,
            ) {
                if (metadata?.thumbnailUrl != null) {
                    AsyncImage(
                        model = metadata.thumbnailUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.music_note),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (artist.isNotBlank()) {
                    Text(
                        text = artist,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { manager.setExpanded(false) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Collapse",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Slider(
            value = position.coerceIn(0L, duration.coerceAtLeast(1L)).toFloat(),
            onValueChange = { new ->
                position = new.toLong()
                manager.seekTo(new.toLong())
            },
            valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = PremiumColors.Primary,
                activeTrackColor = PremiumColors.Primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatTime(position), color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            Text(formatTime(duration), color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ControlButton(
                icon = if (metadata?.liked == true) R.drawable.favorite else R.drawable.favorite_border,
                tint = if (metadata?.liked == true) PremiumColors.Primary else Color.White.copy(alpha = 0.8f),
                onClick = { manager.onToggleLike() },
            )
            ControlButton(icon = R.drawable.skip_previous, onClick = { manager.previous() })
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PremiumColors.Primary)
                    .clickable { manager.togglePlayPause() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            ControlButton(icon = R.drawable.skip_next, onClick = { manager.next() })
        }
    }
}

@Composable
private fun ControlButton(
    icon: Int,
    onClick: () -> Unit,
    tint: Color = Color.White.copy(alpha = 0.85f),
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private const val TAP_SLOP = 8f
