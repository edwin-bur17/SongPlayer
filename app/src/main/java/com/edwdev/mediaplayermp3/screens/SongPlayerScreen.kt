package com.edwdev.mediaplayermp3.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwdev.mediaplayermp3.R
import com.edwdev.mediaplayermp3.model.Song
import com.edwdev.mediaplayermp3.ui.theme.PrimaryColor
import com.edwdev.mediaplayermp3.ui.theme.SecondaryColor
import com.edwdev.mediaplayermp3.ui.theme.TertiaryColor
import com.edwdev.mediaplayermp3.ui.theme.WhiteColor
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SongPlayerScreen(viewModel: MediaPlayerMP3ViewModel) {
    val currentSong by viewModel.currentSong.observeAsState()
    val isRandomMode by viewModel.isRandomMode.collectAsState()

    // Actualizar la posición del audio en tiempo real
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updatePosition()
            delay(100)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(PrimaryColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderImage()
        Spacer(modifier = Modifier.height(8.dp))
        SongTitle(currentSong = currentSong)
        Spacer(modifier = Modifier.height(8.dp))
        AudioSlider(viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))
        NavigationButtons(
            viewModel = viewModel,
            currentSong = currentSong,
            isRandomMode = isRandomMode
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun AudioSlider(viewModel: MediaPlayerMP3ViewModel) {
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = currentPosition.toFloat(),
            onValueChange = {
                viewModel.seekTo(it.toInt())
            },
            valueRange = 0f..duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = TertiaryColor,
                activeTrackColor = TertiaryColor,
                inactiveTrackColor = TertiaryColor.copy(alpha = 0.5f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format(
                    "%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(currentPosition.toLong()) % 60
                ),
                color = WhiteColor,
                fontSize = 12.sp
            )
            Text(
                text = String.format(
                    "%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
                ),
                color = WhiteColor,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SongTitle(currentSong: Song?) {
    val songTitle = currentSong?.title?.substringBefore(".")
    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = songTitle ?: "No hay canción seleccionada",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = WhiteColor
    )
}

@Composable
fun NavigationButtons(viewModel: MediaPlayerMP3ViewModel, currentSong: Song?, isRandomMode: Boolean) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Botón Reiniciar audio
        IconButton(
            onClick = { viewModel.restartCurrentSong() },
            enabled = currentSong != null,
        ) {
            Icon(
                modifier = Modifier.size(55.dp),
                painter = painterResource(R.drawable.ic_replay),
                contentDescription = "replay",
                tint = SecondaryColor
            )
        }

        // Botón anterior
        IconButton(
            onClick = { viewModel.playPreviousSong() },
            enabled = currentSong != null,
        ) {
            Icon(
                modifier = Modifier.size(55.dp),
                painter = painterResource(R.drawable.ic_previous),
                contentDescription = "previous",
                tint = SecondaryColor
            )
        }

        // Botón Play/Pause
        val isPlaying = viewModel.isPlaying.collectAsState().value
        IconButton(
            onClick = {
                if (isPlaying) {
                    viewModel.pauseSong()
                } else {
                    viewModel.resumeSong()
                }
            },
            enabled = currentSong != null,
        ) {
            Icon(
                modifier = Modifier.size(55.dp),
                painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = if (isPlaying) "pause" else "play",
                tint = SecondaryColor
            )
        }

        // Botón siguiente
        IconButton(
            onClick = { viewModel.playNextSong() },
            enabled = currentSong != null,
        ) {
            Icon(
                modifier = Modifier.size(55.dp),
                painter = painterResource(R.drawable.ic_next),
                contentDescription = "next",
                tint = SecondaryColor
            )
        }

        // Modo aleatorio
        IconButton(
            onClick = { viewModel.toggleRandomMode() },
            enabled = true,
        ) {
            Icon(
                modifier = Modifier.size(55.dp),
                painter = painterResource(R.drawable.ic_random),
                contentDescription = "ramdom_mode",
                tint = if (isRandomMode) SecondaryColor else Color.Gray
            )
        }
    }
}

@Composable
fun HeaderImage() {
    Image(
        painter = painterResource(id = R.drawable.icono),
        contentDescription = "sound_icon",
        modifier = Modifier.size(400.dp)
    )
}