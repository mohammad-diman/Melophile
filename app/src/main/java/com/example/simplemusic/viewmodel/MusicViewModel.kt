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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
    var currentSortOrder by mutableStateOf(SortOrder.TITLE)
    var selectedFolderUri by mutableStateOf<android.net.Uri?>(null)
    var searchQuery by mutableStateOf("")
    
    var currentSong by mutableStateOf<Song?>(null)
    var isPlaying by mutableStateOf(false)
    var repeatMode by mutableStateOf(2)
    var isShuffleEnabled by mutableStateOf(false)
    var showFullPlayer by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    var dynamicAccentColor by mutableStateOf(AccentColor)
    var showSleepTimerSheet by mutableStateOf(false)
    var sleepTimerMinutes by mutableIntStateOf(0)
    var isSleepTimerActive by mutableStateOf(false)
    var songToEdit by mutableStateOf<Song?>(null)
    var currentLanguage by mutableStateOf("system")
    
    // Play counts cache for derived states
    private var playCounts by mutableStateOf(mapOf<Long, Int>())
    var weeklyActivity by mutableStateOf(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f))

    val dailyMix by derivedStateOf {
        rawSongs.sortedByDescending { playCounts[it.id] ?: 0 }.take(6)
    }

    val stats by derivedStateOf {
        val totalPlays = playCounts.values.sum()
        val topArtist = rawSongs.groupBy { it.artist }
            .maxByOrNull { group -> group.value.sumOf { playCounts[it.id] ?: 0 } }?.key ?: "N/A"
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
        observeSettings()
        initializeController()
        startPositionUpdateLoop()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsManager.sortOrder
                .distinctUntilChanged()
                .collect { currentSortOrder = it }
        }
        viewModelScope.launch {
            settingsManager.musicDirectory
                .distinctUntilChanged()
                .collect { uri ->
                    if (uri != selectedFolderUri) {
                        selectedFolderUri = uri
                        loadSongs(getApplication())
                    }
                }
        }
        viewModelScope.launch {
            settingsManager.repeatMode
                .distinctUntilChanged()
                .collect { repeatMode = it; player?.repeatMode = it }
        }
        viewModelScope.launch {
            settingsManager.shuffleMode
                .distinctUntilChanged()
                .collect { isShuffleEnabled = it; player?.shuffleModeEnabled = it }
        }
        viewModelScope.launch {
            settingsManager.appLanguage
                .distinctUntilChanged()
                .collect { currentLanguage = it }
        }
        viewModelScope.launch {
            settingsManager.getWeeklyActivity().collect { weeklyActivity = it }
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            settingsManager.resetStats()
            // Reset local playCounts map to 0s for all known songs to immediately update UI
            val resetMap = mutableMapOf<Long, Int>()
            rawSongs.forEach { resetMap[it.id] = 0 }
            playCounts = resetMap
            // weeklyActivity will be updated by collector automatically
        }
    }

    private fun initializeController() {
        val sessionToken = SessionToken(getApplication(), ComponentName(getApplication(), MusicService::class.java))
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            player = controllerFuture?.get()
            isControllerReady = true
            setupPlayerListener()
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
                    viewModelScope.launch {
                        settingsManager.incrementPlayCount(currentSong!!.id)
                        settingsManager.saveLastPlayedSongId(currentSong!!.id)
                        // Update local count for UI
                        val currentCount = playCounts[currentSong!!.id] ?: 0
                        playCounts = playCounts + (currentSong!!.id to currentCount + 1)
                    }
                    updateDynamicColor(currentSong!!)
                }
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
        viewModelScope.launch {
            val fetched = fetchSongs(context, selectedFolderUri)
            
            // Fetch play counts for all fetched songs
            val counts = mutableMapOf<Long, Int>()
            fetched.forEach { song ->
                counts[song.id] = settingsManager.getPlayCount(song.id).first()
            }
            playCounts = counts

            rawSongs = fetched.map { song ->
                val override = settingsManager.getSongOverride(song.id)
                song.copy(
                    title = override.first ?: song.title,
                    artist = override.second ?: song.artist
                )
            }
            
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
    }

    private fun updatePlayerPlaylist() {
        viewModelScope.launch {
            val mediaItems = songs.map { MediaItem.fromUri(it.uri) }
            
            val lastId = settingsManager.getLastPlayedSongId()
            val index = if (lastId != -1L) songs.indexOfFirst { it.id == lastId }.coerceAtLeast(0) else 0
            val position = settingsManager.getLastPosition()

            player?.setMediaItems(mediaItems, index, position)
            player?.prepare()
        }
    }

    fun playSong(song: Song) {
        if (!isControllerReady) return
        
        val index = songs.indexOf(song)
        if (index != -1) {
            currentSong = song
            player?.seekTo(index, 0L)
            player?.play()
            viewModelScope.launch {
                settingsManager.incrementPlayCount(song.id)
                val currentCount = playCounts[song.id] ?: 0
                playCounts = playCounts + (song.id to currentCount + 1)
            }
            updateDynamicColor(song)
        }
    }

    fun togglePlay() { if (isPlaying) player?.pause() else player?.play() }
    fun next() = player?.seekToNextMediaItem()
    fun previous() = player?.seekToPreviousMediaItem()
    fun toggleRepeat() {
        val nextMode = when (player?.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        viewModelScope.launch { settingsManager.saveRepeatMode(nextMode) }
    }
    fun toggleShuffle() { 
        val nextShuffle = !(player?.shuffleModeEnabled ?: false)
        viewModelScope.launch { settingsManager.saveShuffleMode(nextShuffle) }
    }
    fun seekTo(pos: Long) = player?.seekTo(pos)
    fun updateSongInfo(songId: Long, newTitle: String, newArtist: String) {
        viewModelScope.launch {
            settingsManager.saveSongOverride(songId, newTitle, newArtist)
            rawSongs = rawSongs.map { if (it.id == songId) it.copy(title = newTitle, artist = newArtist) else it }
            if (currentSong?.id == songId) currentSong = currentSong?.copy(title = newTitle, artist = newArtist)
        }
    }

    fun updateFolder(uri: android.net.Uri) {
        viewModelScope.launch {
            settingsManager.saveMusicDirectory(uri)
            // loadSongs akan dipicu oleh observer
        }
    }
    fun updateSortOrder(order: SortOrder) {
        viewModelScope.launch {
            settingsManager.saveSortOrder(order)
            // playlist update logic will be triggered by rawSongs change or manual call if needed
            // Actually, we should call updatePlayerPlaylist() here too if we want immediate sync
            delay(100) // wait for flow
            updatePlayerPlaylist()
        }
    }
    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            settingsManager.saveLanguage(lang)
        }
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
