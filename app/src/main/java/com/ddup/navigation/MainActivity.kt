package com.ddup.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ddup.navigation.data.model.Location
import com.ddup.navigation.data.model.NavigationSummary
import com.ddup.navigation.ui.home.HomeScreen
import com.ddup.navigation.ui.navigation.NavigationScreen
import com.ddup.navigation.ui.summary.SummaryScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
            var startLocation by remember { mutableStateOf<Location?>(null) }
            var destLocation by remember { mutableStateOf<Location?>(null) }
            var navigationSummary by remember { mutableStateOf<NavigationSummary?>(null) }

            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    onStartNavigation = { start, destination ->
                        startLocation = start
                        destLocation = destination
                        currentScreen = Screen.Navigation
                    }
                )

                Screen.Navigation -> {
                    if (startLocation != null && destLocation != null) {
                        NavigationScreen(
                            startLat = startLocation!!.latitude,
                            startLng = startLocation!!.longitude,
                            destLat = destLocation!!.latitude,
                            destLng = destLocation!!.longitude,
                            onNavigationComplete = { summary ->
                                navigationSummary = summary
                                currentScreen = Screen.Summary
                            },
                            onBackToMap = {
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }

                Screen.Summary -> {
                    navigationSummary?.let { summary ->
                        SummaryScreen(
                            summary = summary,
                            onBackToMap = {
                                currentScreen = Screen.Home
                                navigationSummary = null
                            }
                        )
                    }
                }

            }
        }
    }
}

sealed class Screen {
    data object Home : Screen()
    data object Navigation : Screen()
    data object Summary : Screen()
}