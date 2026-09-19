package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.QuickActionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    private val jarvisViewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "chat") {
                        composable("chat") {
                            ChatScreen(
                                viewModel = jarvisViewModel,
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToMemory = { navController.navigate("memory") },
                                onNavigateToQuickActions = { navController.navigate("quick_actions") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = jarvisViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("memory") {
                            MemoryScreen(
                                viewModel = jarvisViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("quick_actions") {
                            QuickActionsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
