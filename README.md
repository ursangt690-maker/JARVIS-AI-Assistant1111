# JARVIS Android Assistant — Advanced Build

Native Kotlin + Jetpack Compose Android assistant.

## Included

- Gemini REST chat with runtime API-key configuration
- API key encrypted at rest with Android Keystore
- Android SpeechRecognizer voice input
- TextToSpeech voice output with speed/volume controls
- English / Nepali / Hindi routing
- Persistent Room chat history and user memories
- Recent conversation + saved-memory context sent to Gemini
- Device actions: battery, device info, flashlight, app launch, alarms, contact dialer, WhatsApp/email preparation, web search
- Optional foreground voice service that repeatedly listens for “Hey JARVIS”
- Home-screen JARVIS widget for one-tap opening
- Permission status screen and least-privilege startup permissions
- GitHub Actions debug APK build

## Important wake-word note

The wake-word feature uses Android `SpeechRecognizer` in a foreground service. It is **not** a dedicated low-power hardware hotword engine. Recognition can be interrupted or throttled by Android/OEM battery policies, and continuous recognition uses more battery.

Enable it in **Settings → Wake word**. Android will show a persistent foreground-service notification while it is active.

## API-key security

The key is encrypted using an Android Keystore AES-GCM key before being stored locally. A client-side API key can still be extracted from a compromised/rooted device or reverse-engineered app, so a production commercial app should use a server-side proxy or short-lived credential/token flow.

## Build

Use Android Studio or Gradle:

`./gradlew :app:assembleDebug`

`./gradlew :app:testDebugUnitTest`

The repository also includes a GitHub Actions workflow that installs Gradle and builds the debug APK.

## Android permissions

The app no longer requests contacts/camera/etc. automatically at first launch. Microphone is required for voice input; contacts/camera/notifications can be granted when needed.

## Home screen

After installing the APK, long-press the Android home screen → Widgets → JARVIS → add the widget. Tapping it opens JARVIS directly.


## Advanced core additions (v4.0)

The existing architecture was preserved and the assistant core was extended with:

- System status: RAM, battery and storage
- Date/time commands
- Media volume up/down and percentage control
- Wi-Fi/Bluetooth/system settings shortcuts
- Camera launch
- Maps place search
- YouTube search
- Direct dialer by number
- Explicit "remember ..." memory command
- Clear-chat command
- More English/Nepali command aliases
- Safer WhatsApp phone-number validation and email validation
- No automatic microphone/notification permission request at first app launch
- Fixed the voice-language invocation bug
- Fixed Quick Actions handling of `Result<String>`
- Improved wake-word permission flow
- More truthful action-result handling: JARVIS reports an action only after the Android intent/action succeeds
