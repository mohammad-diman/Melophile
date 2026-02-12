package com.example.simplemusic.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

class SettingsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("music_settings", Context.MODE_PRIVATE)
    private val statsPrefs = context.getSharedPreferences("music_stats", Context.MODE_PRIVATE)

    fun saveMusicDirectory(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        prefs.edit().putString("music_dir", uri.toString()).apply()
    }

    fun getMusicDirectory(): Uri? {
        val uriString = prefs.getString("music_dir", null)
        return uriString?.let { Uri.parse(it) }
    }

    // Statistik
    fun incrementPlayCount(songId: Long) {
        val current = statsPrefs.getInt(songId.toString(), 0)
        statsPrefs.edit().putInt(songId.toString(), current + 1).apply()
    }

    fun getPlayCount(songId: Long): Int {
        return statsPrefs.getInt(songId.toString(), 0)
    }

    fun saveLastPlayedSongId(songId: Long) {
        prefs.edit().putLong("last_song_id", songId).apply()
    }

    fun getLastPlayedSongId(): Long {
        return prefs.getLong("last_song_id", -1L)
    }

    fun saveLastPosition(position: Long) {
        prefs.edit().putLong("last_position", position).apply()
    }

    fun getLastPosition(): Long {
        return prefs.getLong("last_position", 0L)
    }

    fun saveSortOrder(order: com.example.simplemusic.model.SortOrder) {
        prefs.edit().putString("sort_order", order.name).apply()
    }

    fun getSortOrder(): com.example.simplemusic.model.SortOrder {
        val name = prefs.getString("sort_order", com.example.simplemusic.model.SortOrder.TITLE.name)
        return try { com.example.simplemusic.model.SortOrder.valueOf(name!!) } catch(e: Exception) { com.example.simplemusic.model.SortOrder.TITLE }
    }

    fun saveShuffleMode(enabled: Boolean) {
        prefs.edit().putBoolean("shuffle_mode", enabled).apply()
    }

    fun getShuffleMode(): Boolean {
        return prefs.getBoolean("shuffle_mode", false)
    }

    fun saveRepeatMode(mode: Int) {
        prefs.edit().putInt("repeat_mode", mode).apply()
    }

    fun getRepeatMode(): Int {
        // Default to REPEAT_MODE_ALL (2)
        return prefs.getInt("repeat_mode", 2)
    }

    fun saveLastRoute(route: String) {
        prefs.edit().putString("last_route", route).apply()
    }

    fun getLastRoute(): String? {
        return prefs.getString("last_route", null)
    }

    fun saveSongOverride(songId: Long, title: String, artist: String) {
        prefs.edit().putString("override_title_$songId", title).apply()
        prefs.edit().putString("override_artist_$songId", artist).apply()
    }

    fun getSongOverride(songId: Long): Pair<String?, String?> {
        val title = prefs.getString("override_title_$songId", null)
        val artist = prefs.getString("override_artist_$songId", null)
        return title to artist
    }
}
