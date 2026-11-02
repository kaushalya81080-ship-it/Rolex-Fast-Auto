package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AutoPlayLicenseEntity::class,
        AdminLicenseEntity::class,
        PaymentTransactionEntity::class,
        AppSettingEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RolexDatabase : RoomDatabase() {
    abstract fun rolexDao(): RolexDao

    companion object {
        @Volatile
        private var INSTANCE: RolexDatabase? = null

        fun getDatabase(context: Context): RolexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RolexDatabase::class.java,
                    "rolex_fast_auto.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
