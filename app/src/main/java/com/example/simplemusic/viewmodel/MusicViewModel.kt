package com.example.simplemusic.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.CountDownTimer
import android.provider.MediaStore
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import com.example.simplemusic.model.Song
import com.example.simplemusic.model.SortOrder
import com.example.simplemusic.service.MusicService
import com.example.simplemusic.ui.theme.AccentColor
import com.example.simplemusic.utils.SettingsManager
import com.example.simplemusic.utils.fetchSongs
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var player: Player? by mutableStateOf(null)
    var isControllerReady by mutableStateOf(false)
    
    val settingsManager = SettingsManager(application)
    private var sleepTimer: CountDownTimer? = null

    // States
    var rawSongs by mutableStateOf(listOf<Song>())
    var currentSortOrder by mutableStateOf(settingsManager.getSortOrder())
    var selectedFolderUri by mutableStateOf(settingsManager.getMusicDirectory())
    var searchQuery by mutableStateOf("")
    
    var currentSong by mutableStateOf<Song?>(null)
    var isPlaying by mutableStateOf(false)
    var repeatMode by mutableStateOf(settingsManager.getRepeatMode())
    var isShuffleEnabled by mutableStateOf(settingsManager.getShuffleMode())
    var showFullPlayer by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    var dynamicAccentColor by mutableStateOf(AccentColor)
    var showSleepTimerSheet by mutableStateOf(false)
    var sleepTimerMinutes by mutableIntStateOf(0)
    var isSleepTimerActive by mutableStateOf(false)
    var songToEdit by mutableStateOf<Song?>(null)
    var currentLanguage by mutableStateOf(settingsManager.getLanguage())

    val dailyMix by derivedStateOf {
        rawSongs.sortedByDescending { settingsManager.getPlayCount(it.id) }.take(6)
    }

    val stats by derivedStateOf {
        val totalPlays = rawSongs.sumOf { settingsManager.getPlayCount(it.id) }
        val topArtist = rawSongs.groupBy { it.artist }
            .maxByOrNull { group -> group.value.sumOf { settingsManager.getPlayCount(it.id) } }?.key ?: "N/A"
        mapOf("total" to totalPlays.toString(), "artist" to topArtist)
    }

    val songs by derivedStateOf {
        val sorted = when (currentSortOrder) {
            SortOrder.TITLE -> rawSongs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> rawSongs.sortedBy { it.artist.lowercase() }
            SortOrder.RECENT -> rawSongs.sortedByDescending { it.dateAdded }
        }
        if (searchQuery.isBlank()) sorted else sorted.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    init {
        initializeController()
        startPositionUpdateLoop()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(getApplication(), ComponentName(getApplication(), MusicService::class.java))
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            player = controllerFuture?.get()
            isControllerReady = true
            setupPlayerListener()
            // Setelah controller siap, muat lagu jika data sudah ada
            if (rawSongs.isNotEmpty()) {
                updatePlayerPlaylist()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        player?.repeatMode = repeatMode
        player?.shuffleModeEnabled = isShuffleEnabled
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) { isPlaying = isPlayingState }
            override fun onPlaybackStateChanged(playbackState: Int) { 
                if (playbackState == Player.STATE_READY) duration = player?.duration ?: 0L 
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = player?.currentMediaItemIndex ?: -1
                if (index in songs.indices) {
                    currentSong = songs[index]
                    settingsManager.incrementPlayCount(currentSong!!.id)
                    settingsManager.saveLastPlayedSongId(currentSong!!.id)
                    updateDynamicColor(currentSong!!)
                }
            }
            override fun onRepeatModeChanged(mode: Int) { 
                repeatMode = mode 
                settingsManager.saveRepeatMode(mode)
            }
            override fun onShuffleModeEnabledChanged(enabled: Boolean) { 
                isShuffleEnabled = enabled 
                settingsManager.saveShuffleMode(enabled)
            }
        })
    }

    private fun updateDynamicColor(song: Song) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, song.albumArtUri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, song.albumArtUri)
                    }
                } catch (e: Exception) { null }
            }
            bitmap?.let { bmp ->
                Palette.from(bmp).generate { palette ->
                    val color = palette?.getVibrantColor(AccentColor.toArgb()) ?: palette?.getMutedColor(AccentColor.toArgb()) ?: AccentColor.toArgb()
                    dynamicAccentColor = Color(color)
                }
            } ?: run { dynamicAccentColor = AccentColor }
        }
    }

    private fun startPositionUpdateLoop() {
        viewModelScope.launch {
            while (true) {
                if (isPlaying) {
                    currentPosition = player?.currentPosition ?: 0L
                    settingsManager.saveLastPosition(currentPosition)
                }
                delay(1000)
            }
        }
    }

    fun loadSongs(context: Context) {
        val fetched = fetchSongs(context, selectedFolderUri)
        rawSongs = fetched.map { song ->
            val override = settingsManager.getSongOverride(song.id)
            song.copy(
                title = override.first ?: song.title,
                artist = override.second ?: song.artist
            )
        }
        
        // Restore last played song if nothing is currently playing
        if (currentSong == null) {
            val lastId = settingsManager.getLastPlayedSongId()
            if (lastId != -1L) {
                val lastSong = rawSongs.find { it.id == lastId }
                if (lastSong != null) {
                    currentSong = lastSong
                    updateDynamicColor(lastSong)
                }
            }
        }

        if (isControllerReady) {
            updatePlayerPlaylist()
        }
    }

    private fun updatePlayerPlaylist() {
        val mediaItems = songs.map { MediaItem.fromUri(it.uri) }
        
        val lastId = settingsManager.getLastPlayedSongId()
        val index = if (lastId != -1L) songs.indexOfFirst { it.id == lastId }.coerceAtLeast(0) else 0
        val position = settingsManager.getLastPosition()

        player?.setMediaItems(mediaItems, index, position)
        player?.prepare()
    }

    fun playSong(song: Song) {
        if (!isControllerReady) return
        
        val index = songs.indexOf(song)
        if (index != -1) {
            currentSong = song
            player?.seekTo(index, 0L)
            player?.play()
            settingsManager.incrementPlayCount(song.id)
            updateDynamicColor(song)
        }
    }

    fun togglePlay() { if (isPlaying) player?.pause() else player?.play() }
    fun next() = player?.seekToNextMediaItem()
    fun previous() = player?.seekToPreviousMediaItem()
    fun toggleRepeat() {
        player?.repeatMode = when (player?.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }
    fun toggleShuffle() { player?.shuffleModeEnabled = !(player?.shuffleModeEnabled ?: false) }
    fun seekTo(pos: Long) = player?.seekTo(pos)
    fun updateSongInfo(songId: Long, newTitle: String, newArtist: String) {
        settingsManager.saveSongOverride(songId, newTitle, newArtist)
        rawSongs = rawSongs.map { if (it.id == songId) it.copy(title = newTitle, artist = newArtist) else it }
        if (currentSong?.id == songId) currentSong = currentSong?.copy(title = newTitle, artist = newArtist)
    }
    fun updateFolder(uri: android.net.Uri) {
        settingsManager.saveMusicDirectory(uri)
        selectedFolderUri = uri
        loadSongs(getApplication())
    }
    fun updateSortOrder(order: SortOrder) {
        currentSortOrder = order
        settingsManager.saveSortOrder(order)
        updatePlayerPlaylist()
    }
    fun updateLanguage(lang: String) {
        currentLanguage = lang
        settingsManager.saveLanguage(lang)
    }
    fun setSleepTimer(minutes: Int) {
        sleepTimer?.cancel()
        sleepTimerMinutes = minutes
        if (minutes == 0) { isSleepTimerActive = false; return }
        isSleepTimerActive = true
        sleepTimer = object : CountDownTimer(minutes * 60 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                // Update sisa menit setiap detik
                val remainingMinutes = (millisUntilFinished / 1000 / 60).toInt()
                // Jika detik masih ada, kita bulatkan ke atas agar tidak langsung jadi 0
                sleepTimerMinutes = if (millisUntilFinished % 60000 > 0) remainingMinutes + 1 else remainingMinutes
            }
            override fun onFinish() { 
                player?.pause()
                isSleepTimerActive = false
                sleepTimerMinutes = 0 
            }
        }.start()
    }
    override fun onCleared() {
        super.onCleared()
        sleepTimer?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
