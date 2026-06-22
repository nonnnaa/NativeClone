package com.admob.nativeads.nativead

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.admob.nativeads.R
import com.admob.nativeads.utils.log

class SimpleNativeAdvance(
    private val adId: String,
    private val adName: String,
    private val layoutKey: String,
    isShowCountDown: Boolean? = false,
    countDownTotalSecond: Int? = 5
) {

    private val logTag = "com.admob.nativeads.nativead.SimpleNativeAdvance"

    // Countdown settings
    private var isShowCountDown: Boolean = isShowCountDown ?: false
    private var countDownTotalSecond: Int = countDownTotalSecond ?: 0

    // Ad references
    private var nativeAd: NativeAd? = null
    var nativeAdView: NativeAdView? = null

    // Countdown and close button
    private var countDownTimer: CountDownTimer? = null
    private var closeButton: TextView? = null
    private var skipLabel: TextView? = null

    init {
        log(logTag, "$adName - Initialized Ad:(ID: $adId, Layout: $layoutKey)")
    }

    fun loadNativeAd(activity: Activity, callback: INativeAdCallback?) {
        activity.runOnUiThread {
            log(logTag, "$adName - Loading Native Ad")
            val videoOptions = VideoOptions.Builder().setStartMuted(false).build()
            val nativeAdOptions = NativeAdOptions.Builder()
                .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                .setVideoOptions(videoOptions)
                .build()

            val adLoader = AdLoader.Builder(activity, adId)
                .forNativeAd { ad: NativeAd ->
                    nativeAd = ad
                    log(logTag, "$adName - Ad loaded success: headline ${ad.headline}")

                    // Paid event listener for revenue tracking
                    ad.setOnPaidEventListener { adValue ->
                        val microsValue = adValue.valueMicros
                        val currencyCode = adValue.currencyCode
                        val precision = adValue.precisionType
                        val adNetwork = ad.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "Unknown_Network"

                        log(logTag, "$adName - OnPaidEvent: " +
                                "value $microsValue, " +
                                "currency $currencyCode, " +
                                "precision $precision")
                        callback?.onAdRevenuePaid(microsValue, currencyCode, precision, adNetwork)
                    }
                }
                .withAdListener(object: AdListener() {
                    override fun onAdLoaded() {
                        log(logTag, "$adName - Ad loaded successfully.")
                        callback?.onAdLoaded()
                        createNativeAdView(activity)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        log(logTag, "$adName - Ad failed to load: ${error.message}")
                        callback?.onAdFailed(error.message)
                    }

                    override fun onAdOpened() {
                        callback?.onAdOpened()
                    }

                    override fun onAdClicked() {
                        callback?.onAdClicked()
                    }

                    override fun onAdClosed() {
                        callback?.onAdClosed()
                    }

                    override fun onAdImpression() {
                        var adSourceID = "0"
                        if (nativeAd != null) {
                            adSourceID = nativeAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId ?: "0"
                        }
                        log(logTag, "onAdImpression - Ad impression recorded with Ad Source ID: $adSourceID")
                        callback?.onAdImpression(adSourceID)
                    }

                    override fun onAdSwipeGestureClicked() {
                        callback?.onAdSwipeGestureClicked()
                    }
                })
                .withNativeAdOptions(nativeAdOptions)
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    private fun createNativeAdView(activity: Activity) {
        try {
            if (nativeAd == null) {
                log(logTag, "$adName - Native ad data is null, cannot create ad view.")
                return
            }

            val layoutFileName = NativeLayoutConfig.fromKey(layoutKey)?.layout ?: NativeLayoutConfig.NativeCustom1.layout

            val inflater = LayoutInflater.from(activity)
            val adView = inflater.inflate(
                activity.resources.getIdentifier(
                    layoutFileName,
                    "layout",
                    activity.packageName),
                null
            ) as NativeAdView

            nativeAd?.let { populateAdView(it, adView) }

            // Setup countdown button UI (timer starts later when ad is shown)
            setupCountdownButton(adView)

            nativeAdView = adView
        } catch (e: Exception) {
            log(logTag, "$adName - Error creating native ad view: ${e.message}")
        }
    }

    private fun populateAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<View>(R.id.ad_call_to_action)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val mediaView =
            adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(R.id.ad_media)

        headlineView?.let {
            it.text = nativeAd.headline
            adView.headlineView = it
        }
        bodyView?.let {
            if (nativeAd.body == null) {
                it.visibility = View.INVISIBLE
            } else {
                it.visibility = View.VISIBLE
                it.text = nativeAd.body
                adView.bodyView = it
            }
        }
        callToActionView?.let {
            if (nativeAd.callToAction == null) {
                it.visibility = View.INVISIBLE
            } else {
                it.visibility = View.VISIBLE
                (it as? TextView)?.text = nativeAd.callToAction
                adView.callToActionView = it
            }
        }
        iconView?.let {
            if (nativeAd.icon == null) {
                it.visibility = View.GONE
            } else {
                it.setImageDrawable(nativeAd.icon?.drawable)
                it.visibility = View.VISIBLE
                adView.iconView = it
            }
        }

        mediaView?.let {
            adView.mediaView = it
            it.mediaContent = nativeAd.mediaContent
        }

        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent?.videoController

        if (nativeAd.mediaContent != null && nativeAd.mediaContent?.hasVideoContent() == true) {
            log(logTag, "$adName - Ad has video content")

            vc?.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    super.onVideoEnd()
                    log(logTag, "$adName - onVideoEnd")
                }

                override fun onVideoStart() {
                    super.onVideoStart()
                    log(logTag, "$adName - onVideoStart")
                }

                override fun onVideoPlay() {
                    super.onVideoPlay()
                    log(logTag, "$adName - onVideoPlay")
                }

                override fun onVideoPause() {
                    super.onVideoPause()
                    log(logTag, "$adName - onVideoPause")
                }

                override fun onVideoMute(isMuted: Boolean) {
                    super.onVideoMute(isMuted)
                    log(logTag, "$adName - onVideoMute: $isMuted")
                }
            }

            // Auto-play if custom controls are enabled
            if (vc?.isCustomControlsEnabled == true) {
                vc.play()
            }
        } else {
            log(logTag, "$adName - Ad has no video content")
        }
    }

    fun destroyNativeAd() {
        countDownTimer?.cancel()
        countDownTimer = null
        nativeAd?.destroy()
        nativeAdView?.destroy()
        nativeAd = null
        nativeAdView = null
        closeButton = null
        log(logTag, "$adName - destroyed.")
    }

    private fun setupCountdownButton(parentView: View) {
        log(logTag, "$adName - Setting up countdown button")
        if (!isShowCountDown) {
            log(logTag, "$adName - No countdown or close button to setup")
            return
        }

        val closeButton = parentView.findViewById<TextView>(R.id.btn_close_countdown)
        val skipLabel = parentView.findViewById<TextView>(R.id.skip_label)
        val closeContainer = parentView.findViewById<View>(R.id.close_container)

        if (closeButton == null || skipLabel == null) {
            log(logTag, "$adName - Missing views for countdown in layout")
            return
        }

        this.closeButton = closeButton
        this.skipLabel = skipLabel

        // Initial state: hide close button, show countdown label
        closeButton.visibility = View.GONE
        skipLabel.text = "Skip in ${countDownTotalSecond}s "
        closeButton.text = "Skip"

        closeContainer?.isClickable = false
        closeContainer?.isEnabled = false
        this.closeButton?.isClickable = false

        log(logTag, "$adName - Countdown button setup ready")
    }

    fun startCountdown(callback: INativeAdCallback?) {
        log(logTag, "$adName - Starting startCountdown function for countdown")

        if (!isShowCountDown) {
            log(logTag, "$adName - No countdown or close button to start")
            return
        }

        if (nativeAdView == null) {
            log(logTag, "$adName - NativeAdView is null, cannot start countdown")
            return
        }

        if (closeButton == null || skipLabel == null) {
            log(logTag, "$adName - No countdown views in layout, skipping countdown")
            return
        }

        countDownTimer?.cancel()

        // Reset state before starting timer
        skipLabel?.text = "Skip in ${countDownTotalSecond}s "
        skipLabel?.visibility = View.VISIBLE
        closeButton?.text = "Skip"

        val closeContainer = nativeAdView?.findViewById<View>(R.id.close_container)
        closeContainer?.isClickable = false
        closeContainer?.isEnabled = false

        countDownTimer = object : CountDownTimer(
            (countDownTotalSecond * 1000).toLong(),
            1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                skipLabel?.text = "Skip in ${secondsLeft}s "
                log(logTag, "$adName - Countdown: $secondsLeft")
            }

            override fun onFinish() {
                log(logTag, "$adName - Countdown finished")

                // Show skip button, hide countdown label
                skipLabel?.visibility = View.GONE
                closeButton?.visibility = View.VISIBLE
                closeButton?.text = "Skip"

                // Enable click for close button area
                closeContainer?.isClickable = true
                closeContainer?.isEnabled = true
                closeButton?.isClickable = true

                callback?.onCountdownFinished()

                closeContainer?.setOnClickListener {
                    log(logTag, "$adName - Close button container clicked")
                    removeOverlay()
                    callback?.onCloseButtonClicked()
                }

                closeButton?.setOnClickListener {
                    log(logTag, "$adName - Close button text clicked")
                    removeOverlay()
                    callback?.onCloseButtonClicked()
                }
            }
        }.start()
        log(logTag, "$adName - Countdown started: $countDownTotalSecond seconds")
    }

    // =========================================================================
    // WindowManager overlay functions
    // Uses WindowManager instead of addContentView() to ensure ad renders
    // above Unity GL surface (required for video in MediaView).
    // =========================================================================

    companion object {
        // Track active normal ad instance
        private var activeNormalAd: SimpleNativeAdvance? = null
        // Track active on-top ad instance
        private var activeOnTopAd: SimpleNativeAdvance? = null
    }

    private var overlayContainer: FrameLayout? = null

    private fun buildWindowParams(
        activity: Activity,
        x: Int, y: Int,
        width: Int, height: Int,
        isOnTop: Boolean
    ): WindowManager.LayoutParams {

        // TYPE_APPLICATION_ATTACHED_DIALOG has higher Z-index than TYPE_APPLICATION_PANEL
        val windowType = if (isOnTop) {
            WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        }

        return WindowManager.LayoutParams(
            width,
            height,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            token = activity.window.decorView.windowToken
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
    }

    private fun updateWindowLayout(
        activity: Activity,
        container: FrameLayout,
        x: Int, y: Int,
        width: Int, height: Int,
        isOnTop: Boolean
    ) {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            wm.updateViewLayout(container, buildWindowParams(activity, x, y, width, height, isOnTop))
        } catch (e: Exception) {
            log(logTag, "$adName - updateWindowLayout error: ${e.message}")
        }
    }

    /**
     * Attach normal native ad to WindowManager.
     * If an OnTop ad is active, this ad will be hidden until OnTop is removed.
     */
    fun attachToWindowManager(activity: Activity, width: Int, height: Int, x: Int, y: Int) {
        val adView = nativeAdView ?: return

        activeNormalAd = this

        if (activeOnTopAd != null) {
            log(logTag, "$adName - An OnTop Ad is displaying. Initializing Normal Ad as GONE.")
        }

        overlayContainer?.let { existing ->
            existing.visibility = if (activeOnTopAd != null) View.GONE else View.VISIBLE
            updateWindowLayout(activity, existing, x, y, width, height, false)
            return
        }

        val container = FrameLayout(activity)
        overlayContainer = container

        if (activeOnTopAd != null) {
            container.visibility = View.GONE
        }

        (adView.parent as? ViewGroup)?.removeView(adView)
        container.addView(adView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        try {
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.addView(container, buildWindowParams(activity, x, y, width, height, false))
        } catch (e: Exception) {
            overlayContainer = null
        }
    }

    /**
     * Attach native ad OnTop - guaranteed to display above all other layers.
     * Forcefully hides any active normal ad to prevent layer conflicts.
     */
    fun attachToWindowManagerOnTop(activity: Activity, width: Int, height: Int, x: Int, y: Int) {
        val adView = nativeAdView ?: return

        activeOnTopAd = this

        // Force hide normal ad to prevent layer conflicts
        activeNormalAd?.overlayContainer?.visibility = View.GONE
        log(logTag, "$adName - Force hiding Normal Ad for OnTop display.")

        overlayContainer?.let { existing ->
            existing.visibility = View.VISIBLE
            updateWindowLayout(activity, existing, x, y, width, height, true)
            // Re-add to WindowManager to force top Z-order
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            try {
                wm.removeViewImmediate(existing)
                wm.addView(existing, buildWindowParams(activity, x, y, width, height, true))
            } catch (_: Exception) {}
            return
        }

        val container = FrameLayout(activity)
        overlayContainer = container

        (adView.parent as? ViewGroup)?.removeView(adView)
        container.addView(adView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        try {
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.addView(container, buildWindowParams(activity, x, y, width, height, true))
        } catch (e: Exception) {
            overlayContainer = null
        }
    }

    /**
     * Remove overlay from WindowManager.
     * If removing an OnTop ad, automatically restores the normal ad (if any).
     */
    fun removeOverlay() {
        val container = overlayContainer ?: return
        val wm = container.context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        try {
            wm?.removeViewImmediate(container)
        } catch (e: Exception) {
            try { wm?.removeView(container) } catch (_: Exception) {}
        }

        // If removing OnTop ad, restore normal ad visibility
        if (this == activeOnTopAd) {
            activeOnTopAd = null
            activeNormalAd?.overlayContainer?.let { normalContainer ->
                normalContainer.visibility = View.VISIBLE
                log(logTag, "OnTop closed. Restoring Normal Ad visibility.")
            }
        }

        if (this == activeNormalAd) {
            activeNormalAd = null
        }

        overlayContainer = null
    }
}