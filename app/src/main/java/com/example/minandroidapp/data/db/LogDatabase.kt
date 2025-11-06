package com.example.minandroidapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import com.example.minandroidapp.data.db.dao.EntryDao
import com.example.minandroidapp.data.db.dao.TagDao
import com.example.minandroidapp.data.db.entities.EntryEntity
import com.example.minandroidapp.data.db.entities.EntryTagCrossRef
import com.example.minandroidapp.data.db.entities.TagEntity
import com.example.minandroidapp.data.db.entities.TagLinkEntity
import com.example.minandroidapp.data.db.TagSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TagEntity::class,
        TagLinkEntity::class,
        EntryEntity::class,
        EntryTagCrossRef::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class LogDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao

    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: LogDatabase? = null

        fun getInstance(context: Context): LogDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): LogDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                LogDatabase::class.java,
                "quick_log.db",
            )
                .addCallback(PrepopulateCallback())
                .build()
        }
    }

    private class PrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.withTransaction {
                        TagSeeder(database.tagDao()).seed()
                    }
                }
            }
        }
    }
}
