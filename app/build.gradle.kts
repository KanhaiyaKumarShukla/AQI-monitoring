plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.compose.compiler)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
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
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
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
    implementation (libs.androidx.ui)
    implementation (libs.androidx.material3)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.coil.compose)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.navigation.compose)
    implementation (libs.androidx.material.icons.extended)
    val lifecycle_version = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // Lifecycle utilities for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")

    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

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
    implementation(libs.maps.compose)

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


    // dagger hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation (libs.androidx.hilt.navigation.compose)

    // line graph
    implementation(libs.composable.graphs)
    implementation (libs.kotlinx.datetime)
    implementation (libs.androidx.datastore.preferences)

    implementation(libs.maps.compose.v641)
    implementation(libs.play.services.maps.v1910)
    implementation (libs.places)
    implementation (libs.places.ktx)

    // Animation
    implementation (libs.androidx.animation)

    implementation("com.halilibo.compose-richtext:richtext-ui-material:0.17.0")
    implementation("com.halilibo.compose-richtext:richtext-commonmark:0.17.0")
    implementation(libs.richtext.ui)


    implementation ("com.github.skydoves:landscapist-glide:2.3.3")
    implementation ("com.github.a914-gowtham:compose-ratingbar:1.3.4")
    implementation ("com.github.commandiron:WheelPickerCompose:1.1.11")

    //tensorflow
    implementation (libs.tensorflow.lite)
    implementation (libs.tensorflow.lite.support)
    implementation (libs.gson)
    implementation ("org.tensorflow:tensorflow-lite-select-tf-ops:2.10.0")

    // location
    implementation(libs.accompanist.permissions)

    //coil
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil.gif)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

}
kapt {
    correctErrorTypes = true
}