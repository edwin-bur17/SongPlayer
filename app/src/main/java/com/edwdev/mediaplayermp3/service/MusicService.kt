package com.edwdev.mediaplayermp3.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.edwdev.mediaplayermp3.MainActivity
import com.edwdev.mediaplayermp3.R
import com.edwdev.mediaplayermp3.model.Song

class MusicService : Service() {
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private val binder = MusicBinder()
    private val notificationId = 1
    private val channelId = "MusicPlayerChannel"

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        initMediaSession()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.isActive = true
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music player controls"
        }
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("ForegroundServiceType")
    fun updateNotification(song: Song, isPlaying: Boolean, duration: Int, position: Int) {
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"
        val songTitle: String = song.title.substringBefore(".")
        Log.i("songtilte", songTitle)

        // Crear intents para los botones
        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("PLAY_PAUSE"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent("NEXT"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousIntent = PendingIntent.getBroadcast(
            this,
            2,
            Intent("PREVIOUS"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para abrir la actividad al tocar la notificación
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                // Agregar banderas para manejar la navegación
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

                // Pasar datos adicionales para la navegación
                putExtra("NAVIGATE_TO", "songPlayer")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Actualizar metadata
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration.toLong())
            .build()
        mediaSession.setMetadata(metadata)

        // Configuración de estado de reproducción con acciones específicas
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                else PlaybackStateCompat.STATE_PAUSED,
                position.toLong(),
                1f
            )
            .build()

        // IMPORTANTE: Usar un MediaSessionCompat.Callback para manejar las acciones
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                sendBroadcast(Intent("PLAY_PAUSE"))
            }

            override fun onPause() {
                sendBroadcast(Intent("PLAY_PAUSE"))
            }

            override fun onSkipToNext() {
                sendBroadcast(Intent("NEXT"))
            }

            override fun onSkipToPrevious() {
                sendBroadcast(Intent("PREVIOUS"))
            }

            override fun onSeekTo(pos: Long) {
                val seekIntent = Intent("SEEK_TO").apply {
                    putExtra("position", pos.toInt())
                }
                sendBroadcast(seekIntent)
            }
        })

        mediaSession.setPlaybackState(playbackState)

        // Construir notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(songTitle)
            .setContentText("Reproduciendo")
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_previous, "Previous", previousIntent)
            .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
            .addAction(R.drawable.ic_next, "Next", nextIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setProgress(duration, position, false)
            .build()
        startForeground(notificationId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
}