package com.example.simplemusic.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: Uri,
    val albumId: Long,
    val albumArtUri: Uri,
    val dateAdded: Long = 0
)

enum class SortOrder(val label: String) {
    TITLE("Title (A-Z)"),
    ARTIST("Artist (A-Z)"),
    RECENT("Recently Added")
}
