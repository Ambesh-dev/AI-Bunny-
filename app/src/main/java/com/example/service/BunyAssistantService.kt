package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.system.SystemActionHandler

class BunyAssistantService : Service() {

    companion object {
        const val CHANNEL_ID = "buny_assistant_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.buny.START"
        const val ACTION_STOP = "com.example.buny.STOP"
        const val ACTION_YOUTUBE = "com.example.buny.YOUTUBE"
        const val ACTION_TORCH = "com.example.buny.TORCH"

        var isServiceRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, BunyAssistantService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BunyAssistantService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var systemActionHandler: SystemActionHandler

    override fun onCreate() {
        super.onCreate()
        systemActionHandler = SystemActionHandler(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                isServiceRunning = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_YOUTUBE -> {
                systemActionHandler.openYouTube()
            }
            ACTION_TORCH -> {
                systemActionHandler.toggleFlashlight()
            }
            else -> {
                isServiceRunning = true
                val notification = buildNotification()
                startForeground(NOTIFICATION_ID, notification)
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ytIntent = Intent(this, BunyAssistantService::class.java).apply {
            action = ACTION_YOUTUBE
        }
        val ytPendingIntent = PendingIntent.getService(
            this,
            1,
            ytIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val torchIntent = Intent(this, BunyAssistantService::class.java).apply {
            action = ACTION_TORCH
        }
        val torchPendingIntent = PendingIntent.getService(
            this,
            2,
            torchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Buny AI is Active 🐰⚡")
            .setContentText("Your personal AI assistant is running in background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_play, "Open YouTube", ytPendingIntent)
            .addAction(android.R.drawable.ic_menu_camera, "Torch", torchPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Buny Background Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Buny AI active for quick phone actions"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
