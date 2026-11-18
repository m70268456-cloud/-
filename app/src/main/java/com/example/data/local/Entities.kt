package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.example.data.model.AttendanceStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PitchFormat

@Entity(tableName = "matches")
data class MatchEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val pitchName: String,
  val location: String = "",
  val dateMillis: Long,
  val timeLabel: String,
  val format: PitchFormat = PitchFormat.FIVE_A_SIDE,
  val targetPlayers: Int = 10,
  val totalCost: Double = 0.0,
  val isCompleted: Boolean = false,
  val notes: String = "",
  val reminderMinutesBefore: Int? = 120,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "players")
data class PlayerEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val name: String,
  val phone: String = "",
  val position: String = "لاعب",
  val avatarColorIndex: Int = 0,
  val photoUri: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "match_participants",
  foreignKeys =
    [
      ForeignKey(
        entity = MatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["matchId"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = ["id"],
        childColumns = ["playerId"],
        onDelete = ForeignKey.CASCADE,
      ),
    ],
  indices = [Index("matchId"), Index("playerId"), Index(value = ["matchId", "playerId"], unique = true)],
)
data class MatchParticipantEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val matchId: Long,
  val playerId: Long,
  val attendanceStatus: AttendanceStatus = AttendanceStatus.PENDING,
  val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
  val paymentMethod: PaymentMethod? = null,
  val customAmount: Double? = null,
  val amountPaid: Double = 0.0,
  val paymentNote: String = "",
  val paidAt: Long? = null,
  val pitchSlotIndex: Int? = null,
  val rating: Int? = null,
  val ratingNote: String = "",
  val ratedAt: Long? = null,
)

data class ParticipantWithPlayer(
  @Embedded val participant: MatchParticipantEntity,
  @Relation(parentColumn = "playerId", entityColumn = "id") val player: PlayerEntity,
)

data class MatchWithParticipantRelations(
  @Embedded val match: MatchEntity,
  @Relation(
    entity = MatchParticipantEntity::class,
    parentColumn = "id",
    entityColumn = "matchId",
  )
  val participants: List<ParticipantWithPlayer>,
)

class MalabnaConverters {
  @TypeConverter
  fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

  @TypeConverter
  fun toAttendanceStatus(value: String): AttendanceStatus =
    enumValues<AttendanceStatus>().firstOrNull { it.name == value } ?: AttendanceStatus.PENDING

  @TypeConverter
  fun fromPaymentStatus(value: PaymentStatus): String = value.name

  @TypeConverter
  fun toPaymentStatus(value: String): PaymentStatus =
    enumValues<PaymentStatus>().firstOrNull { it.name == value } ?: PaymentStatus.UNPAID

  @TypeConverter
  fun fromPaymentMethod(value: PaymentMethod?): String? = value?.name

  @TypeConverter
  fun toPaymentMethod(value: String?): PaymentMethod? =
    if (value == null) null else enumValues<PaymentMethod>().firstOrNull { it.name == value }

  @TypeConverter
  fun fromPitchFormat(value: PitchFormat): String = value.name

  @TypeConverter
  fun toPitchFormat(value: String): PitchFormat =
    enumValues<PitchFormat>().firstOrNull { it.name == value } ?: PitchFormat.FIVE_A_SIDE
}
