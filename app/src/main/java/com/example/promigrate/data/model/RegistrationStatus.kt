package com.example.promigrate.data.model

/**
 * Die RegistrationStatus Klasse bietet eine strukturierte Art und Weise, das Ergebnis eines Registrierungsprozesses zu kapseln. Sie enthält zwei Felder:
 *
 * success (Boolean): Dieses Feld zeigt an, ob die Registrierung erfolgreich war oder nicht. true signalisiert einen erfolgreichen Registrierungsprozess, während false auf ein Problem oder einen Fehler während der Registrierung hindeutet.
 *
 * message (Comparable<*> = ""): Eine Nachricht, die zusätzliche Informationen zum Registrierungsstatus bietet. Das könnte ein Erfolgsnachricht, eine Fehlerbeschreibung oder Anweisungen für den Benutzer sein. Die Verwendung von Comparable<*> erlaubt eine flexible Handhabung verschiedener Datentypen, aber normalerweise würde man hier eine Zeichenkette (String) erwarten, die den Benutzern gezeigt wird.
 */
data class RegistrationStatus(
    val success: Boolean,
    val message: Comparable<*> = ""
)

