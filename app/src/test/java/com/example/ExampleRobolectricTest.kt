package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MatchEntity
import com.example.data.local.MatchParticipantEntity
import com.example.data.local.PlayerEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.MatchParticipantDetail
import com.example.data.model.MatchWithParticipants
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PitchFormat
import com.example.data.repository.MalabnaRepository
import com.example.ui.components.buildPitchSlots
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app_name matches Rejalet El Hogoozat`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("رجاله الحجوزات", appName)
  }

  @Test
  fun `pitch slot counts match pitch format`() {
    assertEquals(10, buildPitchSlots(PitchFormat.FIVE_A_SIDE).size)
    assertEquals(14, buildPitchSlots(PitchFormat.SEVEN_A_SIDE).size)
    assertEquals(16, buildPitchSlots(PitchFormat.EIGHT_A_SIDE).size)
    assertEquals(22, buildPitchSlots(PitchFormat.ELEVEN_A_SIDE).size)
  }

  @Test
  fun `match share and payment calculations exclude exempt and track collected totals`() {
    val match =
      MatchEntity(
        id = 1L,
        title = "خماسي الخميس",
        pitchName = "ملعب السلام",
        dateMillis = 1_760_000_000_000L,
        timeLabel = "9:00 م",
        targetPlayers = 10,
        totalCost = 1000.0,
      )
    val p1 =
      MatchParticipantDetail(
        participant =
          MatchParticipantEntity(
            id = 1L,
            matchId = 1L,
            playerId = 1L,
            attendanceStatus = AttendanceStatus.GOING,
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = PaymentMethod.MOBILE_PAYMENT,
            amountPaid = 500.0,
          ),
        player = PlayerEntity(id = 1L, name = "أحمد"),
        effectiveShare = 500.0,
      )
    val p2 =
      MatchParticipantDetail(
        participant =
          MatchParticipantEntity(
            id = 2L,
            matchId = 1L,
            playerId = 2L,
            attendanceStatus = AttendanceStatus.GOING,
            paymentStatus = PaymentStatus.UNPAID,
          ),
        player = PlayerEntity(id = 2L, name = "كريم"),
        effectiveShare = 500.0,
      )
    val p3 =
      MatchParticipantDetail(
        participant =
          MatchParticipantEntity(
            id = 3L,
            matchId = 1L,
            playerId = 3L,
            attendanceStatus = AttendanceStatus.GOING,
            paymentStatus = PaymentStatus.EXEMPT,
          ),
        player = PlayerEntity(id = 3L, name = "عمر"),
        effectiveShare = 0.0,
      )

    val wrap = MatchWithParticipants(match = match, participants = listOf(p1, p2, p3))
    assertEquals(3, wrap.goingCount)
    assertEquals(2, wrap.payingGoingCount)
    assertEquals(500.0, wrap.perPlayerShare, 0.01)
    assertEquals(500.0, wrap.totalCollected, 0.01)
    assertEquals(500.0, wrap.remainingAmount, 0.01)
  }

  @Test
  fun `monthly leaderboard filters older than 30 days and ranks highest total first`() {
    val now = 1_760_000_000_000L
    val fiveDaysAgo = now - 5L * 86_400_000L
    val tenDaysAgo = now - 10L * 86_400_000L
    val fortyFiveDaysAgo = now - 45L * 86_400_000L

    val playerA = PlayerEntity(id = 1L, name = "أحمد")
    val playerB = PlayerEntity(id = 2L, name = "كريم")

    val matchRecent1 =
      MatchWithParticipants(
        match = MatchEntity(id = 1L, title = "ماتش 1", pitchName = "السلام", dateMillis = fiveDaysAgo, timeLabel = "9م", targetPlayers = 10),
        participants = listOf(
          MatchParticipantDetail(
            participant = MatchParticipantEntity(id = 1L, matchId = 1L, playerId = 1L, rating = 9),
            player = playerA,
            effectiveShare = 0.0,
          ),
          MatchParticipantDetail(
            participant = MatchParticipantEntity(id = 2L, matchId = 1L, playerId = 2L, rating = 8),
            player = playerB,
            effectiveShare = 0.0,
          ),
        ),
      )
    val matchRecent2 =
      MatchWithParticipants(
        match = MatchEntity(id = 2L, title = "ماتش 2", pitchName = "السلام", dateMillis = tenDaysAgo, timeLabel = "9م", targetPlayers = 10),
        participants = listOf(
          MatchParticipantDetail(
            participant = MatchParticipantEntity(id = 3L, matchId = 2L, playerId = 1L, rating = 8),
            player = playerA,
            effectiveShare = 0.0,
          ),
        ),
      )
    val matchOld =
      MatchWithParticipants(
        match = MatchEntity(id = 3L, title = "ماتش قديم", pitchName = "النصر", dateMillis = fortyFiveDaysAgo, timeLabel = "9م", targetPlayers = 10),
        participants = listOf(
          MatchParticipantDetail(
            participant = MatchParticipantEntity(id = 4L, matchId = 3L, playerId = 2L, rating = 10),
            player = playerB,
            effectiveShare = 0.0,
          ),
          MatchParticipantDetail(
            participant = MatchParticipantEntity(id = 5L, matchId = 3L, playerId = 2L, rating = 10),
            player = playerB,
            effectiveShare = 0.0,
          ),
        ),
      )

    val monthly = MalabnaRepository.buildLeaderboard(listOf(matchRecent1, matchRecent2, matchOld), nowMillis = now, last30DaysOnly = true)
    assertEquals(2, monthly.size)
    assertEquals(1L, monthly[0].player.id)
    assertEquals(17, monthly[0].totalRatingPoints)
    assertEquals(8.5, monthly[0].averageRating, 0.01)
    assertEquals(2L, monthly[1].player.id)
    assertEquals(8, monthly[1].totalRatingPoints)

    val allTime = MalabnaRepository.buildLeaderboard(listOf(matchRecent1, matchRecent2, matchOld), nowMillis = now, last30DaysOnly = false)
    assertEquals(2L, allTime[0].player.id)
    assertEquals(28, allTime[0].totalRatingPoints)
  }
}
