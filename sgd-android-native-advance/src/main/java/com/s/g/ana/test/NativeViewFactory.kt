package com.s.g.ana.test

import android.R
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.unity3d.player.UnityPlayer

class NativeViewFactory {
    companion object {
        @JvmStatic // Bắt buộc để CallStatic từ C# nhận diện trực tiếp
        fun createView(
            testMode: Boolean,
            testActivity: Activity?,
            r: Int, g: Int, b: Int,
            width: Int, height: Int,
            x: Int, y: Int
        ) {
            val activity = if (testMode) testActivity else UnityPlayer.currentActivity
            activity?.runOnUiThread {
                val layoutParams = FrameLayout.LayoutParams(width, height).apply {
                    leftMargin = x
                    topMargin = y
                }

                val myView = View(activity).apply {
                    setBackgroundColor(Color.rgb(r, g, b))
                    tag = "NATIVE_VIEW" // Đánh dấu để xóa sau này
                }

                activity.addContentView(myView, layoutParams)
            }
        }

        @JvmStatic
        fun removeAllViews(
            testMode: Boolean,
            testActivity: Activity?
        ) {
            val activity = if (testMode) testActivity else UnityPlayer.currentActivity
            activity?.runOnUiThread {
                val rootView =
                    activity.window.decorView.findViewById<FrameLayout>(R.id.content)

                // Tìm và xóa các view có tag là "NATIVE_VIEW"
                var viewToRemove: View?
                do {
                    viewToRemove = rootView.findViewWithTag("NATIVE_VIEW")
                    if (viewToRemove != null) {
                        rootView.removeView(viewToRemove)
                    }
                } while (viewToRemove != null)
            }
        }
    }
}