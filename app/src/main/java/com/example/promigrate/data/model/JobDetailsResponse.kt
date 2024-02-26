package com.example.promigrate.data.model

data class JobDetailsResponse(
    val aktuelleVeroeffentlichungsdatum: String?,
    val angebotsart: String?,
    val arbeitgeber: String?,
    val branchengruppe: String?,
    val branche: String?,
    val arbeitgeberHashId: String?,
    val arbeitsorte: List<JobDetailsArbeitsort>?,
    val arbeitszeitmodelle: List<String>?,
    val befristung: String?,
    val uebernahme: Boolean?,
    val betriebsgroesse: String?,
    val eintrittsdatum: String?,
    val ersteVeroeffentlichungsdatum: String?,
    val allianzpartner: String?,
    val allianzpartnerUrl: String?,
    val titel: String?,
    val hashId: String?,
    val beruf: String?,
    val modifikationsTimestamp: String?,
    val stellenbeschreibung: String?,
    val refnr: String?,
    val fuerFluechtlingeGeeignet: Boolean?,
    val nurFuerSchwerbehinderte: Boolean?,
    val anzahlOffeneStellen: Int?,
    val arbeitgeberAdresse: ArbeitgeberAdresse?,
    val fertigkeiten: List<Fertigkeiten>?,
    val mobilitaet: Mobilitaet?,
    val fuehrungskompetenzen: Fuehrungskompetenzen?,
    val verguetung: String?,
    val arbeitgeberdarstellungUrl: String?,
    val arbeitgeberdarstellung: String?,
    val hauptDkz: String?,
    val istBetreut: Boolean?,
    val istGoogleJobsRelevant: Boolean?,
    val anzeigeAnonym: Boolean?
)

data class JobDetailsArbeitsort(
    val land: String?,
    val region: String?,
    val plz: String?,
    val ort: String?,
    val strasse: String?,
    val koordinaten: JobDetailsKoordinaten?
)

data class JobDetailsKoordinaten(
    val lat: Double?,
    val lon: Double?
)

data class ArbeitgeberAdresse(
    val land: String?,
    val region: String?,
    val plz: String?,
    val ort: String?,
    val strasse: String?,
    val strasseHausnummer: String?
)

data class Fertigkeiten(
    val hierarchieName: String?,
    val auspraegungen: Map<String, Any>?
)

data class Mobilitaet(
    val reisebereitschaft: String?
)

data class Fuehrungskompetenzen(
    val hatVollmacht: Boolean?,
    val hatBudgetverantwortung: Boolean?
)