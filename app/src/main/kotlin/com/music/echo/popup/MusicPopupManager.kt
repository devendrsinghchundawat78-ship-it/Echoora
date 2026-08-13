package iad1tya.echo.music.popup

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.animation.ValueAnimator
import androidx.datastore.preferences.core.edit
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import iad1tya.echo.music.constants.MusicPopupAutoHideKey
import iad1tya.echo.music.constants.MusicPopupEnabledKey
import iad1tya.echo.music.constants.MusicPopupSideKey
import iad1tya.echo.music.constants.MusicPopupSizeKey
import iad1tya.echo.music.constants.MusicPopupXKey
import iad1tya.echo.music.constants.MusicPopupYKey
import iad1tya.echo.music.models.MediaMetadata
import iad1tya.echo.music.utils.dataStore
import iad1tya.echo.music.utils.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages the floating Music Popup overlay window.
 *
 * The popup is a single [PopupComposeHost] added to the [WindowManager]. It reads the
 * SAME playback state exposed by [MusicService] (the [ExoPlayer] and its
 * [MediaMetadata] flow) — there is no second audio player or media session.
 *
 * Visibility rule: popup shows only while
 *   - the feature is enabled,
 *   - the overlay permission is granted,
 *   - a track is actually playing, and
 *   - the user has left the Echoora app (app in background).
 */
class MusicPopupManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val playerProvider: () -> ExoPlayer?,
    metadataFlow: StateFlow<MediaMetadata?>,
    private val toggleLike: () -> Unit,
) {
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val density = context.resources.displayMetrics.density
    private val screenWidthPx get() = context.resources.displayMetrics.widthPixels
    private val screenHeightPx get() = context.resources.displayMetrics.heightPixels

    // Live state exposed to the Compose overlay.
    val metadata: StateFlow<MediaMetadata?> = metadataFlow
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    private val _expanded = MutableStateFlow(false)
    val expanded: StateFlow<Boolean> = _expanded
    private val _peeked = MutableStateFlow(false)
    val peeked: StateFlow<Boolean> = _peeked
    private val _popupSize = MutableStateFlow(MusicPopupSize.MEDIUM)
    val popupSize: StateFlow<MusicPopupSize> = _popupSize
    private val _autoHide = MutableStateFlow(true)
    val autoHide: StateFlow<Boolean> = _autoHide

    // Cached preference values used by the visibility decision.
    @Volatile private var enabled = false
    @Volatile private var side = MusicPopupSide.RIGHT

    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isVisible = false
    private var positionAnimator: ValueAnimator? = null
    private var isAppInForeground = true
    private var observeJob: kotlinx.coroutines.Job? = null

    // Cached system-bar safe-area insets (refreshed each time the popup shows,
    // so rotation / config changes are respected). These keep the popup from
    // overlapping the status bar or the navigation/gesture area.
    @Volatile private var topInsetPx = 0
    @Volatile private var bottomInsetPx = 0

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        private var started = 0

        override fun onActivityStarted(activity: Activity) {
            started++
            isAppInForeground = true
            updateVisibility()
        }

        override fun onActivityStopped(activity: Activity) {
            started = (started - 1).coerceAtLeast(0)
            if (started == 0) {
                isAppInForeground = false
                updateVisibility()
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            updateVisibility()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                _isPlaying.value = false
            }
            updateVisibility()
        }
    }

    fun start() {
        registerLifecycleCallbacks()
        val player = playerProvider()
        player?.addListener(playerListener)
        // Handle the case where music is already playing when the manager starts
        // (e.g. the service was recreated while a track was active).
        _isPlaying.value = player?.isPlaying == true
        observePreferences()
    }

    fun dispose() {
        observeJob?.cancel()
        observeJob = null
        unregisterLifecycleCallbacks()
        try {
            playerProvider()?.removeListener(playerListener)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error removing player listener")
        }
        positionAnimator?.cancel()
        hidePopup()
    }

    private fun registerLifecycleCallbacks() {
        (context.applicationContext as? Application)
            ?.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    private fun unregisterLifecycleCallbacks() {
        (context.applicationContext as? Application)
            ?.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    private fun observePreferences() {
        observeJob = scope.launch {
            context.dataStore.data
                .map { prefs ->
                    MusicPopupPrefsSnapshot(
                        enabled = prefs[MusicPopupEnabledKey] ?: false,
                        size = MusicPopupSize.fromStorage(prefs[MusicPopupSizeKey]),
                        side = MusicPopupSide.fromStorage(prefs[MusicPopupSideKey]),
                        autoHide = prefs[MusicPopupAutoHideKey] ?: true,
                    )
                }
                .distinctUntilChanged()
                .collect { snapshot ->
                    enabled = snapshot.enabled
                    side = snapshot.side
                    _popupSize.value = snapshot.size
                    _autoHide.value = snapshot.autoHide
                    updateVisibility()
                }
        }
    }

    private fun hasOverlayPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    private fun updateVisibility() {
        val shouldShow = enabled &&
            hasOverlayPermission() &&
            _isPlaying.value &&
            !isAppInForeground
        if (shouldShow) showPopup() else hidePopup()
    }

    // ---------------------------------------------------------------------
    // Window management
    // ---------------------------------------------------------------------

    private fun bubbleSizePx(): Int = (_popupSize.value.sizeDp * density).toInt()

    private fun panelWidthPx(): Int = (PANEL_WIDTH_DP * density).toInt()
    private fun panelHeightPx(): Int = (PANEL_HEIGHT_DP * density).toInt()

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val size = bubbleSizePx()
        return WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun showPopup() {
        if (isVisible) return
        positionAnimator?.cancel()
        refreshSafeInsets()

        val view = PopupComposeHost(context) {
            MusicPopupOverlay(this@MusicPopupManager)
        }
        layoutParams = createLayoutParams()
        restoreOrPlaceDefaultPosition()
        try {
            windowManager.addView(view, layoutParams)
            overlayView = view
            isVisible = true
            _expanded.value = false
            _peeked.value = false
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to add popup window")
            overlayView = null
            layoutParams = null
        }
    }

    private fun hidePopup() {
        if (!isVisible) return
        positionAnimator?.cancel()
        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Error removing popup window")
        }
        overlayView = null
        layoutParams = null
        isVisible = false
        _expanded.value = false
        _peeked.value = false
    }

    /** Re-adds/refreshes window params when the collapsed/expanded size changes. */
    private fun updateLayoutSize() {
        val view = overlayView ?: return
        val lp = layoutParams ?: return
        if (_expanded.value) {
            lp.width = panelWidthPx()
            lp.height = panelHeightPx()
        } else {
            val size = bubbleSizePx()
            lp.width = size
            lp.height = size
        }
        clampPosition(lp)
        try {
            windowManager.updateViewLayout(view, lp)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Error updating popup layout")
        }
    }

    /**
     * Reads the current status-bar (top) and navigation/gesture (bottom) insets
     * and caches them. Used to keep the popup inside the usable screen area.
     */
    private fun refreshSafeInsets() {
        topInsetPx = statusBarHeightPx()
        bottomInsetPx = navigationBarHeightPx()
    }

    private fun statusBarHeightPx(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.windowInsets
                .getInsets(WindowInsets.Type.statusBars()).top
        } else {
            context.resources
                .getIdentifier("status_bar_height", "dimen", "android")
                .takeIf { it > 0 }
                ?.let { context.resources.getDimensionPixelSize(it) }
                ?: 0
        }

    private fun navigationBarHeightPx(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.windowInsets
                .getInsets(WindowInsets.Type.navigationBars()).bottom
        } else {
            context.resources
                .getIdentifier("navigation_bar_height", "dimen", "android")
                .takeIf { it > 0 }
                ?.let { context.resources.getDimensionPixelSize(it) }
                ?: 0
        }

    /** Minimum Y that keeps the popup below the status bar. */
    private fun minY(): Int = topInsetPx + (SAFE_MARGIN_DP * density).toInt()

    /** Maximum Y that keeps the popup above the navigation/gesture area. */
    private fun maxY(height: Int): Int =
        (screenHeightPx - bottomInsetPx - (SAFE_MARGIN_DP * density).toInt() - height)
            .coerceAtLeast(minY())

    private fun clampPosition(lp: WindowManager.LayoutParams) {
        lp.x = lp.x.coerceIn(0, (screenWidthPx - lp.width).coerceAtLeast(0))
        lp.y = lp.y.coerceIn(minY(), maxY(lp.height))
    }

    private fun restoreOrPlaceDefaultPosition() {
        val lp = layoutParams ?: return
        val storedX = context.dataStore.get(MusicPopupXKey)
        val storedY = context.dataStore.get(MusicPopupYKey)
        val size = bubbleSizePx()

        if (storedX != null && storedY != null) {
            lp.x = storedX.toInt()
            lp.y = storedY.toInt()
        } else {
            lp.x = if (side == MusicPopupSide.LEFT) 0 else (screenWidthPx - size).coerceAtLeast(0)
            lp.y = (screenHeightPx * 0.35f).toInt()
        }
        clampPosition(lp)
    }

    // ---------------------------------------------------------------------
    // Player control (delegates to the single source of truth)
    // ---------------------------------------------------------------------

    fun togglePlayPause() {
        playerProvider()?.let { it.playWhenReady = !it.playWhenReady }
    }

    fun next() {
        playerProvider()?.let {
            it.seekToNext()
            it.playWhenReady = true
        }
    }

    fun previous() {
        playerProvider()?.let {
            if (it.currentPosition > 3000 || !it.hasPreviousMediaItem()) {
                it.seekTo(0)
            } else {
                it.seekToPreviousMediaItem()
            }
            it.playWhenReady = true
        }
    }

    fun onToggleLike() = toggleLike()

    fun seekTo(positionMs: Long) {
        playerProvider()?.seekTo(positionMs)
    }

    fun currentPosition(): Long = playerProvider()?.currentPosition ?: 0L
    fun duration(): Long = playerProvider()?.duration ?: 0L

    // ---------------------------------------------------------------------
    // Expand / collapse
    // ---------------------------------------------------------------------

    fun setExpanded(value: Boolean) {
        if (!isVisible || _expanded.value == value) return
        _peeked.value = false
        _expanded.value = value
        if (value) {
            val lp = layoutParams ?: return
            lp.x = if (side == MusicPopupSide.LEFT) 0
            else (screenWidthPx - panelWidthPx()).coerceAtLeast(0)
        }
        updateLayoutSize()
    }

    // ---------------------------------------------------------------------
    // Dragging + edge snapping
    // ---------------------------------------------------------------------

    /** Moves the window by the given pixel delta while the user drags. */
    fun moveBy(dx: Float, dy: Float) {
        val lp = layoutParams ?: return
        val view = overlayView ?: return
        lp.x = (lp.x + dx.toInt()).coerceIn(0, (screenWidthPx - lp.width).coerceAtLeast(0))
        lp.y = (lp.y + dy.toInt()).coerceIn(minY(), maxY(lp.height))
        try {
            windowManager.updateViewLayout(view, lp)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Error moving popup window")
        }
    }

    /** Called when the user releases the bubble after dragging. */
    fun onDragEnd() {
        val lp = layoutParams ?: return
        persistPosition(lp)
        snapToEdge()
    }

    /** Tapping a peeked bubble reveals it fully. */
    fun reveal() {
        if (!_peeked.value) return
        _peeked.value = false
        animateX(targetEdgeX())
    }

    private fun persistPosition(lp: WindowManager.LayoutParams) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[MusicPopupXKey] = lp.x.toFloat()
                prefs[MusicPopupYKey] = lp.y.toFloat()
            }
        }
    }

    private fun edgeX(): Int {
        val lp = layoutParams ?: return 0
        val size = if (_expanded.value) panelWidthPx() else bubbleSizePx()
        return if (side == MusicPopupSide.LEFT) 0 else (screenWidthPx - size).coerceAtLeast(0)
    }

    private fun targetEdgeX(): Int = edgeX()

    private fun peekX(): Int {
        val size = bubbleSizePx()
        val sliver = (PEEK_SLIVER_DP * density).toInt()
        return if (side == MusicPopupSide.LEFT) sliver - size else screenWidthPx - sliver
    }

    private fun snapToEdge() {
        val lp = layoutParams ?: return
        val size = if (_expanded.value) panelWidthPx() else bubbleSizePx()
        val nearestLeft = lp.x + lp.width / 2 < screenWidthPx / 2
        side = if (nearestLeft) MusicPopupSide.LEFT else MusicPopupSide.RIGHT
        val edge = if (nearestLeft) 0 else (screenWidthPx - size).coerceAtLeast(0)
        animateX(edge) {
            if (_autoHide.value && !_expanded.value) {
                // Slide most of the bubble off-screen, leaving a thin handle.
                _peeked.value = true
                animateX(peekX())
            }
        }
    }

    private fun animateX(to: Int, onEnd: (() -> Unit)? = null) {
        val lp = layoutParams ?: run { onEnd?.invoke(); return }
        positionAnimator?.cancel()
        val from = lp.x
        val animator = ValueAnimator.ofInt(from, to).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                val value = va.animatedValue as Int
                val current = layoutParams ?: return@addUpdateListener
                val view = overlayView ?: return@addUpdateListener
                current.x = value
                try {
                    windowManager.updateViewLayout(view, current)
                } catch (e: Exception) {
                    // Window may have been removed; ignore.
                }
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd?.invoke()
                }
            })
        }
        positionAnimator = animator
        animator.start()
    }

    private data class MusicPopupPrefsSnapshot(
        val enabled: Boolean,
        val size: MusicPopupSize,
        val side: MusicPopupSide,
        val autoHide: Boolean,
    )

    private companion object {
        private const val TAG = "MusicPopupManager"
        private const val PANEL_WIDTH_DP = 300f
        private const val PANEL_HEIGHT_DP = 208f
        private const val PEEK_SLIVER_DP = 16f
        private const val SAFE_MARGIN_DP = 8f
    }
}
