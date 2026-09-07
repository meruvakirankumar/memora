package com.meruvakirankumar.memora.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Memora reminder"
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""

        NotificationChannels.ensureCreated(context)

        val notification: Notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notificationId"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }
}
