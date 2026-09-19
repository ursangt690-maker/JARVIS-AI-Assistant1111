# JARVIS Project Audit

## Major repairs in this regeneration
- Replaced generic `com.example` namespace/application ID with `com.zoya.assistant`.
- Removed fragile release/debug keystore assumptions from the build configuration.
- Added runtime API-key configuration in Settings instead of reading a hard-coded BuildConfig secret.
- Added real Android SpeechRecognizer input and TextToSpeech output.
- Added explicit assistant states and removed the fake microphone button that sent a battery question.
- Added actual permission-state reporting and permission requests.
- Added app launching, contact dialing, WhatsApp message preparation, Gmail/email preparation, flashlight, battery, device info, alarm and web-search helpers.
- Added persistent settings storage.
- Kept Room memory/chat storage.
- Added a canonical JARVIS prompt hook.
- Added a build-oriented project structure and removed obsolete unused Gradle plugins.

## Important technical limitation
This regeneration provides a working Android voice input/output path and Gemini REST text generation with a runtime key.
A true Gemini Live bidirectional audio session and offline wake-word engine require the exact current supported
Gemini Live Android transport/SDK and a production-safe credential/token architecture. No fake Live implementation
has been inserted. The wake-word switch is therefore conservative and must not claim that continuous wake-word
detection is active until a real wake-word engine is integrated.

## Build
Run:
`./gradlew :app:testDebugUnitTest --stacktrace`
`./gradlew :app:assembleDebug --stacktrace`

Expected APK:
`app/build/outputs/apk/debug/app-debug.apk`

## Security
Never commit a real API key to GitHub. Configure it at runtime in JARVIS Settings.
