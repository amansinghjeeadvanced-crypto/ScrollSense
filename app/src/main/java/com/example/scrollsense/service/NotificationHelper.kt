package com.example.scrollsense.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.scrollsense.MainActivity
import com.example.scrollsense.domain.model.ProtectedApp

object NotificationHelper {

    const val CHANNEL_ID_ALERTS = "scrollsense_attention_alerts"
    private const val CHANNEL_NAME = "Attention & Limit Alerts"
    private const val NOTIFICATION_ID_WARNING = 1001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when approaching or reaching daily digital wellness limits"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showLimitWarningNotification(
        context: Context,
        app: ProtectedApp,
        usedMinutes: Int,
        limitMinutes: Int
    ) {
        createNotificationChannels(context)

        val remaining = (limitMinutes - usedMinutes).coerceAtLeast(0)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Approaching limit: ${app.displayName}")
            .setContentText("You've used $usedMinutes of $limitMinutes min ($remaining min remaining today).")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You are getting close to your daily limit for ${app.displayName}. You have used $usedMinutes min of your $limitMinutes min allowance, with $remaining min remaining.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_WARNING + app.ordinal, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted
        }
    }
}
