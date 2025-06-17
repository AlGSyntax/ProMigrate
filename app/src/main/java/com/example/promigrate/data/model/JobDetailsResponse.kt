package com.example.promigrate.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Diese Klasse beinhaltet Datenklassen, die als Antwortmodelle für den ProMigrateAPI-Service
 * dienen. Die Struktur ermöglicht die Verarbeitung und Darstellung der Daten, die von der API geliefert werden.
 */
@JsonClass(generateAdapter = true)
data class JobDetailsResponse(
    val aktuelleVeroeffentlichungsdatum: String?,
    val angebotsart: String?,
    @Json(name = "firma") val arbeitgeber: String?,
    val branchengruppe: String?,
    val branche: String?,
    val arbeitgeberHashId: String?,
    @Json(name = "stellenlokationen") val arbeitsorte: List<JobDetailsArbeitsort>?,
    val arbeitszeitmodelle: List<String>?,
    @Json(name = "vertragsdauer") val befristung: String?,
    val uebernahme: Boolean?,
    val betriebsgroesse: String?,
    val eintrittsdatum: String?,
    val ersteVeroeffentlichungsdatum: String?,
    val allianzpartner: String?,
    val allianzpartnerUrl: String?,
    val titel: String?,
    val hashId: String?,
    @Json(name = "hauptberuf") val beruf: String?,
    val modifikationsTimestamp: String?,
    @Json(name = "stellenangebotsBeschreibung") val stellenbeschreibung: String?,
    @Json(name = "referenznummer") val refnr: String?,
    val fuerFluechtlingeGeeignet: Boolean?,
    val nurFuerSchwerbehinderte: Boolean?,
    val anzahlOffeneStellen: Int?,
    val arbeitgeberAdresse: ArbeitgeberAdresse?,
    val fertigkeiten: List<Fertigkeiten>?,
    val mobilitaet: Mobilitaet?,
    val fuehrungskompetenzen: Fuehrungskompetenzen?,
    @Json(name = "gehalt") val verguetung: String?,
    val arbeitgeberdarstellungUrl: String?,
    val arbeitgeberdarstellung: String?,
    val hauptDkz: String?,
    val istBetreut: Boolean?,
    val istGoogleJobsRelevant: Boolean?,
    val anzeigeAnonym: Boolean?
)

@JsonClass(generateAdapter = true)
data class JobDetailsArbeitsort(
    @Json(name = "adresse")
    val adresse: ArbeitgeberAdresse?,
    @Json(name = "breite")
    val breite: Double?,
    @Json(name = "laenge")
    val laenge: Double?
)


@JsonClass(generateAdapter = true)
data class ArbeitgeberAdresse(
    val land: String?,
    val region: String?,
    val plz: String?,
    val ort: String?,
    val strasse: String?,
    val strasseHausnummer: String?
)

@JsonClass(generateAdapter = true)
data class Fertigkeiten(
    val hierarchieName: String?,
    val auspraegungen: Map<String, Any>?
)

@JsonClass(generateAdapter = true)
data class Mobilitaet(
    val reisebereitschaft: String?
)

@JsonClass(generateAdapter = true)
data class Fuehrungskompetenzen(
    val hatVollmacht: Boolean?,
    val hatBudgetverantwortung: Boolean?
)