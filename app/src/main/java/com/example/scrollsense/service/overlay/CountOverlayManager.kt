package com.example.scrollsense.service.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView

class CountOverlayManager(private val context: Context) {

    companion object {
        private const val TAG = "ScrollSense_Overlay"
        private const val TOTAL_VISIBLE_DURATION_MS = 5000L // Stays visible for ~5 seconds
        private const val FADE_OUT_DURATION_MS = 500L       // Smooth fade-out during final ~500ms
        private const val FADE_IN_DURATION_MS = 250L        // Smooth fade-in when it appears
        private const val DELAY_BEFORE_FADE_OUT_MS = TOTAL_VISIBLE_DURATION_MS - FADE_OUT_DURATION_MS // 4500ms
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var overlayRoot: FrameLayout? = null
    private var pillLayout: FrameLayout? = null
    private var textView: TextView? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var isAttached = false
    private var currentCount = -1

    // Tracks when the current 5-second display window ends
    private var displayEndTime = 0L

    // Scheduled task to perform the final ~500ms fade-out
    private val fadeOutRunnable = Runnable {
        startFadeOut()
    }

    init {
        createView()
    }

    private fun createView() {
        val root = FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
        }

        val pill = FrameLayout(context).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(16f)
                // Sleek, high-contrast dark slate pill with subtle cyan border
                setColor(Color.parseColor("#EE0B0F17"))
                setStroke(dpToPx(1.5f).toInt(), Color.parseColor("#38BDF8"))
            }
            background = bg
            // Generous horizontal padding so text never touches rounded borders
            setPadding(
                dpToPx(18f).toInt(),
                dpToPx(7f).toInt(),
                dpToPx(18f).toInt(),
                dpToPx(7f).toInt()
            )
            // Initial state for smooth fade-in entrance
            alpha = 0f
            scaleX = 0.95f
            scaleY = 0.95f
            translationY = -dpToPx(8f)
        }

        val tv = TextView(context).apply {
            setTextColor(Color.parseColor("#F8FAFC"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            isSingleLine = true
            maxLines = 1
            ellipsize = null // Explicitly avoid ellipsis truncation
            includeFontPadding = false
            // Subtle shadow for readability over high-contrast video content
            setShadowLayer(4f, 0f, 1f, Color.parseColor("#B3000000"))
            text = "Short videos today: 0"
        }

        val tvParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        pill.addView(tv, tvParams)

        // Pill wraps its content width and is centered horizontally with safe margins
        val pillParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            marginStart = dpToPx(16f).toInt()
            marginEnd = dpToPx(16f).toInt()
        }
        root.addView(pill, pillParams)

        // Listen for system window insets so overlay gracefully avoids camera cutouts/notches
        root.setOnApplyWindowInsetsListener { _, insets ->
            val topInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(
                    WindowInsets.Type.statusBars() or WindowInsets.Type.displayCutout()
                )
                bars.top
            } else {
                @Suppress("DEPRECATION")
                insets.systemWindowInsetTop
            }
            if (topInset > 0) {
                val safeY = maxOf(topInset + dpToPx(6f).toInt(), dpToPx(44f).toInt())
                windowParams?.let { p ->
                    if (p.y != safeY && isAttached && overlayRoot != null) {
                        p.y = safeY
                        try {
                            windowManager.updateViewLayout(root, p)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to update layout insets: ${e.message}")
                        }
                    }
                }
            }
            insets
        }

        overlayRoot = root
        pillLayout = pill
        textView = tv
    }

    /**
     * Updates the unified counter text immediately on the main thread and displays
     * the counter for approximately 5 seconds before fading out.
     *
     * If another new Short/Reel is detected while the counter is already visible (or fading out),
     * the counter text updates immediately and restarts the 5-second visibility timer.
     */
    fun updateCount(count: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            applyCountUpdate(count)
        } else {
            mainHandler.post {
                applyCountUpdate(count)
            }
        }
    }

    private fun applyCountUpdate(count: Int) {
        currentCount = count
        val displayText = "Short videos today: $count"
        textView?.text = displayText
        pillLayout?.requestLayout()
        overlayRoot?.requestLayout()

        // Cancel any pending fade-out callback
        mainHandler.removeCallbacks(fadeOutRunnable)
        displayEndTime = System.currentTimeMillis() + TOTAL_VISIBLE_DURATION_MS

        val root = overlayRoot
        val pill = pillLayout
        if (root == null || pill == null) return

        if (!isAttached || root.visibility != View.VISIBLE) {
            // New entrance: show overlay and animate smooth fade-in
            showInternal()
        } else if (pill.alpha < 0.95f) {
            // Was fading out: cancel fade-out and restore full opacity smoothly
            pill.animate()?.cancel()
            pill.animate()
                ?.alpha(1f)
                ?.scaleX(1f)
                ?.scaleY(1f)
                ?.translationY(0f)
                ?.setDuration(150L)
                ?.setInterpolator(DecelerateInterpolator())
                ?.start()
            triggerTextPulse()
        } else {
            // Already fully visible: cancel any animator and micro-pulse text to confirm swipe
            pill.animate()?.cancel()
            pill.alpha = 1f
            pill.scaleX = 1f
            pill.scaleY = 1f
            pill.translationY = 0f
            triggerTextPulse()
        }

        // Schedule smooth fade-out during final ~500ms of the 5-second window
        mainHandler.postDelayed(fadeOutRunnable, DELAY_BEFORE_FADE_OUT_MS)

        Log.d(TAG, "Unified overlay updated: '$displayText', visible for ~5s")
    }

    private fun triggerTextPulse() {
        textView?.animate()
            ?.scaleX(1.08f)
            ?.scaleY(1.08f)
            ?.setDuration(80L)
            ?.withEndAction {
                textView?.animate()
                    ?.scaleX(1f)
                    ?.scaleY(1f)
                    ?.setDuration(120L)
                    ?.start()
            }
            ?.start()
    }

    fun show() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showInternal()
        } else {
            mainHandler.post {
                showInternal()
            }
        }
    }

    private fun showInternal() {
        val root = overlayRoot ?: return

        if (!isAttached) {
            try {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    y = calculateTopPosition()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }
                windowParams = params

                windowManager.addView(root, params)
                isAttached = true
                root.visibility = View.VISIBLE
                animateEntrance()
                Log.d(TAG, "Unified overlay attached to WindowManager at top-center")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to attach overlay: ${e.message}")
            }
        } else {
            root.visibility = View.VISIBLE
            windowParams?.let { p ->
                try {
                    windowManager.updateViewLayout(root, p)
                } catch (_: Exception) {}
            }
            animateEntrance()
        }
    }

    /**
     * Smooth fade-in entrance when the counter first appears.
     */
    private fun animateEntrance() {
        val pill = pillLayout ?: return
        pill.animate()?.cancel()
        pill.alpha = 0f
        pill.scaleX = 0.95f
        pill.scaleY = 0.95f
        pill.translationY = -dpToPx(8f)

        pill.animate()
            ?.alpha(1f)
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.translationY(0f)
            ?.setDuration(FADE_IN_DURATION_MS)
            ?.setInterpolator(DecelerateInterpolator())
            ?.start()
    }

    /**
     * Smooth fade-out during the final ~500ms of the 5-second visibility window.
     */
    private fun startFadeOut() {
        if (!isAttached || overlayRoot == null) return
        val pill = pillLayout ?: return

        pill.animate()?.cancel()
        pill.animate()
            ?.alpha(0f)
            ?.scaleX(0.96f)
            ?.scaleY(0.96f)
            ?.translationY(-dpToPx(6f))
            ?.setDuration(FADE_OUT_DURATION_MS)
            ?.setInterpolator(AccelerateInterpolator())
            ?.withEndAction {
                // Only hide root view if alpha completed at 0 and was not interrupted
                if (pill.alpha <= 0.05f) {
                    overlayRoot?.visibility = View.GONE
                    Log.d(TAG, "Unified overlay 5s window completed; view hidden")
                }
            }
            ?.start()
        Log.d(TAG, "Unified overlay final 500ms fade-out started")
    }

    /**
     * Hides the overlay. If a 5-second display window is active and [force] is false,
     * the overlay stays visible until its 5-second countdown finishes.
     */
    fun hide(force: Boolean = false) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            hideInternal(force)
        } else {
            mainHandler.post {
                hideInternal(force)
            }
        }
    }

    private fun hideInternal(force: Boolean = false) {
        val now = System.currentTimeMillis()
        // If 5-second timer is currently active and not forced, let the 5-second countdown finish
        if (!force && now < displayEndTime) {
            Log.d(TAG, "Ignoring non-forced hide; 5s visibility window is active")
            return
        }

        mainHandler.removeCallbacks(fadeOutRunnable)
        displayEndTime = 0L

        if (isAttached && overlayRoot != null) {
            val pill = pillLayout ?: return
            pill.animate()?.cancel()
            pill.animate()
                ?.alpha(0f)
                ?.scaleX(0.96f)
                ?.scaleY(0.96f)
                ?.translationY(-dpToPx(6f))
                ?.setDuration(FADE_OUT_DURATION_MS)
                ?.setInterpolator(AccelerateInterpolator())
                ?.withEndAction {
                    if (pill.alpha <= 0.05f) {
                        overlayRoot?.visibility = View.GONE
                    }
                }
                ?.start()
            Log.d(TAG, "Unified overlay fade-out initiated")
        }
    }

    fun destroy() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            destroyInternal()
        } else {
            mainHandler.post {
                destroyInternal()
            }
        }
    }

    private fun destroyInternal() {
        mainHandler.removeCallbacks(fadeOutRunnable)
        displayEndTime = 0L

        if (isAttached && overlayRoot != null) {
            try {
                windowManager.removeView(overlayRoot)
                isAttached = false
                Log.d(TAG, "Unified overlay removed from WindowManager")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view: ${e.message}")
            }
        }
        overlayRoot = null
        pillLayout = null
        textView = null
        windowParams = null
    }

    private fun calculateTopPosition(): Int {
        val statusBarHeight = getStatusBarHeight()
        return maxOf(statusBarHeight + dpToPx(6f).toInt(), dpToPx(44f).toInt())
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            dpToPx(28f).toInt()
        }
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }
}
