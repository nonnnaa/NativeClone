package com.s.g.ana.utils
import android.util.Log
import com.s.g.ana.Config

fun log(tag: String, message: String) {
    if (Config.isDebug)
        Log.v(tag, message)
}