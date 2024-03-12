package com.example.promigrate.data.model


/**
 *
 * Die FlashCard Klasse ist so gestaltet, dass sie alle relevanten Informationen für eine Lernkarte speichert, die in der Vokabellern-App verwendet wird. Sie hat folgende Attribute:
 *
 * id (String? = null): Eine optionale ID für die Flashcard, die hilfreich sein kann, um einzelne Karten in einer Datenbank oder einem Datenspeicher eindeutig zu identifizieren.
 *
 * frontText (String = ""): Der Text, der auf der Vorderseite der Karte angezeigt wird. Typischerweise könnte dies das zu lernende Wort oder ein Satz sein, in dem das Wort verwendet wird. Der Standardwert ist ein leerer String, der bei der Erstellung einer neuen Karte überschrieben werden sollte.
 *
 * backText (String = ""): Der Text für die Rückseite der Karte. Dies könnte die Übersetzung des Wortes, eine Definition oder eine Ergänzung zum Fronttext sein, um das Lernen zu erleichtern.
 *
 * isFlipped (Boolean = false): Ein Boolescher Wert, der angibt, ob die Karte aktuell umgedreht ist oder nicht. false bedeutet, dass die Vorderseite angezeigt wird, und true, dass die Rückseite sichtbar ist. Diese Funktion ermöglicht es, die Interaktivität der Flashcard zu steuern, sodass Benutzer die Antwort überprüfen können, indem sie die Karte umdrehen.
 *
 *
 */
data class FlashCard(
    var id: String? = null,
    var frontText: String = "",
    var backText: String = "",
    var isFlipped: Boolean = false
)

