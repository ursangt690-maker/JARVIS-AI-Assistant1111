# JARVIS Android Assistant

Native Kotlin + Jetpack Compose Android assistant.

## Current implemented path
- Runtime Gemini API-key configuration in Settings
- Android SpeechRecognizer voice input
- Android TextToSpeech voice output
- English/Nepali/Hindi language routing
- Room chat and memory
- Android actions: app launch, battery, flashlight, device info, alarms, calls, WhatsApp/email preparation, web search
- Real permission status display
- GitHub Actions APK build

## Build
`gradle :app:assembleDebug --stacktrace`

APK:
`app/build/outputs/apk/debug/app-debug.apk`

## Security
Do not commit API keys. Enter the key at runtime in JARVIS Settings.

## Live voice
The project deliberately does not fake Gemini Live. A true bidirectional Gemini Live session should be integrated using the currently supported Android Live transport/SDK and secure credential flow before claiming Live functionality.
