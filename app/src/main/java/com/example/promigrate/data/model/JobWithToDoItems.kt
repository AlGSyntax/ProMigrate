package com.example.promigrate.data.model

/**
 * jobTitle (String): Dieses Feld speichert den Titel des Jobs. Der Jobtitel dient als Identifikator
 * oder Überschrift für die zugeordneten ToDo-Elemente. Er könnte in der Benutzeroberfläche als
 * Kopfzeile oder als Teil eines Abschnitts angezeigt werden, der die ToDo-Elemente für diesen
 * spezifischen Job gruppiert.
 *
 * toDoItems (List<ToDoItem>): Eine Liste von ToDoItem-Objekten, die die spezifischen Aufgaben oder
 * Aktionen darstellen, die mit dem Job verbunden sind. Jedes ToDoItem könnte verschiedene
 * Eigenschaften wie Beschreibung, Fälligkeitsdatum, Priorität usw. haben, je nachdem, wie die
 * ToDoItem-Klasse definiert ist.
 */
data class JobWithToDoItems(
    val jobTitle: String,
    val toDoItems: List<ToDoItem>
)
