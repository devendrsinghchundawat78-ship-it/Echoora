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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Ultra-premium futuristic audio waveform visualizer.
 * iOS Siri-style with gradient neon colors, glow effects, and smooth organic motion.
 */
@Composable
fun SoundWaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 7,
    barColor: Color = Color(0xFF3A86FF), // Electric Blue
    barWidth: Dp = 2.dp,
    barSpacing: Dp = 2.dp,
    maxHeight: Dp = 20.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SoundWave")

    // Multiple animation speeds for layered motion
    val progress1 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val progress2 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    val progress3 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3"
    )

    // Neon gradient colors: Electric Blue → Cyan → Purple → Violet
    val neonColors = listOf(
        Color(0xFF3A86FF), // Electric Blue
        Color(0xFF00E5FF), // Cyan
        Color(0xFF8B5CF6), // Neon Purple
        Color(0xFFA855F7), // Violet
        Color(0xFFFF7A18), // Orange
        Color(0xFFFFB000), // Amber
        Color(0xFF3A86FF), // Electric Blue (loop)
    )

    val phases = remember(barCount) {
        List(barCount) { index ->
            0.15f + (index * 0.28f)
        }
    }

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

            // 3-layer sine wave for ultra-smooth organic motion
            val w1 = sin((progress1.value + phase) * 2f * PI.toFloat())
            val w2 = sin((progress2.value * 1.3f + phase * 0.6f) * 2f * PI.toFloat())
            val w3 = sin((progress3.value * 0.7f + phase * 1.2f) * PI.toFloat())

            val combined = (w1 * 0.45f + w2 * 0.35f + w3 * 0.2f).coerceIn(-1f, 1f)

            // Center-biased height distribution
            val centerBias = 1f - kotlin.math.abs(i - (barCount - 1) / 2f) / (barCount / 2f) * 0.25f
            val normalized = ((combined + 1f) / 2f) * centerBias

            val barHeight = if (isPlaying) {
                (normalized * 0.65f + 0.35f) * maxHeightPx
            } else {
                maxHeightPx * 0.12f
            }

            val x = i * (barWidthPx + barSpacingPx)
            val y = (maxHeightPx - barHeight) / 2f

            // Gradient color per bar
            val colorIndex = i.toFloat() / (barCount - 1).coerceAtLeast(1) * (neonColors.size - 1)
            val ci = colorIndex.toInt().coerceIn(0, neonColors.size - 2)
            val frac = colorIndex - ci
            val barNeonColor = androidx.compose.ui.graphics.lerp(neonColors[ci], neonColors[ci + 1], frac)

            // Glow layer (slightly larger, more transparent)
            if (isPlaying) {
                drawRoundRect(
                    color = barNeonColor.copy(alpha = 0.2f),
                    topLeft = Offset(x - 1.dp.toPx(), y - 1.dp.toPx()),
                    size = Size(barWidthPx + 2.dp.toPx(), barHeight + 2.dp.toPx()),
                    cornerRadius = CornerRadius(cornerRadius + 1.dp.toPx())
                )
            }

            // Main bar
            drawRoundRect(
                color = if (isPlaying) barNeonColor.copy(alpha = 0.85f) else barNeonColor.copy(alpha = 0.3f),
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(cornerRadius)
            )
        }
    }
}
