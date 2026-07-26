package iad1tya.echo.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * iOS Siri-style dynamic soundwave visualizer.
 * Shows animated bars that move with the music when playing,
 * and settle to a flat line when paused.
 */
@Composable
fun SoundWaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barWidth: Dp = 2.5.dp,
    barSpacing: Dp = 1.5.dp,
    maxHeight: Dp = 18.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SoundWave")

    // Each bar gets its own phase offset for natural wave motion
    val phases = remember(barCount) {
        List(barCount) { index ->
            0.2f + (index * 0.35f) // Staggered phase offsets
        }
    }

    // Animate a global progress value for smooth continuous motion
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    // Smooth transition for play/pause state
    val playPauseAlpha = if (isPlaying) 1f else 0.3f

    Canvas(
        modifier = modifier
            .width((barWidth + barSpacing) * barCount - barSpacing)
            .height(maxHeight)
    ) {
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        val maxHeightPx = maxHeight.toPx()
        val cornerRadius = barWidthPx / 2f

        for (i in 0 until barCount) {
            val phase = phases[i]

            // Create organic wave motion using sine waves
            val wave1 = sin((progress.value + phase) * 2f * PI.toFloat()).coerceIn(-1f, 1f)
            val wave2 = sin((progress.value * 1.5f + phase * 0.7f) * 2f * PI.toFloat()).coerceIn(-1f, 1f)

            // Combine waves for more natural movement
            val combinedWave = (wave1 * 0.6f + wave2 * 0.4f)

            // Map to height: center bars taller, edges shorter
            val centerBias = 1f - kotlin.math.abs(i - (barCount - 1) / 2f) / (barCount / 2f) * 0.3f
            val normalizedHeight = ((combinedWave + 1f) / 2f) * centerBias

            // When playing: full animation. When paused: minimal height
            val barHeight = if (isPlaying) {
                (normalizedHeight * 0.7f + 0.3f) * maxHeightPx * playPauseAlpha
            } else {
                maxHeightPx * 0.15f // Small flat bars when paused
            }

            val x = i * (barWidthPx + barSpacingPx)
            val y = (maxHeightPx - barHeight) / 2f

            drawRoundRect(
                color = barColor.copy(alpha = if (isPlaying) 0.9f else 0.4f),
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}
