package com.example.sih.application

import android.app.Application
import android.preference.PreferenceManager
import com.example.sih.ui.theme.ThemeColor
import com.example.sih.ui.theme.ThemeManager
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import com.example.sih.R
import com.example.sih.repository.AQIPredictor

@HiltAndroidApp
class MyApplication : Application() {
    //lateinit var aqiPredictor: AQIPredictor
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // aqiPredictor = AQIPredictor(this)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        ThemeManager.setDarkTheme(prefs.getBoolean("dark_theme", false))
        ThemeManager.setThemeColor(
            ThemeColor.values()[prefs.getInt("theme_color", 0)]
        )
        Places.initialize(applicationContext, this.getString(R.string.PLACE_API_KEY))
    }
}
