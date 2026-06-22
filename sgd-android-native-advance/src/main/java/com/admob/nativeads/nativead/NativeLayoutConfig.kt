package com.admob.nativeads.nativead

enum class NativeLayoutConfig(
    val key: String,
    val layout: String,
    val defaultHeight: Double
) {

    // Default
    NativeCustom1("panel_layout", "panel_layout", 250.0),
    NativeCustom2("banner_layout", "banner_layout", 100.0),
    NativeCustom3("fullscreen_layout", "fullscreen_layout", 270.0);

    companion object {

        fun fromKey(key: String?): NativeLayoutConfig? {
            if (key.isNullOrEmpty()) return null
            return entries.firstOrNull { it.key == key }
        }

        fun fromLayout(layout: String?): NativeLayoutConfig? {
            if (layout.isNullOrEmpty()) return null
            return entries.firstOrNull { it.layout == layout }
        }
    }
}