package com.s.g.ana.native

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
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
import com.s.g.ana.R
import com.s.g.ana.utils.log

class SimpleNativeAdvance(
    private val adId: String,
    private val adName: String,
    private val layoutKey: String,
    isShowCountDown: Boolean? = false,
    countDownTotalSecond: Int? = 5,
    //isShowCloseButton: Boolean? = false,
    //showCloseButtonAfter: Int? = 0
) {

    private val logTag = "com.s.g.ana.native.SimpleNativeAdvance"

    //countdown and close button setting
    //if true: show count down second before show button next or button close
    //if false: show button next or button close immediately
    private var isShowCountDown: Boolean = isShowCountDown ?: false
    //count down total second
    private var countDownTotalSecond: Int = countDownTotalSecond ?: 0

    //for ad
    private var nativeAd: NativeAd? = null
    var nativeAdView: NativeAdView? = null

    //for countdown and close button
    private var countDownTimer: CountDownTimer? = null
    private var closeButton: TextView? = null
    private var skipLabel: TextView? = null

    // Lưu lại activity để dùng trong populateAdView
    //20260306 anh comment out
    //private var currentActivity: Activity? = null

    // THÊM MỚI: container dùng để gắn ad vào WindowManager thay vì addContentView()
    // WindowManager.TYPE_APPLICATION_PANEL đè lên Unity GL surface → video trong MediaView render được


    // init block có thể dùng để log or xử lý khởi tạo ban đầu
    init {
        log(logTag, "$adName - Initialized Ad:(ID: $adId, Layout: $layoutKey)")
    }

    fun loadNativeAd(activity: Activity, callback: INativeAdCallback?) {
        activity.runOnUiThread {
            log(logTag, "$adName - Loading Native Ad")
            val videoOptions = VideoOptions.Builder().setStartMuted(false).build()
            val nativeAdOptions = NativeAdOptions.Builder().setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE).setVideoOptions(videoOptions).build()
            val adLoader = AdLoader.Builder(activity, adId)
                .forNativeAd { ad: NativeAd ->
                    // Lưu trữ quảng cáo sau khi load xong
                    nativeAd = ad
                    log(logTag, "$adName - Ad loaded success: headline ${ad.headline}")
                    // 2. THÊM EVENT TÍNH TIỀN (Paid Event) TẠI ĐÂY
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
                        log( logTag, "$adName - Ad loaded successfully.")
                        callback?.onAdLoaded()
                        //anh 20260306 comment out
                        //currentActivity = activity
                        //create native ad view
                        createNativeAdView(activity)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        log(logTag, "$adName - Ad failed to load: ${error.message}")
                        callback?.onAdFailed(error.message)
                    }
                    override fun onAdOpened(){
                        //when display overlay screen
                        //log(logTag, "Ad opened.")
                        callback?.onAdOpened()
                    }

                    override fun onAdClicked(){
                        //log(logTag, "Ad clicked.")
                        callback?.onAdClicked()
                    }

                    override fun onAdClosed(){
                        //log(logTag, "Ad closed.")
                        callback?.onAdClosed()
                    }

                    override fun onAdImpression(){
                        var adSourceID ="0"
                        if (nativeAd != null) {
                            adSourceID = nativeAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId ?: "0"
                        }
                        log(logTag, "onAdImpression - Ad impression recorded with Ad Source ID: $adSourceID")
                        callback?.onAdImpression(adSourceID)
                    }

                    override  fun onAdSwipeGestureClicked(){
                        //log(logTag, "Ad swipe gesture clicked.")
                        callback?.onAdSwipeGestureClicked()
                    }
                })
                //original
                //.withNativeAdOptions(NativeAdOptions.Builder().build())
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
            //get native layout from xml and populate data
            val layoutFileName = NativeLayoutConfig.fromKey(layoutKey)?.layout ?: NativeLayoutConfig.NativeDefault.layout

            // 1. Khởi tạo Layout từ XML (đây là cách vẽ Ad vào Layout)
            // Lưu ý: Bạn cần file native_ad_layout.xml trong Plugins/Android/res/layout
            val inflater = LayoutInflater.from(activity)
            val adView = inflater.inflate(
                activity.resources.getIdentifier(
                    layoutFileName,
                    "layout",
                    activity.packageName),
                null
            ) as NativeAdView

            // 2. Logic "Vẽ" dữ liệu vào Layout (Populate)
            // Giả sử adData chứa thông tin quảng cáo, bạn cast nó và gán vào các TextView, ImageView
            nativeAd?.let { populateAdView(it, adView) }

            //setup countdown button if needed
            // ========== CHỈ SETUP BUTTON, CHƯA START TIMER ==========
            setupCountdownButton(adView)

            // 3. Trả về adView cho C# dưới dạng một đối tượng View
            // Mặc dù trả về cho C#, nhưng View này vẫn "sống" trong bộ nhớ Android
            nativeAdView = adView
        }catch ( e: Exception) {
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

        //  FIX: gán cả mediaView lẫn mediaContent ( trước đây chỉ gán mediaView)
        mediaView?.let {
            adView.mediaView = it
            it.mediaContent = nativeAd.mediaContent  // bind mediaContent vào mediaView
        }

        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent?.videoController

        if (nativeAd.mediaContent != null && nativeAd.mediaContent?.hasVideoContent() == true) {
            log(logTag, "$adName - Ad has video content")

            //  THÊM: đầy đủ tất cả video lifecycle callbacks
            vc?.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {

                override fun onVideoEnd() {
                    super.onVideoEnd()
                    log(logTag, "$adName - onVideoEnd")
                    onNativeMediaVideoEnd()
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

            // THÊM: tự động play nếu custom controls được bật
            if (vc?.isCustomControlsEnabled == true) {
                vc.play()
            }
        } else {
            log(logTag, "$adName - Ad has no video content")
            // THÊM: gọi callback không có video giống class 2
            onCheckNativeNotMediaVideo()
        }
    }

    /**
     *  THÊM: callback khi native không có video (override trong subclass nếu cần)
     */
    private fun onCheckNativeNotMediaVideo() {
        //call callback to C#
        return
    }

    /**
     *  THÊM: callback khi video kết thúc
     */
    private fun onNativeMediaVideoEnd() {
        //call callback to C#
        return
    }

    /**
     * THÊM: reload lại native ad (ví dụ sau khi video kết thúc) => Đã confilm với chị Yến là ko cần
     */
    //anh 20260306 comment out
//    fun refreshAdNative() {
//        currentActivity?.let { activity ->
//            if (!activity.isDestroyed && !activity.isFinishing) {
//                log(logTag, "$adName - Refreshing native ad")
//                loadNativeAd(activity, null)
//            }
//        }
//    }

    fun destroyNativeAd() {
        countDownTimer?.cancel()
        countDownTimer = null
        nativeAd?.destroy()
        nativeAdView?.destroy()
        nativeAd = null
        nativeAdView = null
        closeButton = null
        //anh 20260306 comment out
        //currentActivity = null
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

        // Cấu hình trạng thái ban đầu khi đang đếm ngược
        closeButton.visibility = View.GONE
        skipLabel.text = "Skip in ${countDownTotalSecond}s "
        closeButton.text = "Skip"

        // Cả container chứa nút sẽ chưa thể click khi đang đếm ngược
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

        // Reset trạng thái ban đầu trước khi chạy timer
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
                // Cập nhật số giây giảm dần vào skip_label
                skipLabel?.text = "Skip in ${secondsLeft}s "
                log(logTag, "$adName - Countdown: $secondsLeft")
            }

            override fun onFinish() {
                log(logTag, "$adName - Countdown finished")

                // Khi đếm ngược xong:
                // Cách 1 (Giống ảnh): Ẩn hẳn chữ "Skip in 0s", chỉ để lại chữ "Skip" màu cam căn giữa
                skipLabel?.visibility = View.GONE
                closeButton?.visibility = View.VISIBLE
                closeButton?.text = "Skip" // Hoặc đổi thành "✕" nếu muốn thành nút tắt

                // Kích hoạt tính năng click cho cả vùng nút Capsule
                closeContainer?.isClickable = true
                closeContainer?.isEnabled = true
                closeButton?.isClickable = true

                callback?.onCountdownFinished()

                // Lắng nghe sự kiện click để đóng quảng cáo
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

    fun checkTopOverlap(): Boolean {
        return isShowCountDown
    }

    // =========================================================================
    // THÊM MỚI: các hàm hỗ trợ WindowManager
    // Lý do: addContentView() bị Unity GL surface che → video không hiển thị được.
    // WindowManager.TYPE_APPLICATION_PANEL đè lên tất cả kể cả Unity GL → video render bình thường.
    // Các hàm này được gọi từ NativeCodeManager thay cho addContentView()/removeView() cũ.
    // =========================================================================



    /**
     * THÊM MỚI: ẩn overlay tạm thời (không destroy ad).
     * Được gọi từ NativeCodeManager.hideNativeAd() thay cho removeView() cũ.
     * Dùng visibility = GONE để giữ overlay trong WindowManager, show lại nhanh hơn.
     */
    fun detachFromWindowManager(activity: Activity) {
        overlayContainer?.visibility = View.GONE
        log(logTag, "$adName - overlay hidden")
    }


    private fun buildWindowParams(
        activity: Activity,
        x: Int, y: Int,
        width: Int, height: Int,
        isOnTop: Boolean
    ): WindowManager.LayoutParams {

        // Quyết định Window Type dựa trên tính chất Ad
        // TYPE_APPLICATION_ATTACHED_DIALOG luôn có Z-Index cao hơn TYPE_APPLICATION_PANEL
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
            // Cực kỳ quan trọng cho cả 2 loại khi chạy chung với Unity
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

    /**
     * Cập nhật vị trí/kích thước overlay đang hiển thị.
     */
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

    // =========================================================================
    // THÊM MỚI: các hàm hỗ trợ WindowManager
    // Lý do: addContentView() bị Unity GL surface che → video không hiển thị được.
    // WindowManager.TYPE_APPLICATION_PANEL đè lên tất cả kể cả Unity GL → video render bình thường.
    // =========================================================================

    companion object {
        // Lưu lại instance của native thường đang hiển thị
        private var activeNormalAd: SimpleNativeAdvance? = null
        // Lưu lại instance của native on top đang hiển thị
        private var activeOnTopAd: SimpleNativeAdvance? = null
    }

    private var overlayContainer: FrameLayout? = null

    /**
     * NATIVE THƯỜNG
     */
    fun attachToWindowManager(activity: Activity, width: Int, height: Int, x: Int, y: Int) {
        val adView = nativeAdView ?: return

        // Ghi nhận đây là Ad thường đang hiển thị
        activeNormalAd = this

        // NẾU ĐANG CÓ AD ON TOP HIỂN THỊ: Ẩn ad thường này ngay từ đầu để tránh đè lên On Top
        if (activeOnTopAd != null) {
            log(logTag, "$adName - An OnTop Ad is displaying. Initializing Normal Ad as GONE.")
        }

        overlayContainer?.let { existing ->
            // Nếu có On Top thì giữ GONE, không thì VISIBLE
            existing.visibility = if (activeOnTopAd != null) View.GONE else View.VISIBLE
            updateWindowLayout(activity, existing, x, y, width, height, false)
            return
        }

        val container = FrameLayout(activity)
        overlayContainer = container

        // Nếu có On Top đang hiển thị thì tạm thời ẩn container này đi luôn
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
     * NATIVE ON TOP - ĐẢM BẢO 100% TRÊN CÙNG
     */
    fun attachToWindowManagerOnTop(activity: Activity, width: Int, height: Int, x: Int, y: Int) {
        val adView = nativeAdView ?: return

        // Ghi nhận đây là Ad On Top đang hiển thị
        activeOnTopAd = this

        // 100% TRICK: Nếu có Ad thường đang chạy, cưỡng ép ẨN nó đi luôn để không bao giờ có chuyện tranh chấp layer
        activeNormalAd?.overlayContainer?.visibility = View.GONE
        log(logTag, "$adName - Cưỡng ép ẩn Native Ad thường để On Top hiển thị.")

        overlayContainer?.let { existing ->
            existing.visibility = View.VISIBLE
            updateWindowLayout(activity, existing, x, y, width, height, true)
            // Ép WindowManager vẽ lại lên trên cùng
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
     * Cập nhật lại hàm xóa Overlay để tự động khôi phục Ad thường (nếu có)
     */
    fun removeOverlay() {
        val container = overlayContainer ?: return
        val wm = container.context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        try {
            wm?.removeViewImmediate(container)
        } catch (e: Exception) {
            try { wm?.removeView(container) } catch (_: Exception) {}
        }

        // Nếu cái bị xóa là Ad On Top -> Giải phóng biến và hiện lại Ad thường (nếu có)
        if (this == activeOnTopAd) {
            activeOnTopAd = null
            activeNormalAd?.overlayContainer?.let { normalContainer ->
                normalContainer.visibility = View.VISIBLE
                log(logTag, "On Top đã đóng. Tự động hiển thị lại Native Ad thường.")
            }
        }

        if (this == activeNormalAd) {
            activeNormalAd = null
        }

        overlayContainer = null
    }
}