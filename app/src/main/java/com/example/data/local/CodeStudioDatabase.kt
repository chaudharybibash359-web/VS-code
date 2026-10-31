package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        FileEntity::class,
        GitCommitEntity::class,
        CloudBackupEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CodeStudioDatabase : RoomDatabase() {
    abstract fun codeStudioDao(): CodeStudioDao

    companion object {
        @Volatile
        private var INSTANCE: CodeStudioDatabase? = null

        fun getDatabase(context: Context): CodeStudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CodeStudioDatabase::class.java,
                    "codestudio_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
