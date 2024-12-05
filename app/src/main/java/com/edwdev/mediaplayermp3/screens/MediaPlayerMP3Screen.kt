package com.edwdev.mediaplayermp3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import com.edwdev.mediaplayermp3.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayerMP3Screen(viewModel: MediaPlayerMP3ViewModel, navController: NavHostController) {
    val songs by viewModel.songs.observeAsState(emptyList())
    val currentSong by viewModel.currentSong.observeAsState()
    val isRandomMode by viewModel.isRandomMode.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Song Player", color = WhiteColor)
                        Text(text = "${songs.size} canciones", color = WhiteColor, fontSize = 14.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VariantPrimaryColor,
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleRandomMode() }) {
                        Icon(
                            modifier = Modifier.size(35.dp),
                            painter = painterResource(R.drawable.ic_random),
                            contentDescription = "Random mode",
                            tint = if (isRandomMode) TertiaryColor else Color.LightGray
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VariantPrimaryColor)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(PrimaryColor)
                    .padding(horizontal = 16.dp)
            ) {
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

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 0.dp)
                    .height(55.dp)
                    .fillMaxWidth()
                    .background(VariantPrimaryColor),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val songTitle = currentSong?.title?.substringBefore(".")
                Text(
                    text = songTitle ?: "Seleccione una canción",
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 0.dp).weight(0.85f),
                    color = TertiaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Botón Play/pause
                val isPlaying = viewModel.isPlaying.collectAsState().value

                IconButton(
                    onClick = {
                        if (isPlaying) {
                            viewModel.pauseSong()
                        } else {
                            viewModel.resumeSong()
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(35.dp),
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = if (isPlaying) "pausar" else "reanudar",
                        tint = Color.White
                    )
                }

                // Botón siguiente
                IconButton(
                    onClick = { viewModel.playNextSong() },
                    enabled = songs.isNotEmpty(),
                ) {
                    Icon(
                        modifier = Modifier.size(35.dp),
                        painter = painterResource(R.drawable.ic_next),
                        contentDescription = "siguiente",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SongItem(song: Song, isSelected: Boolean, onSongClick: () -> Unit) {
    val songTitle = song.title.substringBeforeLast(".")
    Row(
        modifier = Modifier.clickable { onSongClick() }.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = "Song Icon",
            tint = if (isSelected) TertiaryColor else Color.LightGray,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = songTitle,
            color = if (isSelected) SecondaryColor else Color.LightGray,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}