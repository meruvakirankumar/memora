package com.meruvakirankumar.memora.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {

    const val REMINDERS = "memora-reminders"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(REMINDERS) == null) {
            val channel = NotificationChannel(
                REMINDERS,
                "Memora Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Reminds you to act on your memories at the right time."
            }
            manager.createNotificationChannel(channel)
        }
    }
}
