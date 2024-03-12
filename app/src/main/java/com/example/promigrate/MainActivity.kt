package com.example.promigrate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.promigrate.data.remote.ProMigrateAPI
import com.example.promigrate.data.remote.ProMigrateLangLearnAPI

/**
 * Hauptaktivitätsklasse, die als Einstiegspunkt der Anwendung dient.
 * Diese Klasse initialisiert die ProMigrateAPI und ProMigrateLangLearnAPI.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Diese Funktion wird aufgerufen, wenn die Aktivität startet.
     * Sie initialisiert die ProMigrateAPI und ProMigrateLangLearnAPI und setzt den Inhalt der Aktivität aus einer Layout-Ressource.
     * @param savedInstanceState: Wenn die Aktivität nach dem vorherigen Herunterfahren neu initialisiert wird, enthält dieses Bundle die Daten, die sie zuletzt in onSaveInstanceState(Bundle) bereitgestellt hat. Hinweis: Andernfalls ist es null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialisiert  die ProMigrateAPI mit dem Kontext dieser Aktivität.
        ProMigrateAPI.init(this)
        // Initialisiert  die ProMigrateLangLearnAPI mit dem Kontext dieser Aktivität.
        ProMigrateLangLearnAPI.init(this)
        // Setzt den Inhalt der Aktivität aus einer Layout-Ressource.
        setContentView(R.layout.activity_main)


    }
}
