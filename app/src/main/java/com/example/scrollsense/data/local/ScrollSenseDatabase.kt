package com.example.scrollsense.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.scrollsense.data.local.dao.InterventionDao
import com.example.scrollsense.data.local.dao.ReflectionDao
import com.example.scrollsense.data.local.dao.UsageRecordDao
import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.ReflectionEntity
import com.example.scrollsense.data.local.entities.UsageRecordEntity

@Database(
    entities = [
        UsageRecordEntity::class,
        InterventionEntity::class,
        ReflectionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ScrollSenseDatabase : RoomDatabase() {

    abstract fun usageRecordDao(): UsageRecordDao
    abstract fun interventionDao(): InterventionDao
    abstract fun reflectionDao(): ReflectionDao

    companion object {
        @Volatile
        private var INSTANCE: ScrollSenseDatabase? = null

        fun getInstance(context: Context): ScrollSenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScrollSenseDatabase::class.java,
                    "scrollsense_wellness.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
