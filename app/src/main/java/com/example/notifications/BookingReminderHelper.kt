package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.MatchEntity
import com.example.ui.util.formatArabicDate

object BookingReminderHelper {
  const val CHANNEL_ID = "malabna_booking_reminders"
  const val EXTRA_MATCH_ID = "extra_match_id"
  const val EXTRA_TITLE = "extra_title"
  const val EXTRA_PITCH = "extra_pitch"
  const val EXTRA_TIME = "extra_time"
  const val EXTRA_DATE_MILLIS = "extra_date_millis"

  fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      if (manager.getNotificationChannel(CHANNEL_ID) == null) {
        val channel =
          NotificationChannel(
            CHANNEL_ID,
            "تذكير حجوزات الملاعب",
            NotificationManager.IMPORTANCE_HIGH,
          ).apply {
            description = "تنبيهات مواعيد مباريات كرة القدم وتأكيد الحضور"
          }
        manager.createNotificationChannel(channel)
      }
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun scheduleReminder(context: Context, match: MatchEntity, minutesBefore: Int?) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent =
      Intent(context, BookingReminderReceiver::class.java).apply {
        putExtra(EXTRA_MATCH_ID, match.id)
        putExtra(EXTRA_TITLE, match.title)
        putExtra(EXTRA_PITCH, match.pitchName)
        putExtra(EXTRA_TIME, match.timeLabel)
        putExtra(EXTRA_DATE_MILLIS, match.dateMillis)
      }
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        match.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    if (minutesBefore == null || match.isCompleted) {
      alarmManager.cancel(pendingIntent)
      return
    }

    val triggerAt = match.dateMillis - minutesBefore * 60_000L
    val effectiveTrigger =
      if (triggerAt > System.currentTimeMillis()) triggerAt
      else System.currentTimeMillis() + 5_000L

    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, effectiveTrigger, pendingIntent)
  }

  fun showBookingNotification(
    context: Context,
    matchId: Long,
    title: String,
    pitchName: String,
    timeLabel: String,
    dateMillis: Long,
  ) {
    if (!hasNotificationPermission(context)) return
    ensureNotificationChannel(context)

    val openIntent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(EXTRA_MATCH_ID, matchId)
      }
    val contentPendingIntent =
      PendingIntent.getActivity(
        context,
        matchId.toInt(),
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val dateStr = formatArabicDate(dateMillis)
    val body = "ملعب $pitchName • $dateStr ($timeLabel) — اضغط لمراجعة الحضور وتأكيد الدفع"

    val notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("⚽ تذكير حجز: $title")
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .build()

    NotificationManagerCompat.from(context).notify(matchId.toInt(), notification)
  }

  fun buildSmsIntent(phoneNumbers: List<String>, match: MatchEntity): Intent {
    val cleanPhones = phoneNumbers.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    val recipientUri =
      if (cleanPhones.isNotEmpty()) {
        Uri.parse("smsto:${cleanPhones.joinToString(";")}")
      } else {
        Uri.parse("smsto:")
      }
    val dateStr = formatArabicDate(match.dateMillis)
    val message =
      "أهلاً يا شباب ⚽ تذكير بحجز (${match.title}) في ملعب ${match.pitchName} يوم $dateStr الساعة ${match.timeLabel}. ياريت تأكدوا الحضور قبل الماتش!"
    return Intent(Intent.ACTION_SENDTO, recipientUri).apply {
      putExtra("sms_body", message)
    }
  }
}
