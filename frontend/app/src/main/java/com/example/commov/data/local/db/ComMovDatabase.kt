package com.example.commov.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PendingProfilePhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ComMovDatabase : RoomDatabase() {
    abstract fun pendingProfilePhotoDao(): PendingProfilePhotoDao

    companion object {
        @Volatile
        private var instance: ComMovDatabase? = null

        fun getInstance(context: Context): ComMovDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ComMovDatabase::class.java,
                    "commov.db"
                ).build().also { instance = it }
            }
        }
    }
}
