 package com.example.promigrate.data.model




 /**
  * Enthält alle Informationen zu einem einzelnen Kurstermin (Sprach- oder Integrationskurs).
  *
  * Die Struktur orientiert sich an der früheren API-Antwort, ist jetzt aber rein lokal.
  *
  * @property id               Eindeutige interne ID (z. B. 1 = Berlin-Kurs, 2 = Hannover …).
  * @property angebot          Detailinformationen zum Kurs selbst (Titel, Anbieter …).
  * @property pruefendeStelle  Prüf- bzw. Zertifizierungsinstitution (z. B. „BAMF“).
  * @property beginn           Startdatum als Unix-Millis (nullable, weil evtl. offen).
  * @property ende             Enddatum als Unix-Millis (nullable).
  * @property kostenWert       Preis in EUR; 0 oder null → kostenlos.
  * @property foerderung       true = Förderfähig/finanziert, false sonst.
  * @property anmeldeschluss   Deadline zur Anmeldung als Unix-Millis (nullable).
  */
 data class TerminResponse(
     val id: Int,
     val angebot: Angebot?,
     val pruefendeStelle: String?,
     val beginn: Long?,            // Unix-Millis   (nullable)
     val ende: Long?,              // Unix-Millis   (nullable)
     val kostenWert: Int?,         // Betrag in EUR (nullable)
     val foerderung: Boolean?,     // true = gefördert
     val anmeldeschluss: Long?     // Unix-Millis   (nullable)
 ) {

     data class Angebot(
         val titel: String?,
         val bildungsanbieter: Bildungsanbieter,
         val inhalt: String?,
         val abschlussart: String?,
         val zielgruppe: String?
     )

     data class Bildungsanbieter(
         val name: String?,
         val adresse: Adresse
     )

     data class Adresse(
         val ortStrasse: OrtStrasse
     )

     data class OrtStrasse(
         val name: String?
     )
 }