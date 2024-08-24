plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.sih"
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.androidx.recyclerview)
    implementation (libs.glide)
    implementation(libs.androidx.core.splashscreen)
    val nav_version = "2.7.7"

    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    val lifecycle_version = "2.8.0"

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v280)
    implementation(libs.androidx.lifecycle.livedata.ktx.v280)
    implementation (libs.androidx.lifecycle.common.java8)
    implementation (libs.logging.interceptor)

    // shimmer
    implementation (libs.shimmer)

    // coroutine
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.kotlinx.coroutines.core)

}