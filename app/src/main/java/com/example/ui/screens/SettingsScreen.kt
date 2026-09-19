package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.viewmodel.JarvisSettings
import com.example.ui.viewmodel.JarvisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState(initial = JarvisSettings())
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JARVIS SETTINGS", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: AI Settings
            SettingsSectionHeader("AI SETTINGS")
            SettingsItemRow(title = "AI Provider", subtitle = settings.aiProvider)
            SettingsItemRow(title = "AI Model", subtitle = settings.aiModel)
            val keyVal = try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                field.get(null) as? String ?: ""
            } catch (e: Exception) {
                ""
            }
            val apiKeyStatus = if (keyVal.isNotBlank() && keyVal != "MY_GEMINI_API_KEY") "Configured" else "Missing / Placeholder (Using Offline Mode)"
            SettingsItemRow(title = "API Key Status", subtitle = apiKeyStatus)

            HorizontalDivider()

            // Section 2: Voice Settings
            SettingsSectionHeader("VOICE SETTINGS")
            SettingsSwitchRow(
                title = "Voice Assistant",
                subtitle = "Enable speech interaction",
                checked = settings.voiceAssistantEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(voiceAssistantEnabled = it)) }
            )
            SettingsSwitchRow(
                title = "Auto-detect Language",
                subtitle = "Detect English, Nepali, Hindi",
                checked = settings.autoDetectLanguage,
                onCheckedChange = { viewModel.updateSettings(settings.copy(autoDetectLanguage = it)) }
            )
            SettingsSwitchRow(
                title = "Interrupt while Speaking",
                subtitle = "Barge-in capability",
                checked = settings.interruptWhileSpeaking,
                onCheckedChange = { viewModel.updateSettings(settings.copy(interruptWhileSpeaking = it)) }
            )

            HorizontalDivider()

            // Section 3: Wake Word Settings
            SettingsSectionHeader("WAKE WORD SETTINGS")
            SettingsSwitchRow(
                title = "Wake Word Detection",
                subtitle = "Listen for \"Hey JARVIS\"",
                checked = settings.wakeWordEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(wakeWordEnabled = it)) }
            )
            SettingsItemRow(title = "Wake Phrase", subtitle = settings.wakePhrase)

            HorizontalDivider()

            // Section 4: Proactive Settings
            SettingsSectionHeader("PROACTIVE SETTINGS")
            SettingsSwitchRow(
                title = "Self-Initiated Conversation",
                subtitle = "Allow proactive alerts and suggestions",
                checked = settings.proactiveEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(proactiveEnabled = it)) }
            )
            SettingsSwitchRow(
                title = "Quiet Mode",
                subtitle = "Silence non-essential proactive alerts",
                checked = settings.quietMode,
                onCheckedChange = { viewModel.updateSettings(settings.copy(quietMode = it)) }
            )

            HorizontalDivider()

            // Section 5: Memory Settings
            SettingsSectionHeader("MEMORY SETTINGS")
            SettingsSwitchRow(
                title = "Memory System",
                subtitle = "Remember preferences and context",
                checked = settings.memoryEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(memoryEnabled = it)) }
            )

            HorizontalDivider()

            // Section 6: Android Control Settings
            SettingsSectionHeader("ANDROID CONTROL & PERMISSIONS")
            SettingsItemRow(title = "Microphone Permission", subtitle = "Granted (Required for voice)")
            SettingsItemRow(title = "Camera Permission", subtitle = "Granted (Required for flashlight/vision)")
            SettingsItemRow(title = "Location Permission", subtitle = "Granted (Required for maps)")
            SettingsItemRow(title = "Contacts Permission", subtitle = "Granted (Required for calling)")

            HorizontalDivider()

            // Section 7: Appearance
            SettingsSectionHeader("APPEARANCE")
            SettingsItemRow(title = "Theme Mode", subtitle = settings.themeMode)
            SettingsSwitchRow(
                title = "Futuristic JARVIS Interface",
                subtitle = "Arc reactor visuals and animations",
                checked = settings.futuristicUi,
                onCheckedChange = { viewModel.updateSettings(settings.copy(futuristicUi = it)) }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsItemRow(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
