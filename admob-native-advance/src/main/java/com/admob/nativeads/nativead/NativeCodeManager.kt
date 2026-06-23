package com.admob.nativeads.nativead

import android.app.Activity
import com.admob.nativeads.utils.log
import com.unity3d.player.UnityPlayer

object NativeCodeManager {
    private const val TAG = "com.admob.nativeads.nativead.NativeCodeManager"

    private val nativeAdMap = HashMap<String, SimpleNativeAdvance?>()

    @JvmStatic
    fun initialize() {
        // Reserved for future initialization logic
    }

    @JvmStatic
    @JvmOverloads
    fun registerNativeAd(
        adKey: String,
        adID: String,
        adName: String,
        layout: String,
        isShowCountDown: Boolean = false,
        countDownTotalSecond: Int = 0
    ) {
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
    fun loadNativeAd(
        testMode: Boolean,
        testActivity: Activity?,
        adKey: String,
        callback: INativeAdCallback?
    ) {
        val functionTag = "$adKey - loadNativeAd"
        log(TAG, "$functionTag - Preparing to load Native Ad with Ad Key: $adKey")

        if (!nativeAdMap.containsKey(adKey)) {
            log(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }

        val activity = if (testMode) testActivity else UnityPlayer.currentActivity
        activity?.runOnUiThread {
            log(TAG, "$adKey - Loading Native Ad with Ad Key: $adKey")
            nativeAdMap[adKey]?.loadNativeAd(activity, callback)
        }
        log(TAG, "$functionTag - call load ad for Ad Key: $adKey")
    }

    @JvmStatic
    fun showNativeAd(
        testMode: Boolean,
        testActivity: Activity?,
        adKey: String,
        width: Int, height: Int,
        x: Int, y: Int,
        callback: INativeAdCallback?
    ) {
        val functionTag = "$adKey - showNativeAd"
        if (!nativeAdMap.containsKey(adKey)) {
            log(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }

        log(TAG, "$functionTag - Showing Native Ad at position ($x, $y) with size ($width x $height)")
        val activity = if (testMode) testActivity else UnityPlayer.currentActivity
        activity?.runOnUiThread {
            val nativeAd = nativeAdMap[adKey]
            val nativeAdView = nativeAd?.nativeAdView
            if (nativeAdView == null) {
                log(TAG, "$functionTag - Native Ad View is null for Ad Key: $adKey. Cannot show ad.")
                return@runOnUiThread
            }

            nativeAd.attachToWindowManager(activity, width, height, x, y)
            nativeAd.startCountdown(callback)

            log(TAG, "$functionTag - Native Ad displayed on screen.")
        }
    }

    @JvmStatic
    fun hideNativeAd(
        testMode: Boolean,
        testActivity: Activity?,
        adKey: String
    ) {
        val functionTag = "$adKey - hideNativeAd"
        if (!nativeAdMap.containsKey(adKey)) {
            log(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }

        log(TAG, "$functionTag - Hiding Native Ad")
        val activity = if (testMode) testActivity else UnityPlayer.currentActivity
        activity?.runOnUiThread {
            nativeAdMap[adKey]?.removeOverlay()
        }
        log(TAG, "$functionTag - Native Ad hidden from view.")
    }

    @JvmStatic
    fun destroyNativeAd(
        testMode: Boolean,
        testActivity: Activity?,
        adKey: String
    ) {
        val functionTag = "$adKey - destroyNativeAd"
        if (!nativeAdMap.containsKey(adKey)) {
            log(TAG, "$functionTag - Native Ad Key not registered: $adKey. Please register before loading.")
            return
        }

        val activity = if (testMode) testActivity else UnityPlayer.currentActivity
        activity?.runOnUiThread {
            nativeAdMap[adKey]?.removeOverlay()
            nativeAdMap[adKey]?.destroyNativeAd()
        }
        log(TAG, "$functionTag - Native Ad destroyed and resources cleaned up.")
    }

    // Overloaded convenience methods for Unity C# (avoids passing false, null on every call)
    @JvmStatic
    fun loadNativeAd(adKey: String, callback: INativeAdCallback?) {
        loadNativeAd(false, null, adKey, callback)
    }

    @JvmStatic
    fun showNativeAd(adKey: String, width: Int, height: Int, x: Int, y: Int, callback: INativeAdCallback?) {
        showNativeAd(false, null, adKey, width, height, x, y, callback)
    }

    @JvmStatic
    fun hideNativeAd(adKey: String) {
        hideNativeAd(false, null, adKey)
    }

    @JvmStatic
    fun destroyNativeAd(adKey: String) {
        destroyNativeAd(false, null, adKey)
    }
}