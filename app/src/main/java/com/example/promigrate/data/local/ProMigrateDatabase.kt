package com.example.promigrate.data.local



import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/**
 * Diese Klasse ist eine abstrakte Datenbankklasse für Room. Sie definiert die Entitäten,
 * die in der Datenbank gespeichert werden (in diesem Fall NoteEntity), die Version der Datenbank
 * und ob ein Export-Schema verwendet werden soll oder nicht.
 */
@Database(entities = [PreferenceEntity::class], version = 1, exportSchema = false)
abstract class ProMigrateDatabase : RoomDatabase() {

    // Abstrakte Methode, die das MemeDao für Zugriffe auf die Datenbank bereitstellt.
    abstract val noteDao: ProMigrateDao

    // Companion-Objekt, um die Datenbankinstanz als Singleton bereitzustellen.
    companion object {
        // Instanz der MemeDataBase für Singleton-Zwecke.
        private lateinit var INSTANCE: ProMigrateDatabase

        /**
         * Gibt die Singleton-Instanz der MemeDataBase zurück.
         *
         * @param context: Der Kontext der Anwendung, der für die Datenbankerstellung benötigt wird.
         * @return: Die Singleton-Instanz der MemeDataBase.
         */
        fun getDatabase(context: Context): ProMigrateDatabase {
            // Synchronisierter Block, um sicherzustellen, dass die Datenbankinstanz thread-sicher
            // initialisiert wird.
            synchronized(this) {
                // Prüft, ob die Instanz bereits initialisiert wurde.
                if (!this::INSTANCE.isInitialized) {
                    // Erstellt die Datenbankinstanz mit Room.databaseBuilder.
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ProMigrateDatabase::class.java,
                        "note_database"// Name der Datenbankdatei.
                    ).build()
                }
                // Gibt die Datenbankinstanz zurück.
                return INSTANCE
            }
        }
    }
}