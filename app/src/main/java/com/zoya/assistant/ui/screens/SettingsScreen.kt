package com.zoya.assistant.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.zoya.assistant.ui.viewmodel.JarvisViewModel
import com.zoya.assistant.utils.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: JarvisViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val s by viewModel.settings.collectAsState()
    val key by viewModel.apiKey.collectAsState()
    var keyInput by remember(key) { mutableStateOf(key) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    Scaffold(topBar = {
        TopAppBar(title={Text("JARVIS SETTINGS")}, navigationIcon={
            IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription="Back")}
        })
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("AI CONFIGURATION", style=MaterialTheme.typography.titleSmall)
            OutlinedTextField(keyInput,{keyInput=it},Modifier.fillMaxWidth(),label={Text("Gemini API key")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(onClick={viewModel.setApiKey(keyInput)}) { Text("Save") }
                OutlinedButton(onClick={viewModel.clearApiKey()}) { Text("Clear") }
            }
            Text(if (key.isBlank()) "API key: Not configured" else "API key: Configured", color=if(key.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            OutlinedTextField(s.aiModel,{viewModel.updateSettings(s.copy(aiModel=it))},Modifier.fillMaxWidth(),label={Text("Gemini model")},singleLine=true)

            HorizontalDivider()
            Text("VOICE", style=MaterialTheme.typography.titleSmall)
            SettingSwitch("Voice assistant", "Speak responses aloud", s.voiceAssistantEnabled) { viewModel.updateSettings(s.copy(voiceAssistantEnabled=it)) }
            SettingSwitch("Interrupt while speaking", "Stop current speech when you start a new request", s.interruptWhileSpeaking) { viewModel.updateSettings(s.copy(interruptWhileSpeaking=it)) }

            HorizontalDivider()
            Text("WAKE WORD", style=MaterialTheme.typography.titleSmall)
            SettingSwitch("Wake word", "Enable only when a real wake-word engine is available", s.wakeWordEnabled) { viewModel.updateSettings(s.copy(wakeWordEnabled=it)) }
            Text("Phrase: ${s.wakePhrase}")

            HorizontalDivider()
            Text("PROACTIVE", style=MaterialTheme.typography.titleSmall)
            SettingSwitch("Self-initiated conversation", "Off by default; prevents unexpected speech", s.proactiveEnabled) { viewModel.updateSettings(s.copy(proactiveEnabled=it)) }
            SettingSwitch("Quiet mode", "Suppress unnecessary proactive behavior", s.quietMode) { viewModel.updateSettings(s.copy(quietMode=it)) }

            HorizontalDivider()
            Text("MEMORY", style=MaterialTheme.typography.titleSmall)
            SettingSwitch("Memory", "Allow JARVIS to store user-created memories", s.memoryEnabled) { viewModel.updateSettings(s.copy(memoryEnabled=it)) }

            HorizontalDivider()
            Text("ANDROID PERMISSIONS", style=MaterialTheme.typography.titleSmall)
            PermissionRow("Microphone", PermissionHelper.microphoneGranted(context))
            PermissionRow("Contacts", PermissionHelper.contactsGranted(context))
            PermissionRow("Phone", PermissionHelper.callGranted(context))
            PermissionRow("Camera", PermissionHelper.cameraGranted(context))
            PermissionRow("Notifications", PermissionHelper.notificationsGranted(context))
            Button(onClick={
                val permissions = buildList {
                    add(Manifest.permission.RECORD_AUDIO)
                    add(Manifest.permission.READ_CONTACTS)
                    add(Manifest.permission.CALL_PHONE)
                    add(Manifest.permission.CAMERA)
                    if (android.os.Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }) { Text("Request permissions") }
            OutlinedButton(onClick={context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))}) { Text("Open Android App Settings") }

            HorizontalDivider()
            Text("APPEARANCE", style=MaterialTheme.typography.titleSmall)
            SettingSwitch("JARVIS interface", "Use the futuristic assistant visualizer", s.futuristicUi) { viewModel.updateSettings(s.copy(futuristicUi=it)) }
        }
    }
}
@Composable private fun SettingSwitch(title:String, subtitle:String, checked:Boolean, onChecked:(Boolean)->Unit){
    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(title);Text(subtitle,style=MaterialTheme.typography.bodySmall)};Switch(checked,onChecked)}
}
@Composable private fun PermissionRow(name:String, granted:Boolean){
    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){Text(name);Text(if(granted) "Granted" else "Not granted", color=if(granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}
}
