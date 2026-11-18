package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
  @Transaction
  @Query("SELECT * FROM matches ORDER BY isCompleted ASC, dateMillis ASC")
  fun observeAllMatchesWithParticipants(): Flow<List<MatchWithParticipantRelations>>

  @Transaction
  @Query("SELECT * FROM matches WHERE id = :matchId")
  fun observeMatchWithParticipants(matchId: Long): Flow<MatchWithParticipantRelations?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMatch(match: MatchEntity): Long

  @Update suspend fun updateMatch(match: MatchEntity)

  @Query("UPDATE matches SET isCompleted = :isCompleted WHERE id = :matchId")
  suspend fun setMatchCompleted(matchId: Long, isCompleted: Boolean)

  @Query("DELETE FROM matches WHERE id = :matchId")
  suspend fun deleteMatchById(matchId: Long)
}

@Dao
interface PlayerDao {
  @Query("SELECT * FROM players ORDER BY name ASC")
  fun observeAllPlayers(): Flow<List<PlayerEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlayer(player: PlayerEntity): Long

  @Update suspend fun updatePlayer(player: PlayerEntity)

  @Query("DELETE FROM players WHERE id = :playerId")
  suspend fun deletePlayerById(playerId: Long)
}

@Dao
interface ParticipantDao {
  @Query("SELECT * FROM match_participants")
  fun observeAllParticipants(): Flow<List<MatchParticipantEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertParticipant(participant: MatchParticipantEntity): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertParticipants(participants: List<MatchParticipantEntity>)

  @Update suspend fun updateParticipant(participant: MatchParticipantEntity)

  @Query("UPDATE match_participants SET pitchSlotIndex = NULL WHERE matchId = :matchId AND pitchSlotIndex = :slotIndex")
  suspend fun clearPitchSlot(matchId: Long, slotIndex: Int)

  @Query("DELETE FROM match_participants WHERE id = :participantId")
  suspend fun deleteParticipantById(participantId: Long)

  @Query("DELETE FROM match_participants WHERE matchId = :matchId AND playerId = :playerId")
  suspend fun removePlayerFromMatch(matchId: Long, playerId: Long)
}
