package com.example.promigrate.data.model

/**
 * Repräsentiert einen einzelnen Eintrag in der To-Do-Liste.
 *
 * Die Klasse wird als immutable Datenobjekt genutzt; lediglich der
 * Fertigstellungsstatus (`isCompleted`) ist veränderbar, um ein schnelles
 * Abhaken in der UI zu ermöglichen.
 *
 * @property id           Eindeutige UUID oder ähnlich—dient als Primärschlüssel.
 * @property text         Klartext-Beschreibung der Aufgabe.
 * @property isCompleted  true = Aufgabe abgehakt / erledigt; false = offen.
 */
data class ToDoItem(
    val id: String,
    val text: String,
    var isCompleted: Boolean = false)