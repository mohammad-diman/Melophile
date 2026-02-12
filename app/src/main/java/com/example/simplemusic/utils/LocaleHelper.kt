package com.example.simplemusic.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import java.util.Locale

object LocaleHelper {
    fun applyLocale(context: Context, languageCode: String): Context {
        if (languageCode == "system") return context
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        
        return context.createConfigurationContext(config)
    }

    fun Context.findActivity(): ComponentActivity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is ComponentActivity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }
}
