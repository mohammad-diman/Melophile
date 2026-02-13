package com.example.simplemusic.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.example.simplemusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun formatTime(ms: Long): String {
    val sec = ms / 1000
    return String.format("%d:%02d", sec / 60, sec % 60)
}

suspend fun fetchSongs(context: Context, folderUri: Uri? = null): List<Song> = withContext(Dispatchers.IO) {
    val list = mutableListOf<Song>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) 
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) 
    else 
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        
    val projection = mutableListOf(
        MediaStore.Audio.Media._ID, 
        MediaStore.Audio.Media.TITLE, 
        MediaStore.Audio.Media.ARTIST, 
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DATE_ADDED
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Audio.Media.RELATIVE_PATH)
        } else {
            add(MediaStore.Audio.Media.DATA)
        }
    }.toTypedArray()
    
    val artBase = Uri.parse("content://media/external/audio/albumart")

    var selection: String? = null
    var selectionArgs: Array<String>? = null

    if (folderUri != null) {
        val folderName = getFolderNameFromUri(folderUri)
        if (folderName != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
                selectionArgs = arrayOf("%$folderName%")
            } else {
                selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
                selectionArgs = arrayOf("%/$folderName/%")
            }
        }
    }

    context.contentResolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
        val idIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idIdx)
            val albumId = cursor.getLong(albumIdIdx)
            val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            list.add(Song(
                id = id, 
                title = cursor.getString(titleIdx) ?: "Unknown Title", 
                artist = cursor.getString(artistIdx) ?: "Unknown Artist", 
                uri = songUri,
                albumId = albumId, 
                albumArtUri = ContentUris.withAppendedId(artBase, albumId),
                dateAdded = cursor.getLong(dateIdx)
            ))
        }
    }
    list
}

private fun getFolderNameFromUri(uri: Uri): String? {
    val path = uri.path ?: return null
    return path.split(":").last().split("/").last()
}
