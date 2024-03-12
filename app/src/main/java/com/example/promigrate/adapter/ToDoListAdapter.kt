package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.ToDoListJobApplicationItemBinding

/**
 * Ein Adapter für eine RecyclerView, der ToDo-Elemente anzeigt.
 *
 * @property onItemEdit: Eine Funktion, die aufgerufen wird, wenn ein ToDo-Element bearbeitet wird.
 */
class ToDoListAdapter(
    private val onItemEdit: (String, String, String) -> Unit
) : ListAdapter<ToDoItem, ToDoListAdapter.ToDoViewHolder>(ToDoDiffCallback) {

    /**
     * Ein ViewHolder, der das Layout für ein ToDo-Element verwaltet.
     *
     * @property binding: Das Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
     * @property onItemEdit: Eine Funktion, die aufgerufen wird, wenn ein ToDo-Element bearbeitet wird.
     */
    class ToDoViewHolder(
        // Das Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
        private val binding: ToDoListJobApplicationItemBinding,
        // Eine Funktion, die aufgerufen wird, wenn ein ToDo-Element bearbeitet wird.
        private val onItemEdit: (String, String, String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet die Daten von einem ToDoItem Objekt an den ViewHolder.
         * Hier werden die ToDo-Text und der Zustand an die jeweiligen UI-Komponenten gebunden.
         *
         * @param toDoItem: Ein Objekt, das einen ToDo-Text und einen Zustand enthält.
         */
        fun bind(toDoItem: ToDoItem) {
            binding.todoItemTextView.text = toDoItem.text
            binding.todoItemCheckbox.isChecked = toDoItem.isCompleted

            // Setzt einen OnClickListener für den Button zum Bearbeiten eines ToDo-Items.
            binding.editTodoItemButton.setOnClickListener {
                onItemEdit(
                    toDoItem.id,
                    toDoItem.text,
                    binding.todoItemTextView.text.toString()
                )  // Text als Parameter hinzugefügt
            }
        }
    }

    /**
     * Erstellt einen neuen ViewHolder, der das Layout für ein ToDo-Element verwaltet.
     * Diese Methode wird von der RecyclerView aufgerufen, wenn ein neuer ViewHolder
     * benötigt wird, um ein Listenelement darzustellen.
     *
     * @param parent: Der ViewGroup, in dem die neue Ansicht hinzugefügt wird.
     * @param viewType: Der View-Typ des neuen Views. In diesem Fall wird er nicht verwendet,
     * da der Adapter nur einen Typ von ViewHolder verwendet.
     * @return: Ein neuer Instanz von ToDoViewHolder, der das Layout für ein ToDo-Element verwaltet.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = ToDoListJobApplicationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToDoViewHolder(binding, onItemEdit)
    }

    /**
     * Bindet die Daten eines ToDoItem an einen ToDoViewHolder.
     *
     * @param holder: Der ViewHolder, der die ToDo-Daten darstellen soll.
     * @param position: Die Position des Items in der Datenliste.
     */
    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Ein DiffUtil.ItemCallback, der zwei ToDoItem Objekte vergleicht.
     */
    companion object ToDoDiffCallback : DiffUtil.ItemCallback<ToDoItem>() {
        // Prüft, ob die beiden ToDoItem Objekte identisch sind.
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean =
            oldItem.id == newItem.id

        // Prüft, ob die Inhalte der beiden ToDoItem Objekte identisch sind.
        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean =
            oldItem == newItem
    }
}