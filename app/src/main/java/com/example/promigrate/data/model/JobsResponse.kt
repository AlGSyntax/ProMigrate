package com.example.promigrate.data.model


/**
 * Diese Klasse beinhaltet Datenklassen, die als Antwortmodelle für den ProMigrateAPI-Service
 * dienen. Die Struktur ermöglicht die Verarbeitung und Darstellung der Daten, die von der API geliefert werden.
 */
data class JobResponse(
    val stellenangebote: List<Job>,
    val facetten: Facetten
)



data class Job(
    val beruf: String?,
    val titel: String?,
    val refnr: String,
    val arbeitsort: Arbeitsort,
    val arbeitgeber: String?,
    val aktuelleVeroeffentlichungsdatum: String,
    val modifikationsTimestamp: String,
    val eintrittsdatum: String,
    val logoHashId: String?,
    val kundennummerHash: String?
)

data class Arbeitsort(
    val plz: String?,
    val ort: String?,
    val strasse: String?,
    val region: String?,
    val land: String?,
    val koordinaten: Koordinaten
)

data class Koordinaten(
    val lat: Double,
    val lon: Double
)

data class ArbeitsortList(
    val counts:Map<String,Int>
)


data class Berufsfeld(
    val counts:Map<String,Int>
)

data class Facetten(
    val berufsfeld: Berufsfeld,
    val arbeitsort: ArbeitsortList,

)





