# JARVIS Project Audit — Advanced Repair

## Fixed in this build

1. **Duplicate Compose source tree**
   - Removed the duplicated `ui/ui/...` source tree.
   - Consolidated screens, components, theme and ViewModel under the canonical `ui/...` packages.
   - Fixed package/import path mismatches that could cause duplicate declarations and unresolved imports.

2. **Permission flow**
   - Startup no longer asks for contacts/camera/phone permissions unnecessarily.
   - Microphone is requested for voice input.
   - Notification permission is requested on supported Android versions.
   - Contacts/camera are left for features that actually need them.

3. **API-key storage**
   - Replaced plaintext SharedPreferences storage with Android Keystore AES-GCM encryption.
   - The UI can still save/clear the key without committing it to source control.
   - A production service should still use a server-side credential architecture.

4. **Wake-word/background mode**
   - Added an optional foreground microphone service.
   - It repeatedly runs short SpeechRecognizer sessions and detects “Hey JARVIS”.
   - It shows a persistent notification while active.
   - Pending commands are retained briefly so a command is not lost when the activity is temporarily stopped.
   - This is explicitly not marketed as a hardware-level hotword engine.

5. **Home-screen shortcut**
   - Added an Android AppWidgetProvider and widget layout.
   - One tap opens JARVIS.

6. **Settings persistence**
   - Speech speed, volume, interrupt mode, wake phrase, UI preference and other toggles now persist.

7. **Assistant context**
   - Recent chat messages and enabled saved memories are included in the Gemini prompt.
   - The assistant prompt continues to prohibit claiming an Android action was completed unless it actually succeeded.

8. **Command handling**
   - Added/strengthened battery, flashlight, device info, app launch, web search, alarm, contact dialer, WhatsApp preparation, email preparation and stop-speaking commands.
   - Alarm parsing supports examples such as `7:30 AM`.

## Known platform limitations

- Android may stop/restrict long-running microphone recognition, especially under aggressive OEM battery management.
- SpeechRecognizer is not a true low-power hotword engine.
- Background activity launches are restricted by modern Android versions. The service stores a pending command and the foreground app consumes it when available.
- Gemini requires a valid API key and network connection for online AI responses.
- No fake Gemini Live implementation is claimed.

## Build verification

The source was statically reviewed and repaired. A local Android Gradle build could not be executed in this environment because a Gradle executable/wrapper distribution was unavailable. Use Android Studio or the included GitHub Actions workflow for the authoritative compile/test result.


## v4.0 fixes and core features

### Code fixes
- Fixed the invalid `languageTag(...)` call in `JarvisViewModel`; the selected language tag is now passed directly to `VoiceManager`.
- Removed the automatic first-launch microphone/notification permission request from `MainActivity`; permissions are requested when the related feature is enabled.
- Fixed `QuickActionsScreen` treating a Kotlin `Result<String>` as a Boolean.
- Improved wake-word permission flow so enabling the feature requests the microphone and supported notification permission, then starts the service when microphone access is available.
- Improved action result handling so intent failures are surfaced instead of being reported as successful actions.

### New assistant-core commands
- `time`, `date`
- `system status`, `storage`
- `volume up`, `volume down`, `volume 50`
- `open settings`, `wifi settings`, `bluetooth settings`
- `camera`
- `maps <place>`
- `youtube <query>`
- `dial <number>`
- `remember <note>`
- `clear chat`
- Expanded Nepali aliases for common commands.

### Verification note
The project was statically inspected and the identified source-level issues were repaired. A Gradle/Android SDK toolchain is not available in this execution environment, so an authoritative APK compilation still needs to be run in Android Studio or CI.
