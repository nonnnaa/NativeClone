plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.s.g.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.s.g.androidnative"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":admob-native-advance"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // mediation
    implementation("com.google.ads.mediation:facebook:6.20.0.0")
    implementation("com.google.ads.mediation:pangle:7.3.0.3.0")
    implementation("com.google.ads.mediation:applovin:13.3.1.1")
    //implementation("com.google.ads.mediation:vungle:7.4.2.0")
    implementation("com.google.ads.mediation:mintegral:17.0.61.0")
//    // firebase
//    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
//    implementation("com.google.firebase:firebase-messaging")
//    implementation("com.google.firebase:firebase-analytics")
//    implementation("com.google.firebase:firebase-crashlytics")
//    implementation("com.google.firebase:firebase-config")
//
//    //Ads google
//    implementation("com.google.android.gms:play-services-ads:24.4.0")
//
//    //multidex
//    implementation("androidx.multidex:multidex:2.0.1")
//    implementation("com.facebook.shimmer:shimmer:0.5.0")
//    implementation("com.tbuonomo:dotsindicator:5.1.0")
//
//    //ump
//    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
//
//    //SDK Mediation
//    implementation("com.google.ads.mediation:facebook:6.20.0.0")
//    implementation("com.google.ads.mediation:pangle:7.3.0.3.0")
//    implementation("com.google.ads.mediation:applovin:13.3.1.1")
//
//    //fb sdk
//    implementation("com.facebook.android:facebook-android-sdk:latest.release")
}