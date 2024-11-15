package com.edwdev.mediaplayermp3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.edwdev.mediaplayermp3.ui.theme.PrimaryColor
import com.edwdev.mediaplayermp3.ui.theme.WhiteColor
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect (key1 = true) {
        delay(2500)
        navController.navigate("home"){
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    Box (
        modifier = Modifier.fillMaxSize().background(PrimaryColor),
    )
    {
        Text(text = "Song Player", fontSize = 40.sp, color = WhiteColor)
    }
}