package com.s.g.sample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.s.g.ana.native.INativeAdCallback
import com.s.g.ana.test.TestUnityNative
import com.s.g.ana.native.NativeCodeManager
import com.s.g.ana.utils.log


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //tag log
        val logTag = "android_native_advance"

        // Tìm TextView bằng ID
        val textView = findViewById<TextView>(R.id.textView)

        // Gán giá trị xxx
        textView.text = TestUnityNative.getHello()
        log("android_native_advance", "test log")

        //init native advance
        NativeCodeManager.initialize()

        //init register native ad
        NativeCodeManager.registerNativeAd(
            adKey = "native_test",
            adID = "ca-app-pub-3940256099942544/2247696110", //test ad id
            adName = "google_ad_mob",
            layout = "l_full1",
            isShowCountDown = true,
            countDownTotalSecond = 5
        )

        //test overlay
//        val btnTestView = findViewById<TextView>(R.id.btnTestOverlay)
//        btnTestView.setOnClickListener {
//            Log.v("android_native_advance", "Test Overlay View button clicked")
//            NativeViewFactory.createView(
//                testMode = true,
//                testActivity = this,
//                0, 400, 400,
//                100, 200,
//                200, 300
//            )
//        }
        //remove
//        val btnRemoveView = findViewById<TextView>(R.id.btnRemoveOverlay)
//        btnRemoveView.setOnClickListener {
//            Log.v("android_native_advance", "Remove Overlay View button clicked")
//            NativeViewFactory.removeAllViews(
//                testMode = true,
//                testActivity = this
//            )
//        }

        val callback: INativeAdCallback = object : INativeAdCallback {
            override fun onAdLoaded() {
                log(logTag, "Native Ad Loaded")
            }

            override fun onAdFailed(message: String) {
                log(logTag, "Native Ad Failed to Load: $message")
            }
            override fun onAdClicked() {
                log(logTag, "Native Ad Clicked")
            }
            override fun onAdClosed() {
                log(logTag, "Native Ad Closed")
            }
            override fun onAdImpression(adSourceID: String) {
                log(logTag, "Native Ad Impression")
            }
            override fun onAdSwipeGestureClicked() {
                log(logTag, "Native Ad Swipe Gesture Clicked")
            }
            override fun onAdOpened() {
                log(logTag, "Native Ad Opened")
            }
            override fun onAdRevenuePaid(microsValue: Long,
                                         currencyCode: String,
                                         precision: Int,
                                         adNetwork: String) {
                log(logTag, "Native Ad Revenue Paid: " +
                        "value $microsValue, " +
                        "currency $currencyCode, " +
                        "precision: $precision, "+
                        "network: $adNetwork")
            }
            override fun onCloseButtonClicked() {
                log(logTag, "Native Ad Close Button Clicked")
                NativeCodeManager.destroyNativeAd(
                    testMode = true,
                    testActivity = this@MainActivity,
                    adKey = "native_test"
                )
            }
            override fun onCountdownFinished() {
                log(logTag, "Native Ad Countdown Finished")
            }

        }
        //test load native2
        val btnLoadNative = findViewById<TextView>(R.id.btnLoadNativeAd)
        btnLoadNative.setOnClickListener {
            log(logTag, "Load Native Ad 2 button clicked")
            NativeCodeManager.loadNativeAd(
                testMode = true,
                testActivity =this,
                "native_test",
                callback = callback)
        }

        //test show native
        val btnShowNative = findViewById<TextView>(R.id.btnShowNativeAd)
        btnShowNative.setOnClickListener {
            log(logTag, "Show Native Ad button clicked")
            NativeCodeManager.showNativeAd(
                testMode = true,
                testActivity =this,
                adKey = "native_test",
                width = 1200,
                height = 400,
                x = 400,
                y = 50,
                callback = callback
            )
        }

        //test destroy native
        val btnDestroyNative = findViewById<TextView>(R.id.btnDestroyNativeAd)
        btnDestroyNative.setOnClickListener {
            log(logTag, "Destroy Native Ad button clicked")
            NativeCodeManager.destroyNativeAd(
                testMode = true,
                testActivity = this,
                adKey = "native_test")
        }
    }
}