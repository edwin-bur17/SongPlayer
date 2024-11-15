package com.edwdev.mediaplayermp3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.edwdev.mediaplayermp3.model.Song
import com.edwdev.mediaplayermp3.ui.theme.PrimaryColor
import com.edwdev.mediaplayermp3.ui.theme.SecondaryColor
import com.edwdev.mediaplayermp3.ui.theme.TertiaryColor
import com.edwdev.mediaplayermp3.ui.theme.VariantPrimaryColor
import com.edwdev.mediaplayermp3.ui.theme.WhiteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayerMP3Screen(viewModel: MediaPlayerMP3ViewModel, navController: NavHostController) {
    val songs by viewModel.songs.observeAsState(emptyList())
    val currentSong by viewModel.currentSong.observeAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Song Player") },
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VariantPrimaryColor,
                    titleContentColor = WhiteColor
                )
            )
        },
        bottomBar = {
            SongMenuBar(
                viewModel = viewModel,
                currentSong = currentSong,
                songs = songs
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VariantPrimaryColor)
        ) { // Aplica el padding aquí
            LazyColumn(modifier = Modifier.weight(1f).background(PrimaryColor).padding(horizontal = 16.dp)) {
                items(songs) { song ->
                    SongItem(song, viewModel.currentSong.value == song) {
                        if (song == viewModel.currentSong.value && viewModel.mediaPlayer?.isPlaying == true) {
                            navController.navigate("songPlayer")
                        } else {
                            viewModel.currentSong.value = song
                            viewModel.playSong(song)
                            navController.navigate("songPlayer")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongMenuBar(viewModel: MediaPlayerMP3ViewModel, currentSong: Song?, songs: List<Song>) {
    Row(
        modifier = Modifier
            //.padding(horizontal = 16.dp, vertical = 1.dp)
            // .height(55.dp)
            .fillMaxWidth()
            .background(VariantPrimaryColor),
    ) {
        Text(
            text = currentSong?.title ?: "Seleccione una canción",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(fraction = 0.6f),
            color = TertiaryColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Botón Play/pause
        val isPlaying = viewModel.isPlaying.collectAsState().value
        Button(
            onClick = {
                if (isPlaying) {
                    viewModel.pauseSong()
                } else {
                    viewModel.resumeSong()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            )
            // enabled = currentSong != null
        ) {
            //Text(if (isPlaying) "Pausar" else "Reanudar")
            Icon(
                modifier = Modifier.size(35.dp),
                //   modifier = Modifier.padding(4.dp),
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "pausar" else "reanudar"
            )
        }
        //  Spacer(modifier = Modifier.width(8.dp)) // Espacio entre botones

        Button(
            onClick = { viewModel.playNextSong() },
            enabled = songs.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            )
        ) {
            //Text("Siguiente")
            Icon(
                modifier = Modifier.size(35.dp),
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "siguiente"
            )
        }
    }
}

@Composable
fun SongItem(song: Song, isSelected: Boolean, onSongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onSongClick() }
            .fillMaxWidth(),
        // .background(if (isSelected) Color.LightGray else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = "Song Icon",
            tint = if (isSelected) TertiaryColor else Color.LightGray,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = song.title,
            color = if (isSelected) SecondaryColor else WhiteColor,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}