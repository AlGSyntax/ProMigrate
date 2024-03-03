package com.example.promigrate.data.model

data class JobWithToDoItems(
    val jobTitle: String,
    val toDoItems: List<ToDoItem>
)
