package com.s.g.sample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.admob.nativeads.nativead.INativeAdCallback
import com.admob.nativeads.nativead.NativeCodeManager
import com.admob.nativeads.utils.log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val logTag = "android_native_advance"

        NativeCodeManager.initialize()

        NativeCodeManager.registerNativeAd(
            adKey = "native_test",
            adID = "ca-app-pub-3940256099942544/2247696110",
            adName = "google_ad_mob",
            layout = "l_full1",
            isShowCountDown = true,
            countDownTotalSecond = 5
        )

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

            override fun onAdRevenuePaid(
                microsValue: Long,
                currencyCode: String,
                precision: Int,
                adNetwork: String
            ) {
                log(logTag, "Native Ad Revenue Paid: " +
                        "value $microsValue, " +
                        "currency $currencyCode, " +
                        "precision: $precision, " +
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

        // Load native ad button
        val btnLoadNative = findViewById<TextView>(R.id.btnLoadNativeAd)
        btnLoadNative.setOnClickListener {
            log(logTag, "Load Native Ad button clicked")
            NativeCodeManager.loadNativeAd(
                testMode = true,
                testActivity = this,
                "native_test",
                callback = callback
            )
        }

        // Show native ad button
        val btnShowNative = findViewById<TextView>(R.id.btnShowNativeAd)
        btnShowNative.setOnClickListener {
            log(logTag, "Show Native Ad button clicked")
            NativeCodeManager.showNativeAd(
                testMode = true,
                testActivity = this,
                adKey = "native_test",
                width = 1200,
                height = 400,
                x = 400,
                y = 50,
                callback = callback
            )
        }

        // Destroy native ad button
        val btnDestroyNative = findViewById<TextView>(R.id.btnDestroyNativeAd)
        btnDestroyNative.setOnClickListener {
            log(logTag, "Destroy Native Ad button clicked")
            NativeCodeManager.destroyNativeAd(
                testMode = true,
                testActivity = this,
                adKey = "native_test"
            )
        }
    }
}