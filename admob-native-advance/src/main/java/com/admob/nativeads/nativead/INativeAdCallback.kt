package com.admob.nativeads.nativead

interface INativeAdCallback {

    // AdListener events
    fun onAdLoaded() {}
    fun onAdFailed(message: String) {}
    fun onAdClicked() {}
    fun onAdClosed() {}
    fun onAdImpression(adSourceID: String) {}
    fun onAdSwipeGestureClicked() {}
    fun onAdOpened() {}

    // Revenue tracking
    fun onAdRevenuePaid(
        microsValue: Long,
        currencyCode: String,
        precision: Int,
        adNetwork: String
    ) {}

    // Countdown and close button actions
    fun onCloseButtonClicked() {}
    fun onCountdownFinished() {}
}