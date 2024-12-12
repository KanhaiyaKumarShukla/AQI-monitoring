plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.sih"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sih"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        renderscriptTargetApi = 19
        renderscriptSupportModeEnabled = true
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
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.androidx.recyclerview)
    implementation (libs.glide)
    implementation(libs.androidx.core.splashscreen)

    // compose
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material3)

    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.common.java8)
    implementation (libs.logging.interceptor)

    // shimmer
    implementation (libs.shimmer)

    // coroutine
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.kotlinx.coroutines.core)

    implementation(libs.androidx.fragment.ktx)
    // cronet
    implementation(libs.play.services.cronet)
    implementation (libs.cronet.api)

    // maps
    implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)
    implementation (libs.play.services.location)

    implementation (libs.okhttp)
    // coroutine
    implementation(libs.kotlinx.coroutines.core.v190)
    implementation (libs.kotlinx.coroutines.android.v190)

    // graph
    implementation (libs.mpandroidchart)

    // socket.io
    implementation (libs.socket.io.client)

    // kotlin reflect
    implementation(kotlin("reflect"))

    implementation (libs.shimmer)
    implementation (libs.mpandroidchart)


}