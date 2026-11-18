package com.example.ui.util

import com.example.data.model.AttendanceStatus
import com.example.data.model.MatchWithParticipants
import com.example.data.model.PaymentStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val englishDigitsSymbols = DecimalFormatSymbols(Locale.ENGLISH)
private val currencyFormatter = DecimalFormat("#,##0.##", englishDigitsSymbols)
private val arabicDateFormatter = SimpleDateFormat("EEEE d MMMM", Locale("ar"))

fun formatCurrency(amount: Double): String {
  return "${currencyFormatter.format(amount)} ج.م"
}

fun formatNumber(amount: Double): String {
  return currencyFormatter.format(amount)
}

fun formatArabicDate(millis: Long): String {
  return arabicDateFormatter.format(Date(millis))
}

fun playerInitials(name: String): String {
  val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
  return when {
    parts.isEmpty() -> "ل"
    parts.size == 1 -> parts[0].take(2)
    else -> "${parts[0].first()}${parts[1].first()}"
  }
}

fun buildMatchShareText(matchWrap: MatchWithParticipants): String {
  val match = matchWrap.match
  val goingList =
    matchWrap.participants.filter { it.participant.attendanceStatus == AttendanceStatus.GOING }
  val notGoingList =
    matchWrap.participants.filter { it.participant.attendanceStatus == AttendanceStatus.NOT_GOING }
  val pendingList =
    matchWrap.participants.filter { it.participant.attendanceStatus == AttendanceStatus.PENDING }

  return buildString {
    appendLine("⚽ حجز كورة: ${match.title}")
    appendLine("🏟️ الملعب: ${match.pitchName}${if (match.location.isNotBlank()) " (${match.location})" else ""}")
    appendLine("📅 الموعد: ${formatArabicDate(match.dateMillis)} • ${match.timeLabel}")
    appendLine("💰 إجمالي الحجز: ${formatCurrency(match.totalCost)} | حصة الفرد: ${formatCurrency(matchWrap.perPlayerShare)}")
    appendLine()

    appendLine("✅ الجايين (${goingList.size}/${match.targetPlayers}):")
    if (goingList.isEmpty()) {
      appendLine("   - لا يوجد تأكيدات بعد")
    } else {
      goingList.forEachIndexed { index, detail ->
        val p = detail.participant
        val payLabel =
          when (p.paymentStatus) {
            PaymentStatus.PAID -> {
              val method = p.paymentMethod?.shortLabelAr ?: "مدفوع"
              "💵 دفع (${formatNumber(p.amountPaid)} ج.م - $method)"
            }
            PaymentStatus.UNPAID -> "⏳ لم يدفع"
            PaymentStatus.EXEMPT -> "🚫 مش هيدفع"
          }
        val ratingLabel = if (p.rating != null) " | ⭐ ${p.rating}/10" else ""
        appendLine("   ${index + 1}. ${detail.player.name} — $payLabel$ratingLabel")
      }
    }

    if (pendingList.isNotEmpty()) {
      appendLine()
      appendLine("🕒 لم يردوا (${pendingList.size}):")
      pendingList.forEach { appendLine("   • ${it.player.name}") }
    }

    if (notGoingList.isNotEmpty()) {
      appendLine()
      appendLine("❌ مش جايين (${notGoingList.size}):")
      notGoingList.forEach { appendLine("   • ${it.player.name}") }
    }

    appendLine()
    appendLine("📊 المحصّل: ${formatCurrency(matchWrap.totalCollected)} من ${formatCurrency(match.totalCost)}")
  }
}
