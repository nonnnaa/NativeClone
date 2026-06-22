package com.s.g.ana.native

enum class NativeLayoutConfig(
    val key: String,
    val layout: String,
    val defaultHeight: Double
) {

    // 🟦 Default
    NativeDefault("default", "ad_native_default_layout", 130.0),

    NativeCustom1("l_c1", "ad_native_landscape_custom1_layout", 250.0),

    NativeCustom2("l_c2", "ad_native_landscape_custom2_layout", 250.0),
    //NativeCustom3("l_c3", "ad_native_landscape_custom3_layout", 250.0),

    // 🟩 landscape layout
    NativeLFooter1("l_footer_banner", "ad_native_landscape_footer_banner_layout", 277.0),
    NativeLFooter2("l_footer_banner2", "ad_native_landscape_footer_banner2_layout", 100.0),
    NativeLFull1("l_full1", "ad_native_landscape_full1_layout", 270.0),
    NativeLFull2("l_full2", "ad_native_landscape_full2_layout", 270.0),
    NativeLFull3("l_full3", "ad_native_landscape_full3_layout", 275.0),

    // 🟨 portrait
    NativePFooter1("p_footer_banner", "ad_native_portrait_footer_banner_layout", 277.0),
    NativePFull1("p_full1", "ad_native_portrait_full1_layout", 270.0),
    NativePFull2("p_full2", "ad_native_portrait_full2_layout", 281.0),
    NativePFull3("p_full3", "ad_native_portrait_full3_layout", 275.0);


    companion object {

        // 🔷 Danh sách tất cả layout
        val valuesList: List<NativeLayoutConfig> = entries

        // 🔍 Helper: tìm layout theo key (tránh crash)
        fun fromKey(key: String?): NativeLayoutConfig? {
            if (key.isNullOrEmpty()) return null
            return entries.firstOrNull { it.key == key }
        }

        // 🔍 Helper: tìm layout theo tên layout
        fun fromLayout(layout: String?): NativeLayoutConfig? {
            if (layout.isNullOrEmpty()) return null
            return entries.firstOrNull { it.layout == layout }
        }
    }
}