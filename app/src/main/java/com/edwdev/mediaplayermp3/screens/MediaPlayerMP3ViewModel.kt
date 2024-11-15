package com.edwdev.mediaplayermp3.screens

import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.edwdev.mediaplayermp3.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaPlayerMP3ViewModel : ViewModel() {
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // MediaPlayer
    var mediaPlayer: MediaPlayer? = null

    val currentSong = MutableLiveData<Song?>(null)


    // Cargar canciones desde el dispositivo
    fun loadSongs(context: Context) {
        // Lista para almacenar las canciones
        val songsList = mutableListOf<Song>()

        // Uri para acceder a la colección de canciones
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // Columnas que deseas recuperar de la base de datos
        val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA)

        // Realizar la consulta a la base de datos
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

        // Recorrer el cursor y agregar canciones a la lista
        cursor?.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            // Recorrer el cursor y agregar canciones a la lista
            while (it.moveToNext()) {
                val songName = it.getString(nameColumn)
                val songUri = it.getString(dataColumn)
                // solo agregar audios mp3 excluyendo audios tipo ringtone y los de whatsapp
                if (songUri.endsWith(".mp3", ignoreCase = true) && !songName.startsWith("AUD", ignoreCase = true) && !songName.startsWith("tone")) {
                    songsList.add(Song(title = songName, uri = songUri))
                }
            }
        }

        // Actualizar la lista de canciones
        _songs.value = songsList
    }

    // Reproducir y pausar canciones
    fun playSong(song: Song) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.uri)
            prepare()
            // iniciar la siguiente canción de forma automática
            setOnCompletionListener {
                playNextSong()
            }
            start()
            _isPlaying.value = true
        }
    }

    // Pausar
    fun pauseSong() {
        mediaPlayer?.pause()
        _isPlaying.value = false
    }

    // Reanudar
    fun resumeSong() {
        mediaPlayer?.start()
        _isPlaying.value = true
    }

    // Reproducir el audio anterior
    fun playPreviousSong() {
        val currentSongIndex = songs.value?.indexOf(currentSong.value) ?: -1
        if (currentSongIndex > 0) {
            currentSong.value = songs.value?.get(currentSongIndex - 1)
            currentSong.value?.let { playSong(it) }
        }
    }

    // Reproducir el audio siguiente
    fun playNextSong() {
        val currentSongIndex = songs.value?.indexOf(currentSong.value) ?: -1
        val nextIndex = (currentSongIndex + 1) % (songs.value?.size ?: 1)
        currentSong.value = songs.value?.get(nextIndex)
        playSong(currentSong.value!!)
    }

    // Limpiar recursos al eliminar el ViewModel
    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}