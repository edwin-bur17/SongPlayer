package com.edwdev.mediaplayermp3.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class MusicServiceConnection(private val context: Context) {
    private var musicService: MusicService? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            bound = false
        }
    }

    fun bind() {
        Intent(context, MusicService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        if (bound) {
            context.unbindService(serviceConnection)
            bound = false
        }
    }

    fun getMusicService(): MusicService? = musicService
}