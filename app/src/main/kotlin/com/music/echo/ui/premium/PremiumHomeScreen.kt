package iad1tya.echo.music.ui.premium

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.utils.parseCookieString
import iad1tya.echo.music.LocalPlayerConnection
import iad1tya.echo.music.R
import iad1tya.echo.music.constants.InnerTubeCookieKey
import iad1tya.echo.music.extensions.toggleRepeatMode
import iad1tya.echo.music.models.toMediaMetadata
import iad1tya.echo.music.playback.PlayerConnection
import iad1tya.echo.music.playback.queues.YouTubeQueue
import iad1tya.echo.music.ui.theme.PremiumColors
import iad1tya.echo.music.ui.theme.PremiumTheme
import iad1tya.echo.music.utils.rememberPreference
import iad1tya.echo.music.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import androidx.media3.common.Player

/**
 * Theme 2 home screen — premium, dark, minimal.
 *
 * Reuses the exact same data and player state as the original home screen
 * ([iad1tya.echo.music.ui.screens.HomeScreen]); it is purely another presentation
 * layer driven by [HomeViewModel] and [PlayerConnection].
 */

private data class PremiumCategory(
    val id: String,
    val title: String,
)

// Data-driven categories (kept out of business logic; extend the list freely).
private val PremiumCategories = listOf(
    PremiumCategory("all", "All"),
    PremiumCategory("relax", "Relax"),
    PremiumCategory("party", "Party"),
    PremiumCategory("lofi", "Lofi"),
    PremiumCategory("romantic", "Romantic"),
)

private data class TrendingEntry(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String?,
    val rank: Int,
    val onClick: () -> Unit,
)

