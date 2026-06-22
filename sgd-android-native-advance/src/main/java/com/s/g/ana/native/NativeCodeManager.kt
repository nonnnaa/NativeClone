package com.s.g.ana.native

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.s.g.ana.utils.log
import com.unity3d.player.UnityPlayer
import kotlin.text.get

object NativeCodeManager {
    private const val TAG = "com.s.g.ana.native.NativeCodeManager"
    // Sử dụng String làm Key (ví dụ: "splash", "intro", "break")
    private val nativeAdMap = HashMap<String, SimpleNativeAdvance?>()

    @JvmStatic
    fun initialize() {

    }

    @JvmStatic
    fun registerNativeAd(adKey: String,
                         adID: String,
                         adName: String,
                         layout: String,
                         isShowCountDown: Boolean = false,
                         countDownTotalSecond: Int = 0) {
        val functionTag = "$adKey - registerNativeAd"
        log(TAG, "$functionTag - Registering Native Ad with Ad Key: $adKey")
        if (nativeAdMap.containsKey(adKey)) {
            log(TAG, "$functionTag - Native Ad Key already registered: $adKey, resetting instance.")
            nativeAdMap[adKey]?.destroyNativeAd()
            nativeAdMap.remove(adKey)
        }
        nativeAdMap[adKey] = SimpleNativeAdvance(adID, adName, layout, isShowCountDown, countDownTotalSecond)
        log(TAG, "$functionTag - Registered Native Ad Key: $adKey")
    }

    @JvmStatic
    fun loadNativeAd(testMode: Boolean,
                     testActivity: Activity?,
                     adKey: String,
                     callback: INativeAdCallback) {
        val functionTag = "$adKey - loadNativeAd"
        log(TAG, "$functionTag - Preparing to load Native Ad with Ad Key: $adKey")

        if (!nativeAdMap.containsKey(adKey)) {
            log(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }
        val activity = if (testMode) testActivity else com.unity3d.player.UnityPlayer.currentActivity
        activity?.runOnUiThread {
            log(TAG, "$adKey - Loading Native Ad with Ad Key: $adKey")
            val nativeAd = nativeAdMap[adKey]
            nativeAd?.loadNativeAd(activity, callback)
        }
        log(TAG, "$functionTag - call load ad for Ad Key: $adKey")
    }

    @JvmStatic
    fun showNativeAd(testMode: Boolean,
                     testActivity: Activity?,
                     adKey: String,
                     width: Int, height: Int,
                     x:Int, y: Int,
                     callback: INativeAdCallback) {
        val functionTag = "$adKey - showNativeAd"
        if (!nativeAdMap.containsKey(adKey)) {
            Log.e(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }
        log(TAG, "$functionTag - Showing Native Ad at position ($x, $y) with size ($width x $height)")
        val activity =
            if (testMode) testActivity else com.unity3d.player.UnityPlayer.currentActivity
        activity?.runOnUiThread {
            // 1. Lấy NativeAdView từ Manager của bạn
            val nativeAd = nativeAdMap[adKey]
            val checkTopOverlap = nativeAdMap[adKey]?.checkTopOverlap()
            val nativeAdView = nativeAd?.nativeAdView
            if (nativeAdView == null) {
                Log.e(TAG, "$functionTag - Native Ad View is null for Ad Key: $adKey. Cannot show ad.")
                return@runOnUiThread
            }

            // SỬA: trước đây dùng addContentView() + FrameLayout.LayoutParams để gắn ad vào Activity
            // addContentView() bị Unity GL surface che → video trong MediaView không render được
            // Nay dùng attachToWindowManager() → WindowManager.TYPE_APPLICATION_PANEL đè lên Unity GL surface
            // → video render bình thường, các tham số x, y, width, height giữ nguyên không đổi
            nativeAd.attachToWindowManager(activity, width, height, x, y)

            // ========== START COUNTDOWN KHI SHOW ==========
            nativeAd.startCountdown(callback)

            log(TAG, "$functionTag - Native Ad displayed on screen.")
        }
    }

    @JvmStatic
    fun showNativeAdOnTop(
        testMode: Boolean,
        testActivity: Activity?,
        adKey: String,
        width: Int, height: Int,
        x: Int, y: Int,
        callback: INativeAdCallback?
    ) {
        val functionTag = "$adKey - showNativeAdOnTop"
        Log.e(TAG, "$functionTag - CALLED width=$width height=$height x=$x y=$y") // dùng Log.e để dễ thấy
        try {
            if (!nativeAdMap.containsKey(adKey)) {
                Log.e(TAG, "$functionTag - key not found in map. Map keys: ${nativeAdMap.keys}")
                return
            }
            val activity = if (testMode) testActivity else UnityPlayer.currentActivity
            Log.e(TAG, "$functionTag - activity: $activity")
            activity?.runOnUiThread {
                try {
                    val nativeAd = nativeAdMap[adKey]
                    Log.e(TAG, "$functionTag - nativeAd: $nativeAd, nativeAdView: ${nativeAd?.nativeAdView}")
                    nativeAd?.attachToWindowManagerOnTop(activity, width, height, x, y)
                    nativeAd?.startCountdown(callback)
                } catch (e: Exception) {
                    Log.e(TAG, "$functionTag - runOnUiThread error: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "$functionTag - outer error: ${e.message}", e)
        }
    }
    @JvmStatic
    fun hideNativeAd(testMode: Boolean,
                     testActivity: Activity?,
                     adKey: String) {
        val functionTag = "$adKey - hideNativeAd"
        if (!nativeAdMap.containsKey(adKey)) {
            Log.e(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }
        log(TAG, "$functionTag - Hiding Native Ad")
        val activity =
            if (testMode) testActivity else com.unity3d.player.UnityPlayer.currentActivity
        activity?.runOnUiThread {
            val nativeAdView = nativeAdMap[adKey]?.nativeAdView
            (nativeAdView?.parent as? ViewGroup)?.removeView(nativeAdView)
            //  dùng removeOverlay() để remove view khỏi WindowManager, tránh trường hợp view vẫn còn đó nhưng không thấy (invisible) dẫn đến việc không click được vào vùng đấy
            nativeAdMap[adKey]?.removeOverlay()
        }
        log(TAG, "$functionTag - Native Ad hidden from view.")
    }

    @JvmStatic
    fun destroyNativeAd(testMode: Boolean,
                        testActivity: Activity?,
                        adKey: String) {
        val functionTag = "$adKey - destroyNativeAd"
        if (!nativeAdMap.containsKey(adKey)) {
            Log.e(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }
        // Logic to destroy or clean up native ad resources
        val activity =
            if (testMode) testActivity else com.unity3d.player.UnityPlayer.currentActivity
        activity?.runOnUiThread {
            // THÊM: gọi removeOverlay() trước để xoá container khỏi WindowManager trước khi destroy ad
            nativeAdMap[adKey]?.removeOverlay()
            nativeAdMap[adKey]?.destroyNativeAd()
            //nativeAdMap[adKey]=null
        }
        log(TAG, "$functionTag - Native Ad destroyed and resources cleaned up.")
    }


}