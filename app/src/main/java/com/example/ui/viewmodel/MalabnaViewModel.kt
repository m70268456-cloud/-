package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MalabnaDatabase
import com.example.data.local.MatchEntity
import com.example.data.local.MatchParticipantEntity
import com.example.data.local.PlayerEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.MatchWithParticipants
import com.example.data.model.PaymentLedgerItem
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PitchFormat
import com.example.data.model.PlayerRatingLeaderboardEntry
import com.example.data.model.PlayerWithStats
import com.example.data.repository.MalabnaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MalabnaViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: MalabnaRepository by lazy {
    val db = MalabnaDatabase.getInstance(application)
    MalabnaRepository(db.matchDao(), db.playerDao(), db.participantDao())
  }

  val matches: StateFlow<List<MatchWithParticipants>> =
    repository.matchesWithParticipants.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList(),
    )

  val playersWithStats: StateFlow<List<PlayerWithStats>> =
    repository.playersWithStats.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList(),
    )

  val allPlayers: StateFlow<List<PlayerEntity>> =
    repository.players.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList(),
    )

  val paymentLedger: StateFlow<List<PaymentLedgerItem>> =
    repository.allPaymentLedgerItems.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList(),
    )

  val monthlyLeaderboard: StateFlow<List<PlayerRatingLeaderboardEntry>> =
    matches
      .map { MalabnaRepository.buildLeaderboard(it, last30DaysOnly = true) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList(),
      )

  val allTimeLeaderboard: StateFlow<List<PlayerRatingLeaderboardEntry>> =
    matches
      .map { MalabnaRepository.buildLeaderboard(it, last30DaysOnly = false) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList(),
      )

  fun observeMatch(matchId: Long): Flow<MatchWithParticipants?> = repository.observeMatch(matchId)

  fun createMatch(
    title: String,
    pitchName: String,
    location: String,
    dateMillis: Long,
    timeLabel: String,
    format: PitchFormat,
    targetPlayers: Int,
    totalCost: Double,
    notes: String,
    initialPlayerIds: List<Long>,
    onCreated: (Long) -> Unit = {},
  ) {
    viewModelScope.launch {
      val match =
        MatchEntity(
          title = title.trim(),
          pitchName = pitchName.trim(),
          location = location.trim(),
          dateMillis = dateMillis,
          timeLabel = timeLabel.trim(),
          format = format,
          targetPlayers = targetPlayers.coerceAtLeast(2),
          totalCost = totalCost.coerceAtLeast(0.0),
          notes = notes.trim(),
        )
      val id = repository.createMatch(match = match, initialPlayerIds = initialPlayerIds)
      com.example.notifications.BookingReminderHelper.scheduleReminder(
        getApplication(),
        match.copy(id = id),
        match.reminderMinutesBefore,
      )
      onCreated(id)
    }
  }

  fun updateMatch(match: MatchEntity) {
    viewModelScope.launch {
      repository.updateMatch(match)
      com.example.notifications.BookingReminderHelper.scheduleReminder(
        getApplication(),
        match,
        match.reminderMinutesBefore,
      )
    }
  }

  fun updateMatchReminder(match: MatchEntity, minutesBefore: Int?) {
    val updated = match.copy(reminderMinutesBefore = minutesBefore)
    updateMatch(updated)
  }

  fun setMatchCompleted(matchId: Long, isCompleted: Boolean) {
    viewModelScope.launch { repository.setMatchCompleted(matchId, isCompleted) }
  }

  fun deleteMatch(matchId: Long, onDeleted: () -> Unit = {}) {
    viewModelScope.launch {
      repository.deleteMatch(matchId)
      onDeleted()
    }
  }

  fun addPlayer(name: String, phone: String, position: String, photoUri: String? = null) {
    if (name.isBlank()) return
    viewModelScope.launch { repository.addPlayer(name, phone, position, photoUri) }
  }

  fun updatePlayer(player: PlayerEntity) {
    if (player.name.isBlank()) return
    viewModelScope.launch { repository.updatePlayer(player) }
  }

  fun deletePlayer(playerId: Long) {
    viewModelScope.launch { repository.deletePlayer(playerId) }
  }

  fun addPlayersToMatch(matchId: Long, playerIds: List<Long>) {
    if (playerIds.isEmpty()) return
    viewModelScope.launch { repository.addPlayersToMatch(matchId, playerIds) }
  }

  fun addNewPlayerToMatch(
    matchId: Long,
    name: String,
    phone: String,
    position: String,
    attendanceStatus: AttendanceStatus = AttendanceStatus.GOING,
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      repository.addNewPlayerAndAssignToMatch(matchId, name, phone, position, attendanceStatus)
    }
  }

  fun removePlayerFromMatch(matchId: Long, playerId: Long) {
    viewModelScope.launch { repository.removePlayerFromMatch(matchId, playerId) }
  }

  fun updateAttendance(participant: MatchParticipantEntity, newStatus: AttendanceStatus) {
    viewModelScope.launch { repository.updateAttendance(participant, newStatus) }
  }

  fun recordPayment(
    participant: MatchParticipantEntity,
    status: PaymentStatus,
    method: PaymentMethod?,
    amountPaid: Double,
    customAmount: Double?,
    note: String,
  ) {
    viewModelScope.launch {
      repository.recordPayment(participant, status, method, amountPaid, customAmount, note)
    }
  }

  fun assignPlayerToPitchSlot(
    matchId: Long,
    participant: MatchParticipantEntity,
    slotIndex: Int?,
  ) {
    viewModelScope.launch { repository.assignPlayerToPitchSlot(matchId, participant, slotIndex) }
  }

  fun autoAssignPitchSlots(
    matchId: Long,
    participants: List<MatchParticipantEntity>,
    totalSlots: Int,
  ) {
    viewModelScope.launch { repository.autoAssignPitchSlots(matchId, participants, totalSlots) }
  }

  fun saveTeamRatings(
    matchId: Long,
    ratings: List<Triple<MatchParticipantEntity, Int?, String>>,
    markMatchCompleted: Boolean,
  ) {
    viewModelScope.launch {
      repository.saveTeamRatings(matchId, ratings, markMatchCompleted)
    }
  }
}
