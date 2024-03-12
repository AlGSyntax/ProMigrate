package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.JobWithToDoItems
import com.example.promigrate.databinding.ToDoApplicationItemBinding

/**
 * Ein Adapter für eine RecyclerView, der Jobs und deren zugehörige ToDo-Elemente anzeigt.
 *
 * @property onItemAdd: Eine Funktion, die aufgerufen wird, wenn ein neues ToDo-Element hinzugefügt werden soll.
 * @property onItemEdit: Eine Funktion, die aufgerufen wird, wenn ein ToDo-Element bearbeitet wird.
 * @property onItemDelete: Eine Funktion, die aufgerufen wird, wenn ein ToDo-Element gelöscht wird.
 */
class DetailToDoJobApplicationAdapter(
    private val onItemAdd: (String) -> Unit,
    private val onItemEdit: (String, String, String) -> Unit,
    private val onItemDelete: (String, String) -> Unit
) : ListAdapter<JobWithToDoItems, DetailToDoJobApplicationAdapter.JobViewHolder>(DiffCallback) {


    /**
     * Erstellt einen neuen ViewHolder, der das Layout für ein Job-Element verwaltet.
     * Diese Methode wird von der RecyclerView aufgerufen, wenn ein neuer ViewHolder
     * benötigt wird, um ein Listenelement darzustellen.
     *
     * @param parent: Der ViewGroup, in dem die neue Ansicht hinzugefügt wird.
     * @param viewType: Der View-Typ des neuen Views. In diesem Fall wird er nicht verwendet,
     * da der Adapter nur einen Typ von ViewHolder verwendet.
     * @return: Ein neuer Instanz von JobViewHolder, der das Layout für ein Job-Element verwaltet.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        // Das Layout für einzelne Listenelemente wird aufgebläht.
        val binding =
            ToDoApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Ein neuer ViewHolder wird instanziiert und zurückgegeben.
        return JobViewHolder(binding, onItemAdd, onItemEdit, onItemDelete)
    }

    /**
     * Bindet die Daten eines JobWithToDoItems an einen JobViewHolder.
     *
     * @param holder: Der ViewHolder, der die Job-Daten darstellen soll.
     * @param position: Die Position des Items in der Datenliste.
     */
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Ein ViewHolder, der das Layout für ein Job-Element verwaltet.
     *
     * @property binding: Das Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
     * @property onItemAdd: Eine Funktion, die aufgerufen wird, wenn ein neues ToDo-Element hinzugefügt werden soll.
     * @property onItemEdit: Eine Funktion, die aufgerufen wird, wenn ein ToDo-Element bearbeitet wird.
     * @property onItemDelete: Eine Funktion, die aufgerufen wird, wenn ein ToDo-Element gelöscht wird.
     */
    inner class JobViewHolder(
        // Binding-Objekt, das Zugriff auf die UI-Elemente des Listeneintrags ermöglicht.
        private val binding: ToDoApplicationItemBinding,
        // Callback-Funktion, die aufgerufen wird, wenn ein neues ToDo-Item hinzugefügt werden soll.
        private val onItemAdd: (String) -> Unit,
        // Callback-Funktion, die aufgerufen wird, wenn ein bestehendes ToDo-Item bearbeitet werden soll.
        private val onItemEdit: (String, String, String) -> Unit,
        // Callback-Funktion, die aufgerufen wird, wenn ein ToDo-Item gelöscht werden soll.
        private val onItemDelete: (String, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Zustandsvariable, die speichert, ob die ToDo-Liste aktuell sichtbar ist.
        private var isListVisible = false

        /**
         * Bindet die Daten von einem JobWithToDoItems Objekt an den ViewHolder.
         * Hier werden die Job-Titel und zugehörigen ToDo-Items an die jeweiligen UI-Komponenten gebunden.
         *
         * @param jobWithToDoItems: Ein Objekt, das einen Jobtitel und eine Liste von zugehörigen ToDo-Items enthält.
         */
        fun bind(jobWithToDoItems: JobWithToDoItems) {

            // Setzt den Jobtitel des aktuellen Listeneintrags.
            binding.jobTitleTextView.text = jobWithToDoItems.jobTitle

            // Initialisiert den Adapter für die ToDo-Liste mit einer Callback-Funktion für die Bearbeitung.
            val toDoListAdapter = ToDoListAdapter { todoId, _, text ->
                onItemEdit(
                    jobWithToDoItems.jobTitle,
                    todoId,
                    text
                )
            }

            // Setzt einen OnClickListener für den Jobtitel, der die Sichtbarkeit der ToDo-Liste umschaltet.
            binding.jobTitleTextView.setOnClickListener {
                toggleToDoListVisibility()
            }

            // Setzt einen OnClickListener für den Button zum Hinzufügen eines neuen ToDo-Items.
            binding.addTodoItemButton.setOnClickListener {
                onItemAdd(jobWithToDoItems.jobTitle)
            }

            // Setzt einen OnClickListener für den Button zum Löschen eines ToDo-Items.
            binding.deleteTodoItemButton.setOnClickListener {
                onItemDelete(jobWithToDoItems.jobTitle, "todoId")
            }

            // Passt die Sichtbarkeit der Buttons zur ToDo-Liste an.
            binding.addTodoItemButton.visibility = if (isListVisible) View.GONE else View.VISIBLE
            binding.deleteTodoItemButton.visibility = if (isListVisible) View.GONE else View.VISIBLE

            // Setzt den Adapter und LayoutManager für die RecyclerView der ToDo-Liste.
            binding.todoListRecyclerView.adapter = toDoListAdapter
            binding.todoListRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)

            // Übermittelt die Liste der ToDo-Items an den Adapter.
            toDoListAdapter.submitList(jobWithToDoItems.toDoItems)
        }

        /**
         * Schaltet die Sichtbarkeit der ToDo-Liste um.
         */
        private fun toggleToDoListVisibility() {
            // Invertiert den aktuellen Sichtbarkeitsstatus der ToDo-Liste.
            isListVisible = !isListVisible
            // Setzt die Sichtbarkeit der ToDo-Liste (RecyclerView) abhängig vom aktuellen Zustand.
            binding.todoListRecyclerView.visibility = if (isListVisible) View.VISIBLE else View.GONE
            // Setzt die Sichtbarkeit des Buttons zum Hinzufügen eines ToDo-Elements.
            // Der Button wird nur angezeigt, wenn die Liste nicht sichtbar ist.
            binding.addTodoItemButton.visibility = if (!isListVisible) View.VISIBLE else View.GONE
            // Setzt die Sichtbarkeit des Buttons zum Löschen eines ToDo-Elements.
            // Der Button wird nur angezeigt, wenn die Liste nicht sichtbar ist.
            binding.deleteTodoItemButton.visibility =
                if (!isListVisible) View.VISIBLE else View.GONE
        }
    }

    /**
     * Ein DiffUtil.ItemCallback, der zwei JobWithToDoItems Objekte vergleicht.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<JobWithToDoItems>() {
        // Prüft, ob die beiden JobWithToDoItems Objekte identisch sind.
        override fun areItemsTheSame(
            oldItem: JobWithToDoItems,
            newItem: JobWithToDoItems
        ): Boolean = oldItem.jobTitle == newItem.jobTitle

        // Prüft, ob die Inhalte der beiden JobWithToDoItems Objekte identisch sind.
        override fun areContentsTheSame(
            oldItem: JobWithToDoItems,
            newItem: JobWithToDoItems
        ): Boolean = oldItem == newItem
    }
}

