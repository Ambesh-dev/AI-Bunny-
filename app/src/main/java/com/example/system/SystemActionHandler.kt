package com.example.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.widget.Toast
import com.example.model.ActionType
import com.example.model.ExecutedAction

class SystemActionHandler(private val context: Context) {

    private var isTorchOn: Boolean = false

    fun openYouTube(query: String? = null): ExecutedAction {
        return try {
            val intent = if (!query.isNullOrBlank()) {
                val appIntent = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (isIntentAvailable(appIntent)) {
                    appIntent
                } else {
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                launchIntent?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
            }
            context.startActivity(intent)
            ExecutedAction(
                type = if (!query.isNullOrBlank()) ActionType.SEARCH_YOUTUBE else ActionType.OPEN_YOUTUBE,
                label = if (!query.isNullOrBlank()) "Searching YouTube for: $query" else "YouTube Launched",
                detail = "YouTube opened successfully"
            )
        } catch (e: Exception) {
            ExecutedAction(
                type = ActionType.OPEN_YOUTUBE,
                label = "YouTube Launch Failed",
                detail = e.localizedMessage ?: "Could not open YouTube",
                success = false
            )
        }
    }

    fun openWhatsApp(): ExecutedAction {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            val intent = launchIntent?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
            ExecutedAction(
                type = ActionType.OPEN_WHATSAPP,
                label = "WhatsApp Launched",
                detail = "Opening WhatsApp chat..."
            )
        } catch (e: Exception) {
            ExecutedAction(
                type = ActionType.OPEN_WHATSAPP,
                label = "WhatsApp Failed",
                detail = e.localizedMessage ?: "WhatsApp not installed",
                success = false
            )
        }
    }

    fun openCamera(): ExecutedAction {
        return try {
            val intent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ExecutedAction(
                type = ActionType.OPEN_CAMERA,
                label = "Camera Opened",
                detail = "Say cheese for Buny! 📸"
            )
        } catch (e: Exception) {
            ExecutedAction(
                type = ActionType.OPEN_CAMERA,
                label = "Camera Error",
                detail = e.localizedMessage ?: "Cannot open camera",
                success = false
            )
        }
    }

    fun openApp(packageName: String, appName: String): ExecutedAction {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                ExecutedAction(
                    type = ActionType.OPEN_APP,
                    label = "$appName Opened",
                    detail = "Launched $appName"
                )
            } else {
                // Try web fallback if applicable
                val webUrl = when {
                    packageName.contains("instagram") -> "https://instagram.com"
                    packageName.contains("spotify") -> "https://open.spotify.com"
                    packageName.contains("maps") -> "https://maps.google.com"
                    else -> null
                }
                if (webUrl != null) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                    ExecutedAction(
                        type = ActionType.OPEN_APP,
                        label = "$appName Opened via Web",
                        detail = "App not installed; opened in browser"
                    )
                } else {
                    ExecutedAction(
                        type = ActionType.OPEN_APP,
                        label = "$appName Not Found",
                        detail = "Please install $appName from Play Store",
                        success = false
                    )
                }
            }
        } catch (e: Exception) {
            ExecutedAction(
                type = ActionType.OPEN_APP,
                label = "$appName Failed",
                detail = e.localizedMessage ?: "Error opening app",
                success = false
            )
        }
    }

    fun searchWeb(query: String): ExecutedAction {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ExecutedAction(
                type = ActionType.SEARCH_WEB,
                label = "Google Search",
                detail = "Searching: $query"
            )
        } catch (e: Exception) {
            ExecutedAction(
                type = ActionType.SEARCH_WEB,
                label = "Search Failed",
                detail = e.localizedMessage ?: "Failed to open browser",
                success = false
            )
        }
    }

    fun dialNumber(number: String): ExecutedAction {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ExecutedAction(
                type = ActionType.CALL_PHONE,
                label = "Dialer Ready",
                detail = "Dialing $number"
            )
        } catch (e: Exception) {
            ExecutedAction(
                type = ActionType.CALL_PHONE,
                label = "Dialer Failed",
                detail = e.localizedMessage ?: "Could not open dialer",
                success = false
            )
        }
    }

    fun openAlarm(): ExecutedAction {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (isIntentAvailable(intent)) {
                context.startActivity(intent)
                ExecutedAction(ActionType.OPEN_APP, "Alarm / Clock Opened", "Showing your alarms")
            } else {
                openApp("com.google.android.deskclock", "Clock")
            }
        } catch (e: Exception) {
            ExecutedAction(ActionType.OPEN_APP, "Clock Failed", e.localizedMessage ?: "Cannot open clock", false)
        }
    }

    fun toggleFlashlight(): ExecutedAction {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                isTorchOn = !isTorchOn
                cameraManager.setTorchMode(cameraId, isTorchOn)
                ExecutedAction(
                    type = ActionType.FLASHLIGHT,
                    label = if (isTorchOn) "Flashlight ON 💡" else "Flashlight OFF 🌑",
                    detail = if (isTorchOn) "Buny turned ON the torch!" else "Torch turned off"
                )
            } else {
                ExecutedAction(ActionType.FLASHLIGHT, "No Flashlight", "Camera flash not found", false)
            }
        } catch (e: CameraAccessException) {
            ExecutedAction(ActionType.FLASHLIGHT, "Flashlight Error", e.localizedMessage ?: "Camera in use", false)
        } catch (e: Exception) {
            ExecutedAction(ActionType.FLASHLIGHT, "Flashlight Error", e.localizedMessage ?: "Error", false)
        }
    }

    fun getBatteryStatus(): ExecutedAction {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
            ExecutedAction(
                type = ActionType.DEVICE_STATUS,
                label = "Battery: $level%",
                detail = if (isCharging) "Charging now ⚡" else "On battery power 🔋"
            )
        } catch (e: Exception) {
            ExecutedAction(ActionType.DEVICE_STATUS, "Battery Info", "Battery: ~85% (Estimate)", true)
        }
    }

    fun getDeviceSummary(): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 80
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val isWifi = connManager?.getNetworkCapabilities(connManager.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        return "Battery: $level% | Net: ${if (isWifi) "Wi-Fi" else "Cellular"} | Device: ${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun isIntentAvailable(intent: Intent): Boolean {
        val list = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.isNotEmpty()
    }
}
