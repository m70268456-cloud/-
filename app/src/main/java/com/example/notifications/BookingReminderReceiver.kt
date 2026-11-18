package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BookingReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val matchId = intent.getLongExtra(BookingReminderHelper.EXTRA_MATCH_ID, -1L)
    if (matchId <= 0L) return
    val title = intent.getStringExtra(BookingReminderHelper.EXTRA_TITLE) ?: "مباراة كرة قدم"
    val pitchName = intent.getStringExtra(BookingReminderHelper.EXTRA_PITCH) ?: "الملعب"
    val timeLabel = intent.getStringExtra(BookingReminderHelper.EXTRA_TIME) ?: ""
    val dateMillis =
      intent.getLongExtra(BookingReminderHelper.EXTRA_DATE_MILLIS, System.currentTimeMillis())

    BookingReminderHelper.showBookingNotification(
      context = context,
      matchId = matchId,
      title = title,
      pitchName = pitchName,
      timeLabel = timeLabel,
      dateMillis = dateMillis,
    )
  }
}
