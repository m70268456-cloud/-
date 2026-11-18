package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [MatchEntity::class, PlayerEntity::class, MatchParticipantEntity::class],
  version = 3,
  exportSchema = false,
)
@TypeConverters(MalabnaConverters::class)
abstract class MalabnaDatabase : RoomDatabase() {
  abstract fun matchDao(): MatchDao

  abstract fun playerDao(): PlayerDao

  abstract fun participantDao(): ParticipantDao

  companion object {
    @Volatile private var INSTANCE: MalabnaDatabase? = null

    private val MIGRATION_1_2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE players ADD COLUMN photoUri TEXT")
        }
      }

    private val MIGRATION_2_3 =
      object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE match_participants ADD COLUMN rating INTEGER")
          db.execSQL("ALTER TABLE match_participants ADD COLUMN ratingNote TEXT NOT NULL DEFAULT ''")
          db.execSQL("ALTER TABLE match_participants ADD COLUMN ratedAt INTEGER")
        }
      }

    fun getInstance(context: Context): MalabnaDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                MalabnaDatabase::class.java,
                "malabna_bookings.db",
              )
              .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
