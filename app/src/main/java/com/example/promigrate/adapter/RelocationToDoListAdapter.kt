package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItemRelocation
import com.example.promigrate.databinding.RelocationTodoItemBinding

/**
 * Ein Adapter für die RecyclerView, der die Aufgabenliste für die Umsiedlung anzeigt.
 * Es ermöglicht dem Benutzer, Aufgaben zu bearbeiten und zu löschen.
 *
 * @param onItemEdit: Eine Funktion, die aufgerufen wird, wenn der Benutzer auf die Schaltfläche "Bearbeiten" für eine Aufgabe klickt.
 * @param onItemDelete: Eine Funktion, die aufgerufen wird, wenn der Benutzer auf die Schaltfläche "Löschen" für eine Aufgabe klickt.
 */
class RelocationToDoListAdapter(
    private val onItemEdit: (ToDoItemRelocation) -> Unit,
    private val onItemDelete: (ToDoItemRelocation) -> Unit
) : ListAdapter<ToDoItemRelocation, RelocationToDoListAdapter.ToDoViewHolder>(ToDoDiffCallback) {

    /**
     * Erstellt einen neuen ViewHolder für die Aufgabenliste.
     *
     * @param parent: Die übergeordnete ViewGroup, in die der neue ViewHolder eingefügt wird.
     * @param viewType: Der ViewTyp des neuen ViewHolders.
     * @return :Der erstellte ViewHolder für die Aufgabenliste.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = RelocationTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding,  onItemEdit, onItemDelete)
    }

    /**
     * Bindet die Daten der Aufgabe an den ViewHolder.
     *
     * @param holder: Der ViewHolder, an den die Daten gebunden werden.
     * @param position: Die Position der Aufgabe in der Liste.
     */
    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Ein ViewHolder für die Aufgabenliste.
     * Es hält die Referenz zu den UI-Elementen und bindet die Daten der Aufgabe an diese Elemente.
     *
     * @param binding: Das Binding-Objekt, das Zugriff auf die UI-Elemente ermöglicht.
     * @param onItemEdit: Eine Funktion, die aufgerufen wird, wenn der Benutzer auf die Schaltfläche "Bearbeiten" für eine Aufgabe klickt.
     * @param onItemDelete: Eine Funktion, die aufgerufen wird, wenn der Benutzer auf die Schaltfläche "Löschen" für eine Aufgabe klickt.
     */
    class ToDoViewHolder(
        private val binding: RelocationTodoItemBinding,
        private val onItemEdit: (ToDoItemRelocation) -> Unit,
        private val onItemDelete: (ToDoItemRelocation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet die Daten der Aufgabe an die UI-Elemente des ViewHolders.
         *
         * @param toDoItem: Das Aufgabenelement, dessen Daten an die UI-Elemente gebunden werden.
         */
        fun bind(toDoItem: ToDoItemRelocation) {
            binding.todoItemTextView.text = toDoItem.text

            binding.editTodoItemButton.setOnClickListener { onItemEdit(toDoItem) }
            binding.deleteTodoItemButton.setOnClickListener { onItemDelete(toDoItem) }

            binding.todoItemCheckbox.isChecked = toDoItem.isCompleted
        }
    }

    /**
     * Ein Callback für die DiffUtil, der bestimmt, ob zwei Aufgaben die gleichen sind.
     * Es wird verwendet, um die Änderungen in der Aufgabenliste effizient zu berechnen.
     */
    companion object ToDoDiffCallback : DiffUtil.ItemCallback<ToDoItemRelocation>() {
        /**
         * Überprüft, ob zwei Aufgaben die gleiche ID haben.
         *
         * @param oldItem: Die alte Aufgabe.
         * @param newItem: Die neue Aufgabe.
         * @return :True, wenn die beiden Aufgaben die gleiche ID haben, sonst false.
         */
        override fun areItemsTheSame(oldItem: ToDoItemRelocation, newItem: ToDoItemRelocation): Boolean = oldItem.id == newItem.id

        /**
         * Überprüft, ob zwei Aufgaben die gleichen Daten haben.
         *
         * @param oldItem: Die alte Aufgabe.
         * @param newItem: Die neue Aufgabe.
         * @return :True, wenn die beiden Aufgaben die gleichen Daten haben, sonst false.
         */
        override fun areContentsTheSame(oldItem: ToDoItemRelocation, newItem: ToDoItemRelocation): Boolean = oldItem == newItem
    }
}