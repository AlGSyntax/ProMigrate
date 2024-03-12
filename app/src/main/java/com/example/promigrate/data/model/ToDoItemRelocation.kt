package com.example.promigrate.data.model

/**
 * Diese Klasse repräsentiert ein einzelnes ToDo-Element in der Anwendung und ist so gestaltet, dass sie alle notwendigen Informationen für eine ToDo-Aufgabe enthält:
 *
 * id (String): Eine eindeutige Identifikationsnummer oder ein Schlüssel für das ToDo-Element. Dies ist wichtig, um jedes ToDo-Element eindeutig zu identifizieren, insbesondere wenn es in einer Datenbank gespeichert oder aus ihr abgerufen wird.
 *
 * text (String): Der Text des ToDo-Elements, der die Aufgabe oder den Aktionspunkt beschreibt, den der Benutzer ausführen soll. Dieses Feld sollte eine klare und präzise Beschreibung der Aufgabe enthalten, damit der Benutzer genau weiß, was zu tun ist.
 *
 * isCompleted (Boolean = false): Ein boolescher Wert, der angibt, ob die Aufgabe abgeschlossen ist oder nicht. Standardmäßig ist dieser Wert auf false gesetzt, was bedeutet, dass das ToDo-Element noch nicht erledigt ist. Wenn der Benutzer die Aufgabe abschließt, sollte dieser Wert auf true gesetzt werden, um den aktuellen Status widerzuspiegeln.
 */
data class ToDoItemRelocation(
    var id: String = "",
    val text: String = "",
    val isCompleted: Boolean = false
)