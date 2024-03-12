 package com.example.promigrate.data.model


 /**
  * Diese Klasse beinhaltet Datenklassen, die als Antwortmodelle für den ProMigrateCourseAPI-Service
  * dienen. Die Struktur ermöglicht die Verarbeitung und Darstellung der Daten, die von der API geliefert werden.
  */
 data class EducationalOfferResponse(
         val _embedded: EmbeddedResponse?,
         val page: PageResponse
     )

 data class EmbeddedResponse(
         val termine: List<TerminResponse>
    )

 data class TerminResponse(
     val id: Long?,
     val unterrichtsform: UnterrichtsformResponse?,
     val beginn: Long?,
     val ende: Long?,
     val angebot: AngebotResponse?,
     val foerderung: Boolean?,
     val kostenWert: String?,
     val kostenWaehrung: String?,
     val bemerkung: String?,
     val link: String?,
     val individuellerEinstieg: Boolean?,
     val anmeldeschluss: String?,
     val unterrichtszeiten: String?,
     val bemerkungZeit: String?,
     val eigeneAngebotsnummer: String?,
     val pruefendeStelle: String?,
     var isChecked: Boolean? = false
 )
 data class UnterrichtsformResponse(
         val id: Int,
        val bezeichnung: String
     )

 data class AngebotResponse(
     val id: Long?,
     val titel: String?,
     val inhalt: String?,
     val abschlussart: String?,
     val abschlussbezeichnung: String?,
     val foerderung: String?,
     val zugang: String?,
     val anrechnung: String?,
     val berechtigungen: String?,
     val zusatzqualifikationen: String?,
     val link: String?,
     val zielgruppe: String?,
     val bildungsanbieter: BildungsanbieterResponse?
 )

data class BildungsanbieterResponse(
        val id: Long?,
       val name: String?,
        val adresse: AdresseResponse
     )

 data class AdresseResponse(
         val ortStrasse: OrtStrasseResponse
     )

 data class OrtStrasseResponse(
         val name: String?
     )

 data class PageResponse(
         val size: Int?,
         val totalElements: Int?,
         val totalPages: Int?,
        val number: Int?
    )
