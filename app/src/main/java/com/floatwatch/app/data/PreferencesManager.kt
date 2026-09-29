package com.floatwatch.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "floatwatch_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_IS_PRO_USER = booleanPreferencesKey("is_pro_user")
        private val KEY_SELECTED_THEME = stringPreferencesKey("selected_theme")
        private val KEY_OPACITY = floatPreferencesKey("overlay_opacity")
        private val KEY_SOUND_HAPTIC = booleanPreferencesKey("sound_haptic_enabled")
    }

    val isProUserFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PRO_USER] ?: false
    }

    val selectedThemeFlow: Flow<OverlayTheme> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_SELECTED_THEME] ?: OverlayTheme.MINIMAL_DARK.name
        try {
            OverlayTheme.valueOf(name)
        } catch (_: Exception) {
            OverlayTheme.MINIMAL_DARK
        }
    }

    val opacityFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPACITY] ?: 0.95f
    }

    val hapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_HAPTIC] ?: true
    }

    suspend fun setProUser(isPro: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PRO_USER] = isPro
        }
    }

    suspend fun setSelectedTheme(theme: OverlayTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_THEME] = theme.name
        }
    }

    suspend fun setOpacity(opacity: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPACITY] = opacity.coerceIn(0.2f, 1.0f)
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_HAPTIC] = enabled
        }
    }
}
