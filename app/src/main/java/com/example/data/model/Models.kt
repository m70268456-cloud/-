package com.example.data.model

import com.example.data.local.MatchEntity
import com.example.data.local.MatchParticipantEntity
import com.example.data.local.PlayerEntity

enum class AttendanceStatus(val labelAr: String) {
  GOING("جاي"),
  NOT_GOING("مش جاي"),
  PENDING("لم يرد"),
}

enum class PaymentStatus(val labelAr: String) {
  PAID("مدفوع"),
  UNPAID("غير مدفوع"),
  EXEMPT("مش هيدفع"),
}

enum class PaymentMethod(val labelAr: String, val shortLabelAr: String) {
  CASH("كاش (نقدي)", "كاش"),
  MOBILE_PAYMENT("محفظة موبايل", "محفظة"),
  CREDIT_CARD("بطاقة بنكية", "بطاقة"),
  INSTAPAY("إنستاباي", "إنستاباي"),
  BANK_TRANSFER("تحويل بنكي", "تحويل"),
}

enum class PitchFormat(val labelAr: String, val defaultTargetPlayers: Int) {
  FIVE_A_SIDE("خماسي 5×5", 10),
  SEVEN_A_SIDE("سباعي 7×7", 14),
  EIGHT_A_SIDE("ثماني 8×8", 16),
  ELEVEN_A_SIDE("ملعب كامل 11×11", 22),
}

data class MatchParticipantDetail(
  val participant: MatchParticipantEntity,
  val player: PlayerEntity,
  val effectiveShare: Double,
)

data class MatchWithParticipants(
  val match: MatchEntity,
  val participants: List<MatchParticipantDetail>,
) {
  val goingCount: Int
    get() = participants.count { it.participant.attendanceStatus == AttendanceStatus.GOING }

  val notGoingCount: Int
    get() = participants.count { it.participant.attendanceStatus == AttendanceStatus.NOT_GOING }

  val pendingCount: Int
    get() = participants.count { it.participant.attendanceStatus == AttendanceStatus.PENDING }

  val payingGoingCount: Int
    get() =
      participants.count {
        it.participant.attendanceStatus == AttendanceStatus.GOING &&
          it.participant.paymentStatus != PaymentStatus.EXEMPT
      }

  val paidCount: Int
    get() = participants.count { it.participant.paymentStatus == PaymentStatus.PAID }

  val unpaidCount: Int
    get() =
      participants.count {
        it.participant.paymentStatus == PaymentStatus.UNPAID &&
          it.participant.attendanceStatus != AttendanceStatus.NOT_GOING
      }

  val exemptCount: Int
    get() = participants.count { it.participant.paymentStatus == PaymentStatus.EXEMPT }

  val perPlayerShare: Double
    get() {
      val divisor =
        when {
          payingGoingCount > 0 -> payingGoingCount
          match.targetPlayers > 0 -> match.targetPlayers
          else -> 1
        }
      return if (match.totalCost > 0) match.totalCost / divisor else 0.0
    }

  val totalCollected: Double
    get() =
      participants
        .filter { it.participant.paymentStatus == PaymentStatus.PAID }
        .sumOf { it.participant.amountPaid }

  val remainingAmount: Double
    get() = (match.totalCost - totalCollected).coerceAtLeast(0.0)

  val collectionProgress: Float
    get() =
      if (match.totalCost <= 0.0) 0f
      else (totalCollected / match.totalCost).toFloat().coerceIn(0f, 1f)
}

data class PlayerWithStats(
  val player: PlayerEntity,
  val totalMatches: Int,
  val goingCount: Int,
  val notGoingCount: Int,
  val unpaidMatchesCount: Int,
  val totalPaid: Double,
  val totalOwed: Double,
)

data class PaymentLedgerItem(
  val participant: MatchParticipantEntity,
  val player: PlayerEntity,
  val match: MatchEntity,
  val suggestedShare: Double,
)

data class PlayerRatingLeaderboardEntry(
  val player: PlayerEntity,
  val ratedMatchesCount: Int,
  val totalRatingPoints: Int,
  val averageRating: Double,
  val highestRating: Int,
  val latestMatchTitle: String,
)

