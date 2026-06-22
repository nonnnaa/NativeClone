package com.admob.nativeads.nativead
interface INativeAdCallback {

    //ad AdListener
    fun onAdLoaded()
    fun onAdFailed(message: String)
    fun onAdClicked()
    fun onAdClosed()
    fun onAdImpression(adSourceID: String)
    fun onAdSwipeGestureClicked()
    fun onAdOpened()

    //revenue
    fun onAdRevenuePaid(microsValue: Long,
                        currencyCode: String,
                        precision: Int,
                        adNetwork: String)

    //countdown and close button
    fun onCloseButtonClicked()
    fun onCountdownFinished()
}