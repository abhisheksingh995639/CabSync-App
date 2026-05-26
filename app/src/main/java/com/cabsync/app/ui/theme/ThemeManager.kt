package com.cabsync.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

data class ThemeState(
    val isDark: Boolean,
    val toggle: () -> Unit
)

val LocalTheme = compositionLocalOf<ThemeState> {
    ThemeState(isDark = true, toggle = {})
}
