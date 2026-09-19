package com.zoya.assistant

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zoya.assistant.ui.screens.ChatScreen
import com.zoya.assistant.ui.screens.MemoryScreen
import com.zoya.assistant.ui.screens.QuickActionsScreen
import com.zoya.assistant.ui.screens.SettingsScreen
import com.zoya.assistant.ui.theme.MyApplicationTheme
import com.zoya.assistant.ui.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    private val vm: JarvisViewModel by viewModels()
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        permissionLauncher.launch(permissions.toTypedArray())
        setContent {
            MyApplicationTheme {
                Surface(Modifier.fillMaxSize(), color=MaterialTheme.colorScheme.background) {
                    val nav=rememberNavController()
                    NavHost(nav, "chat") {
                        composable("chat"){ ChatScreen(vm,{nav.navigate("settings")},{nav.navigate("memory")},{nav.navigate("quick_actions")}) }
                        composable("settings"){ SettingsScreen(vm){nav.popBackStack()} }
                        composable("memory"){ MemoryScreen(vm){nav.popBackStack()} }
                        composable("quick_actions"){ QuickActionsScreen{nav.popBackStack()} }
                    }
                }
            }
        }
    }
}
