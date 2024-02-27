package com.example.promigrate.data.model

data class BildungsangebotResponse(
    val _embedded: EmbeddedResponse,
    val page: PageResponse
)

data class EmbeddedResponse(
    val termine: List<TerminResponse>
)

data class TerminResponse(
    val id: Long,
    val unterrichtsform: UnterrichtsformResponse,
    val beginn: Long,
    val ende: Long,
    val angebot: AngebotResponse
)

data class UnterrichtsformResponse(
    val id: Int,
    val bezeichnung: String
)

data class AngebotResponse(
    val id: Long,
    val titel: String,
    val bildungsanbieter: BildungsanbieterResponse
)

data class BildungsanbieterResponse(
    val id: Long,
    val name: String,
    val adresse: AdresseResponse
)

data class AdresseResponse(
    val ortStrasse: OrtStrasseResponse
)

data class OrtStrasseResponse(
    val name: String
)

data class PageResponse(
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
