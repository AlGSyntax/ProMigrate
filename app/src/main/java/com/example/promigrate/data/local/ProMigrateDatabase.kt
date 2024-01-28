package com.example.promigrate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.promigrate.data.model.UserProfile

@Database(entities = [UserProfile::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        // Singleton verhindert mehrere Instanzen der Datenbank zur gleichen Zeit.
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            // Wenn die INSTANCE null ist, dann erstelle eine neue Datenbank.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                // Rückgabe der erstellten Datenbank
                instance
            }
        }
    }
}