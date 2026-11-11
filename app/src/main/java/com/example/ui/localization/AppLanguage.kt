package com.example.ui.localization

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagOrIcon: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        flagOrIcon = "🇬🇧"
    ),
    MARATHI(
        code = "mr",
        displayName = "Marathi",
        nativeName = "मराठी",
        flagOrIcon = "🇮🇳"
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिंदी",
        flagOrIcon = "🇮🇳"
    );

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
