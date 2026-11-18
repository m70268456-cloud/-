package com.example.data.repository

import com.example.data.local.MatchDao
import com.example.data.local.MatchEntity
import com.example.data.local.MatchParticipantEntity
import com.example.data.local.MatchWithParticipantRelations
import com.example.data.local.ParticipantDao
import com.example.data.local.ParticipantWithPlayer
import com.example.data.local.PlayerDao
import com.example.data.local.PlayerEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.MatchParticipantDetail
import com.example.data.model.MatchWithParticipants
import com.example.data.model.PaymentLedgerItem
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PitchFormat
import com.example.data.model.PlayerRatingLeaderboardEntry
import com.example.data.model.PlayerWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MalabnaRepository(
  private val matchDao: MatchDao,
  private val playerDao: PlayerDao,
  private val participantDao: ParticipantDao,
) {
  val matchesWithParticipants: Flow<List<MatchWithParticipants>> =
    matchDao.observeAllMatchesWithParticipants().map { list -> list.map { it.toDomain() } }

  fun observeMatch(matchId: Long): Flow<MatchWithParticipants?> =
    matchDao.observeMatchWithParticipants(matchId).map { it?.toDomain() }

  val players: Flow<List<PlayerEntity>> = playerDao.observeAllPlayers()

  val playersWithStats: Flow<List<PlayerWithStats>> =
    combine(players, matchesWithParticipants) { allPlayers, allMatches ->
      val participantToMatch =
        allMatches.flatMap { matchWrap ->
          matchWrap.participants.map { detail -> detail to matchWrap }
        }
      allPlayers.map { player ->
        val entries = participantToMatch.filter { it.first.player.id == player.id }
        val going = entries.count { it.first.participant.attendanceStatus == AttendanceStatus.GOING }
        val notGoing =
          entries.count { it.first.participant.attendanceStatus == AttendanceStatus.NOT_GOING }
        val unpaidEntries =
          entries.filter {
            it.first.participant.paymentStatus == PaymentStatus.UNPAID &&
              it.first.participant.attendanceStatus != AttendanceStatus.NOT_GOING
          }
        val totalPaid =
          entries
            .filter { it.first.participant.paymentStatus == PaymentStatus.PAID }
            .sumOf { it.first.participant.amountPaid }
        val totalOwed = unpaidEntries.sumOf { it.first.effectiveShare }
        PlayerWithStats(
          player = player,
          totalMatches = entries.size,
          goingCount = going,
          notGoingCount = notGoing,
          unpaidMatchesCount = unpaidEntries.size,
          totalPaid = totalPaid,
          totalOwed = totalOwed,
        )
      }
    }

  val allPaymentLedgerItems: Flow<List<PaymentLedgerItem>> =
    matchesWithParticipants.map { matches ->
      matches.flatMap { matchWrap ->
        matchWrap.participants
          .filter {
            // Exclude people who aren't going AND aren't already marked PAID
            it.participant.attendanceStatus != AttendanceStatus.NOT_GOING ||
              it.participant.paymentStatus == PaymentStatus.PAID
          }
          .map { detail ->
            PaymentLedgerItem(
              participant = detail.participant,
              player = detail.player,
              match = matchWrap.match,
              suggestedShare = detail.effectiveShare,
            )
          }
      }
    }

  suspend fun createMatch(match: MatchEntity, initialPlayerIds: List<Long>): Long {
    val matchId = matchDao.insertMatch(match)
    if (initialPlayerIds.isNotEmpty()) {
      val participants =
        initialPlayerIds.map { playerId ->
          MatchParticipantEntity(
            matchId = matchId,
            playerId = playerId,
            attendanceStatus = AttendanceStatus.PENDING,
            paymentStatus = PaymentStatus.UNPAID,
          )
        }
      participantDao.insertParticipants(participants)
    }
    return matchId
  }

  suspend fun updateMatch(match: MatchEntity) = matchDao.updateMatch(match)

  suspend fun setMatchCompleted(matchId: Long, isCompleted: Boolean) =
    matchDao.setMatchCompleted(matchId, isCompleted)

  suspend fun deleteMatch(matchId: Long) = matchDao.deleteMatchById(matchId)

  suspend fun addPlayer(
    name: String,
    phone: String,
    position: String,
    photoUri: String? = null,
  ): Long {
    val cleanName = name.trim()
    val colorIndex = (cleanName.hashCode().let { if (it < 0) -it else it }) % 8
    return playerDao.insertPlayer(
      PlayerEntity(
        name = cleanName,
        phone = phone.trim(),
        position = position.ifBlank { "لاعب" },
        avatarColorIndex = colorIndex,
        photoUri = photoUri,
      )
    )
  }

  suspend fun updatePlayer(player: PlayerEntity) = playerDao.updatePlayer(player)

  suspend fun deletePlayer(playerId: Long) = playerDao.deletePlayerById(playerId)

  suspend fun addPlayersToMatch(matchId: Long, playerIds: List<Long>) {
    val entries =
      playerIds.map { playerId ->
        MatchParticipantEntity(
          matchId = matchId,
          playerId = playerId,
          attendanceStatus = AttendanceStatus.PENDING,
          paymentStatus = PaymentStatus.UNPAID,
        )
      }
    participantDao.insertParticipants(entries)
  }

  suspend fun addNewPlayerAndAssignToMatch(
    matchId: Long,
    name: String,
    phone: String,
    position: String,
    attendanceStatus: AttendanceStatus = AttendanceStatus.GOING,
  ) {
    val playerId = addPlayer(name, phone, position)
    participantDao.insertParticipant(
      MatchParticipantEntity(
        matchId = matchId,
        playerId = playerId,
        attendanceStatus = attendanceStatus,
        paymentStatus = PaymentStatus.UNPAID,
      )
    )
  }

  suspend fun removePlayerFromMatch(matchId: Long, playerId: Long) {
    participantDao.removePlayerFromMatch(matchId, playerId)
  }

  suspend fun updateAttendance(
    participant: MatchParticipantEntity,
    newStatus: AttendanceStatus,
  ) {
    val updatedPaymentStatus =
      if (newStatus == AttendanceStatus.NOT_GOING && participant.paymentStatus == PaymentStatus.PAID) {
        // Keep PAID if they already paid, otherwise keep existing status
        participant.paymentStatus
      } else {
        participant.paymentStatus
      }
    participantDao.updateParticipant(
      participant.copy(attendanceStatus = newStatus, paymentStatus = updatedPaymentStatus)
    )
  }

  suspend fun recordPayment(
    participant: MatchParticipantEntity,
    status: PaymentStatus,
    method: PaymentMethod?,
    amountPaid: Double,
    customAmount: Double?,
    note: String,
  ) {
    val updated =
      when (status) {
        PaymentStatus.PAID ->
          participant.copy(
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = method ?: PaymentMethod.CASH,
            amountPaid = amountPaid.coerceAtLeast(0.0),
            customAmount = customAmount,
            paymentNote = note.trim(),
            paidAt = participant.paidAt ?: System.currentTimeMillis(),
          )
        PaymentStatus.UNPAID ->
          participant.copy(
            paymentStatus = PaymentStatus.UNPAID,
            paymentMethod = null,
            amountPaid = 0.0,
            customAmount = customAmount,
            paymentNote = note.trim(),
            paidAt = null,
          )
        PaymentStatus.EXEMPT ->
          participant.copy(
            paymentStatus = PaymentStatus.EXEMPT,
            paymentMethod = null,
            amountPaid = 0.0,
            customAmount = 0.0,
            paymentNote = note.trim(),
            paidAt = null,
          )
      }
    participantDao.updateParticipant(updated)
  }

  suspend fun assignPlayerToPitchSlot(
    matchId: Long,
    participant: MatchParticipantEntity,
    slotIndex: Int?,
  ) {
    if (slotIndex != null) {
      participantDao.clearPitchSlot(matchId, slotIndex)
    }
    participantDao.updateParticipant(participant.copy(pitchSlotIndex = slotIndex))
  }

  suspend fun autoAssignPitchSlots(
    matchId: Long,
    participants: List<MatchParticipantEntity>,
    totalSlots: Int,
  ) {
    val occupied = participants.mapNotNull { it.pitchSlotIndex }.toMutableSet()
    val unassigned =
      participants
        .filter { it.pitchSlotIndex == null && it.attendanceStatus != AttendanceStatus.NOT_GOING }
        .sortedBy { if (it.attendanceStatus == AttendanceStatus.GOING) 0 else 1 }
    var nextSlot = 0
    for (p in unassigned) {
      while (nextSlot < totalSlots && nextSlot in occupied) {
        nextSlot++
      }
      if (nextSlot >= totalSlots) break
      participantDao.updateParticipant(p.copy(pitchSlotIndex = nextSlot))
      occupied.add(nextSlot)
    }
  }

  suspend fun saveTeamRatings(
    matchId: Long,
    ratings: List<Triple<MatchParticipantEntity, Int?, String>>,
    markMatchCompleted: Boolean,
  ) {
    val now = System.currentTimeMillis()
    for ((participant, rating, note) in ratings) {
      val clampedRating = rating?.coerceIn(1, 10)
      participantDao.updateParticipant(
        participant.copy(
          rating = clampedRating,
          ratingNote = note.trim(),
          ratedAt = if (clampedRating != null) (participant.ratedAt ?: now) else null,
        )
      )
    }
    if (markMatchCompleted) {
      matchDao.setMatchCompleted(matchId, true)
    }
  }

  companion object {
    private const val THIRTY_DAYS_MS = 30L * 86_400_000L

    fun buildLeaderboard(
      matches: List<MatchWithParticipants>,
      nowMillis: Long = System.currentTimeMillis(),
      last30DaysOnly: Boolean = true,
    ): List<PlayerRatingLeaderboardEntry> {
      val cutoffMillis = nowMillis - THIRTY_DAYS_MS
      val filteredMatches =
        if (last30DaysOnly) matches.filter { it.match.dateMillis >= cutoffMillis }
        else matches

      val ratedEntries =
        filteredMatches.flatMap { matchWrap ->
          matchWrap.participants
            .filter { it.participant.rating != null }
            .map { detail -> detail to matchWrap.match }
        }

      return ratedEntries
        .groupBy { it.first.player.id }
        .values
        .map { playerEntries ->
          val sortedByDate = playerEntries.sortedByDescending { it.second.dateMillis }
          val player = sortedByDate.first().first.player
          val ratings = sortedByDate.mapNotNull { it.first.participant.rating }
          val totalPoints = ratings.sum()
          val count = ratings.size
          val average = if (count > 0) totalPoints.toDouble() / count else 0.0
          val highest = ratings.maxOrNull() ?: 0
          val latestTitle = sortedByDate.first().second.title
          PlayerRatingLeaderboardEntry(
            player = player,
            ratedMatchesCount = count,
            totalRatingPoints = totalPoints,
            averageRating = average,
            highestRating = highest,
            latestMatchTitle = latestTitle,
          )
        }
        .sortedWith(
          compareByDescending<PlayerRatingLeaderboardEntry> { it.totalRatingPoints }
            .thenByDescending { it.averageRating }
            .thenByDescending { it.ratedMatchesCount }
        )
    }
  }

  private fun MatchWithParticipantRelations.toDomain(): MatchWithParticipants {
    val payingGoingCount =
      participants.count {
        it.participant.attendanceStatus == AttendanceStatus.GOING &&
          it.participant.paymentStatus != PaymentStatus.EXEMPT
      }
    val divisor =
      when {
        payingGoingCount > 0 -> payingGoingCount
        match.targetPlayers > 0 -> match.targetPlayers
        else -> 1
      }
    val defaultShare = if (match.totalCost > 0.0) match.totalCost / divisor else 0.0

    val mapped =
      participants
        .sortedWith(
          compareBy<ParticipantWithPlayer>(
            {
              when (it.participant.attendanceStatus) {
                AttendanceStatus.GOING -> 0
                AttendanceStatus.PENDING -> 1
                AttendanceStatus.NOT_GOING -> 2
              }
            },
            {
              when (it.participant.paymentStatus) {
                PaymentStatus.UNPAID -> 0
                PaymentStatus.PAID -> 1
                PaymentStatus.EXEMPT -> 2
              }
            },
            { it.player.name },
          )
        )
        .map { rel ->
          val share =
            when {
              rel.participant.paymentStatus == PaymentStatus.EXEMPT -> 0.0
              rel.participant.customAmount != null -> rel.participant.customAmount
              else -> defaultShare
            }
          MatchParticipantDetail(
            participant = rel.participant,
            player = rel.player,
            effectiveShare = share,
          )
        }
    return MatchWithParticipants(match = match, participants = mapped)
  }
}
