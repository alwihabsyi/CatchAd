package com.catchad.core.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.catchad.core.data.local.room.dao.ContentDao
import com.catchad.core.data.local.room.entity.ContentEntity
import com.catchad.core.domain.constant.Constants.APP_DATABASE

@Database(entities = [ContentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, APP_DATABASE)
                .fallbackToDestructiveMigration().build()
        }
    }
}