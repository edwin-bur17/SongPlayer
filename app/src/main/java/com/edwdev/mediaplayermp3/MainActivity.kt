package com.edwdev.mediaplayermp3

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edwdev.mediaplayermp3.screens.MediaPlayerMP3Screen
import com.edwdev.mediaplayermp3.screens.MediaPlayerMP3ViewModel
import com.edwdev.mediaplayermp3.screens.SongPlayerScreen
import com.edwdev.mediaplayermp3.screens.SplashScreen

class MainActivity : ComponentActivity() {
    // ViewModel
    private val viewModel: MediaPlayerMP3ViewModel by viewModels()

    // Lanzador de permisos
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa el lanzador de permisos
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.loadSongs(this)
                } else {
                    // Manejar el caso en que el permiso no es concedido
                }
            }

        // Configura el contenido de la actividad
        setContent {
            val navController = rememberNavController()
            // Configura la navegación
            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen(navController = navController)
                }
                composable("home") {
                    MediaPlayerMP3Screen(viewModel, navController)
                }
                composable("songPlayer") {
                    SongPlayerScreen(viewModel)
                }
            }
        }
        // Verifica si el permiso ya está concedido
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            viewModel.loadSongs(this)
        }
    }
}