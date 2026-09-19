package com.zoya.assistant.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import java.util.Locale

object AndroidActionsHelper {
    fun getBatteryLevel(context: Context): String {
        val status = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = status?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = status?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) "You currently have ${(level * 100f / scale).toInt()} percent battery."
        else "I couldn't read the battery status."
    }

    fun getDeviceInfo(): String =
        "Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}."

    fun openApp(context: Context, packageName: String): Result<String> = runCatching {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
            ?: error("That app is not installed or has no launchable activity.")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        "I opened the app."
    }

    fun openAppByName(context: Context, appName: String): Result<String> {
        val pm = context.packageManager
        val matches = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .firstOrNull { pm.getApplicationLabel(it).toString().equals(appName, true) }
        return if (matches != null) openApp(context, matches.packageName)
        else Result.failure(IllegalArgumentException("I couldn't find an installed app named $appName."))
    }

    fun toggleFlashlight(context: Context, turnOn: Boolean): String = try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return "Flashlight control requires Android 6 or newer."
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = manager.cameraIdList.firstOrNull() ?: return "No camera torch is available."
        manager.setTorchMode(id, turnOn)
        if (turnOn) "I turned on the flashlight." else "I turned off the flashlight."
    } catch (e: Exception) { "I couldn't change the flashlight: ${e.message ?: "unknown error"}" }

    fun setAlarm(context: Context, hour: Int, minute: Int, message: String = "JARVIS alarm"): Result<String> = runCatching {
        context.startActivity(Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        "I opened the alarm screen for ${String.format(Locale.US, "%02d:%02d", hour, minute)}."
    }

    fun dial(context: Context, number: String): Result<String> = runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        "I opened the Phone app for $number."
    }

    fun searchAndCallContact(context: Context, name: String): Result<String> {
        if (!PermissionHelper.contactsGranted(context)) return Result.failure(SecurityException("Contacts permission is required."))
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"),
            null
        )?.use { c ->
            if (c.moveToFirst()) {
                val number = c.getString(0)
                return dial(context, number)
            }
        }
        return Result.failure(IllegalArgumentException("I couldn't find a contact named $name."))
    }

    fun sendWhatsAppMessage(context: Context, phone: String, message: String): Result<String> = runCatching {
        val uri = Uri.parse("https://wa.me/${phone.filter { it.isDigit() }}?text=${Uri.encode(message)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        "I opened WhatsApp with the message prepared. WhatsApp may require you to confirm sending."
    }

    fun sendGmail(context: Context, email: String, subject: String, body: String): Result<String> = runCatching {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${Uri.encode(email)}")).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        "I opened Gmail or your default mail app with the email prepared."
    }

    fun performWebSearch(context: Context, query: String): Result<String> = runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        "I opened a web search for $query."
    }
}
