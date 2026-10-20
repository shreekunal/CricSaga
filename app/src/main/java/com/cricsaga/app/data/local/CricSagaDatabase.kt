package com.cricsaga.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cricsaga.app.data.local.dao.*
import com.cricsaga.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        TeamEntity::class,
        TeamMemberEntity::class,
        MatchEntity::class,
        InningsEntity::class,
        DeliveryEntity::class,
        PlayerStatsEntity::class,
        RedeemCodeEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CricSagaDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun inningsDao(): InningsDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun redeemCodeDao(): RedeemCodeDao

    companion object {
        @Volatile
        private var INSTANCE: CricSagaDatabase? = null

        fun getDatabase(context: Context): CricSagaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CricSagaDatabase::class.java,
                    "cricsaga_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
