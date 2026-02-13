package com.example.simplemusic.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.simplemusic.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_settings")

class SettingsManager(private val context: Context) {

    private val musicDirKey = stringPreferencesKey("music_dir")
    private val lastSongIdKey = longPreferencesKey("last_song_id")
    private val lastPositionKey = longPreferencesKey("last_position")
    private val sortOrderKey = stringPreferencesKey("sort_order")
    private val shuffleModeKey = booleanPreferencesKey("shuffle_mode")
    private val repeatModeKey = intPreferencesKey("repeat_mode")
    private val lastRouteKey = stringPreferencesKey("last_route")
    private val appLanguageKey = stringPreferencesKey("app_language")

    // Dynamic keys helper
    private fun playCountKey(songId: Long) = intPreferencesKey("play_count_$songId")
    private fun dailyCountKey(dayOfWeek: Int) = intPreferencesKey("daily_count_$dayOfWeek")
    private fun overrideTitleKey(songId: Long) = stringPreferencesKey("override_title_$songId")
    private fun overrideArtistKey(songId: Long) = stringPreferencesKey("override_artist_$songId")

    val musicDirectory: Flow<Uri?> = context.dataStore.data.map { prefs ->
        prefs[musicDirKey]?.let { Uri.parse(it) }
    }

    suspend fun saveMusicDirectory(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        context.dataStore.edit { it[musicDirKey] = uri.toString() }
    }

    suspend fun incrementPlayCount(songId: Long) {
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // 1 (Sun) to 7 (Sat)

        context.dataStore.edit { prefs ->
            val current = prefs[playCountKey(songId)] ?: 0
            prefs[playCountKey(songId)] = current + 1

            val dailyCurrent = prefs[dailyCountKey(dayOfWeek)] ?: 0
            prefs[dailyCountKey(dayOfWeek)] = dailyCurrent + 1
        }
    }

    fun getPlayCount(songId: Long): Flow<Int> = context.dataStore.data.map { it[playCountKey(songId)] ?: 0 }

    fun getWeeklyActivity(): Flow<List<Float>> = context.dataStore.data.map { prefs ->
        val activity = mutableListOf<Float>()
        for (i in 1..7) {
            activity.add((prefs[dailyCountKey(i)] ?: 0).toFloat())
        }
        val max = activity.maxOrNull() ?: 1f
        if (max == 0f) activity.map { 0f } else activity.map { it / max }
    }

    suspend fun resetStats() {
        context.dataStore.edit { prefs ->
            val keysToRemove = prefs.asMap().keys.filter { 
                it.name.startsWith("play_count_") || it.name.startsWith("daily_count_") 
            }
            keysToRemove.forEach { prefs.remove(it) }
        }
    }

    suspend fun saveLastPlayedSongId(songId: Long) {
        context.dataStore.edit { it[lastSongIdKey] = songId }
    }

    suspend fun getLastPlayedSongId(): Long = context.dataStore.data.map { it[lastSongIdKey] ?: -1L }.first()

    suspend fun saveLastPosition(position: Long) {
        context.dataStore.edit { it[lastPositionKey] = position }
    }

    suspend fun getLastPosition(): Long = context.dataStore.data.map { it[lastPositionKey] ?: 0L }.first()

    suspend fun saveSortOrder(order: SortOrder) {
        context.dataStore.edit { it[sortOrderKey] = order.name }
    }

    val sortOrder: Flow<SortOrder> = context.dataStore.data.map { prefs ->
        val name = prefs[sortOrderKey] ?: SortOrder.TITLE.name
        try { SortOrder.valueOf(name) } catch (e: Exception) { SortOrder.TITLE }
    }

    suspend fun saveShuffleMode(enabled: Boolean) {
        context.dataStore.edit { it[shuffleModeKey] = enabled }
    }

    val shuffleMode: Flow<Boolean> = context.dataStore.data.map { it[shuffleModeKey] ?: false }

    suspend fun saveRepeatMode(mode: Int) {
        context.dataStore.edit { it[repeatModeKey] = mode }
    }

    val repeatMode: Flow<Int> = context.dataStore.data.map { it[repeatModeKey] ?: 2 }

    suspend fun saveLastRoute(route: String) {
        context.dataStore.edit { it[lastRouteKey] = route }
    }

    suspend fun getLastRoute(): String? = context.dataStore.data.map { it[lastRouteKey] }.first()

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { it[appLanguageKey] = languageCode }
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { it[appLanguageKey] ?: "system" }

    suspend fun getLanguage(): String = appLanguage.first()

    fun getLanguageSync(): String = runBlocking { getLanguage() }

    suspend fun saveSongOverride(songId: Long, title: String, artist: String) {
        context.dataStore.edit { prefs ->
            prefs[overrideTitleKey(songId)] = title
            prefs[overrideArtistKey(songId)] = artist
        }
    }

    suspend fun getSongOverride(songId: Long): Pair<String?, String?> {
        val prefs = context.dataStore.data.first()
        return prefs[overrideTitleKey(songId)] to prefs[overrideArtistKey(songId)]
    }

    suspend fun getInitialSortOrder(): SortOrder = sortOrder.first()
    suspend fun getInitialRepeatMode(): Int = repeatMode.first()
    suspend fun getInitialShuffleMode(): Boolean = shuffleMode.first()
    suspend fun getInitialLanguage(): String = appLanguage.first()
}
