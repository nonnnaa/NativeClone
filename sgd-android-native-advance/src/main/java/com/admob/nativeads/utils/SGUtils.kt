package com.admob.nativeads.utils
import android.util.Log
import com.admob.nativeads.Config

fun log(tag: String, message: String) {
    if (Config.isDebug)
        Log.v(tag, message)
}