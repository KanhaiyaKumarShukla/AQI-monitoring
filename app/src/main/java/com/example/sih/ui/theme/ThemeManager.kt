package com.example.sih.ui.theme


import androidx.compose.material3.ChipColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

object ThemeManager {
    private val _darkTheme = mutableStateOf(false)
    val darkTheme: State<Boolean> = _darkTheme

    private val _themeColor = mutableStateOf(ThemeColor.DEFAULT)
    val themeColor: State<ThemeColor> = _themeColor

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        // Save to SharedPreferences or DataStore
    }

    fun setThemeColor(color: ThemeColor) {
        _themeColor.value = color
        // Save to SharedPreferences or DataStore
    }
}

enum class ThemeColor(
    val color: Color,
    val displayName: String
) {
    DEFAULT(Purple40, "Default"),
    BLUE(Color(0xFF0D47A1), "Blue"),
    GREEN(Color(0xFF3E6E4D), "Green"),
    PURPLE(Color(0xFFB94E41), "Orange");

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeColor {
            return values().getOrElse(ordinal) { DEFAULT }
        }
    }
}

