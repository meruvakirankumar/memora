package com.meruvakirankumar.memora.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.meruvakirankumar.memora.model.MemoryCandidate
import com.meruvakirankumar.memora.logic.MemoryStatusResolver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Schedules a local reminder the day before an action date at 9am. */
class ReminderScheduler(private val context: Context) {

    fun schedule(candidate: MemoryCandidate): Int? {
        val actionDate = LocalDate.parse(candidate.dateIso)
        val reminderAt = actionDate.minusDays(1).atTime(9, 0)
        if (!reminderAt.isAfter(LocalDateTime.now())) {
            return null
        }

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val triggerAtMillis = reminderAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ReminderReceiver.EXTRA_TITLE, "${candidate.title} needs attention")
            putExtra(
                ReminderReceiver.EXTRA_BODY,
                "${candidate.eventType.label()} is coming up on ${MemoryStatusResolver.formatDate(candidate.dateIso)}.",
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
        return notificationId
    }

    fun cancel(notificationId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