@Composable
fun PremiumHomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current ?: return

    val accountName by viewModel.accountName.collectAsState()
    val accountImageUrl by viewModel.accountImageUrl.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val quickPicks by viewModel.quickPicks.collectAsState()

    val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }

    val trending: List<TrendingEntry> = remember(homePage, quickPicks) {
        val homeSongs = homePage?.sections
            ?.flatMap { section -> section.items.filterIsInstance<SongItem>() }
            ?.distinctBy { it.id }
        if (!homeSongs.isNullOrEmpty()) {
            homeSongs.mapIndexed { index, song ->
                TrendingEntry(
                    id = song.id,
                    title = song.title,
                    artist = song.artists.joinToString { it.name },
                    thumbnail = song.thumbnail,
                    rank = index + 1,
                    onClick = {
                        playerConnection.playQueue(
                            YouTubeQueue(
                                song.endpoint ?: WatchEndpoint(videoId = song.id),
                                song.toMediaMetadata(),
                            )
                        )
                    },
                )
            }
        } else {
            quickPicks.orEmpty().mapIndexed { index, song ->
                TrendingEntry(
                    id = song.id,
                    title = song.title,
                    artist = song.artists.joinToString { it.name },
                    thumbnail = song.thumbnailUrl,
                    rank = index + 1,
                    onClick = { playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata())) },
                )
            }
        }
    }

    PremiumTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumColors.Background)
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 180.dp),
        ) {
            item {
                GreetingHeader(
                    name = accountName,
                    avatarUrl = if (isLoggedIn) accountImageUrl else null,
                    onAvatarClick = {
                        navController.navigate(if (isLoggedIn) "settings/account" else "login")
                    },
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                CategoriesSection()
            }

            item { Spacer(Modifier.height(28.dp)) }

            item {
                SectionTitle(title = "Trending Songs", subtitle = "See all")
            }

            item {
                TrendingCarousel(entries = trending)
            }

            item { Spacer(Modifier.height(32.dp)) }

            item {
                SectionTitle(title = "Playing", subtitle = "Now Playing")
            }

            item {
                NowPlayingSection(playerConnection = playerConnection)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = PremiumColors.OnBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            color = PremiumColors.OnSurfaceVariant,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun GreetingHeader(
    name: String,
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hey, $name",
                color = PremiumColors.OnBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Discover music that moves you",
                color = PremiumColors.OnSurfaceVariant,
                fontSize = 14.sp,
            )
        }
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(PremiumColors.SurfaceHigh)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.account),
                    contentDescription = null,
                    tint = PremiumColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoriesSection() {
    var selected by rememberSaveable { mutableStateOf("all") }
    Column {
        Text(
            text = "Categories",
            color = PremiumColors.OnBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PremiumCategories.forEach { category ->
                CategoryChip(
                    category = category,
                    selected = selected == category.id,
                    onClick = { selected = category.id },
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: PremiumCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "chipScale",
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) PremiumColors.Primary else PremiumColors.SurfaceHigh
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = category.title,
            color = if (selected) Color.White else PremiumColors.OnSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun TrendingCarousel(entries: List<TrendingEntry>) {
    if (entries.isEmpty()) {
        Text(
            text = "Pull to refresh for trending songs",
            color = PremiumColors.OnSurfaceVariant,
            fontSize = 14.sp,
        )
        return
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            TrendingCard(entry = entry)
        }
    }
}

@Composable
private fun TrendingCard(entry: TrendingEntry) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "cardScale",
    )
    Column(
        modifier = Modifier
            .width(150.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(20.dp))
            .background(PremiumColors.Surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = entry.onClick,
            )
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(PremiumColors.SurfaceVariant),
        ) {
            if (entry.thumbnail != null) {
                AsyncImage(
                    model = entry.thumbnail,
                    contentDescription = entry.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.music_note),
                    contentDescription = null,
                    tint = PremiumColors.OnSurfaceVariant,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                )
            }
            Text(
                text = "#${entry.rank}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = entry.title,
            color = PremiumColors.OnBackground,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = entry.artist,
            color = PremiumColors.OnSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NowPlayingSection(playerConnection: PlayerConnection) {
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val shuffle by playerConnection.shuffleModeEnabled.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    LaunchedEffect(mediaMetadata?.id) {
        while (true) {
            position = playerConnection.player.currentPosition
            duration = playerConnection.player.duration
            delay(500)
        }
    }

    val metadata = mediaMetadata
    val title = metadata?.title ?: "Nothing playing"
    val artist = metadata?.artists?.joinToString { it.name }.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(PremiumColors.PlayerGradientStart, PremiumColors.PlayerGradientEnd)
                )
            )
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.25f)),
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
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (artist.isNotBlank()) {
                    Text(
                        text = artist,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Slider(
            value = position.coerceIn(0L, duration.coerceAtLeast(1L)).toFloat(),
            onValueChange = { playerConnection.seekTo(it.toLong()) },
            valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.35f),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(position),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
            )
            Text(
                text = formatTime(duration),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PremiumControlButton(
                icon = if (metadata?.liked == true) R.drawable.favorite else R.drawable.favorite_border,
                onClick = { playerConnection.toggleLike() },
            )
            PremiumControlButton(
                icon = R.drawable.skip_previous,
                onClick = { playerConnection.seekToPrevious() },
            )
            // Prominent play / pause
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { playerConnection.togglePlayPause() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.pause else R.drawable.play
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = PremiumColors.PlayerGradientEnd,
                    modifier = Modifier.size(28.dp),
                )
            }
            PremiumControlButton(
                icon = R.drawable.skip_next,
                onClick = { playerConnection.seekToNext() },
            )
            PremiumControlButton(
                icon = if (shuffle) R.drawable.shuffle_on else R.drawable.shuffle,
                tint = if (shuffle) Color.White else Color.White.copy(alpha = 0.6f),
                onClick = { playerConnection.player.shuffleModeEnabled = !shuffle },
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            PremiumControlButton(
                icon = when {
                    repeatMode == Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                    repeatMode != Player.REPEAT_MODE_OFF -> R.drawable.repeat_on
                    else -> R.drawable.repeat
                },
                tint = if (repeatMode != Player.REPEAT_MODE_OFF) Color.White else Color.White.copy(alpha = 0.7f),
                onClick = { playerConnection.player.toggleRepeatMode() },
                size = 36.dp,
            )
        }
    }
}

@Composable
private fun PremiumControlButton(
    icon: Int,
    onClick: () -> Unit,
    tint: Color = Color.White.copy(alpha = 0.92f),
    size: Dp = 44.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "controlScale",
    )
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
