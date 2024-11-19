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
import androidx.core.app.NotificationCompat
import com.edwdev.mediaplayermp3.MainActivity
import com.edwdev.mediaplayermp3.R
import com.edwdev.mediaplayermp3.model.Song

class MusicService : Service() {
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private val binder = MusicBinder()
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "MusicPlayerChannel"

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
            CHANNEL_ID,
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

        //  CORREGIR ESTA PARTE *** contentIntent *** YA QUE INICIALIZA DESDE 0 LA APP LO QUE GENERA UN DESCONTROL EN EL FLUJO DE LA APP
        // Intent para abrir la actividad al tocar la notificación
//        val contentIntent = PendingIntent.getActivity(
//            this,
//            0,
//            Intent(this, MainActivity::class.java),
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

        // Actualizar metadata
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration.toLong())
            .build()
        mediaSession.setMetadata(metadata)


        // CUANDO ESTA ACTIVADA ESTA FUNCIÓN *** playbackState *** ME FUNCIONA EL SLIDER EN LA NOTIIFICACIÓN
        // PERO CUANDO LO COMENTO ME MUESTRA LOS BOTONES DE ANTERIOR, PLAY/PAUSE Y SIGUIENTE PERO NO APARECE EL SLIDER DE PROGRESO
        // REVISAR ESTA PARTE
        // Actualizar estado de reproducción
//        val playbackState = PlaybackStateCompat.Builder()
//            .setState(
//                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
//                position.toLong(),
//                1f
//            )
//            .build()
//        mediaSession.setPlaybackState(playbackState)

        // Construir notificación
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song.title)
            .setContentText("Reproduciendo")
            //.setContentIntent(contentIntent)
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
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
}