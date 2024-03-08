package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItemRelocation
import com.example.promigrate.databinding.RelocationTodoItemBinding

class RelocationToDoListAdapter(
    private val onItemEdit: (ToDoItemRelocation) -> Unit,
    private val onItemDelete: (ToDoItemRelocation) -> Unit
) : ListAdapter<ToDoItemRelocation, RelocationToDoListAdapter.ToDoViewHolder>(ToDoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = RelocationTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding,  onItemEdit, onItemDelete)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ToDoViewHolder(
        private val binding: RelocationTodoItemBinding,
        private val onItemEdit: (ToDoItemRelocation) -> Unit,
        private val onItemDelete: (ToDoItemRelocation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(toDoItem: ToDoItemRelocation) {
            binding.todoItemTextView.text = toDoItem.text
            // Weitere Datenfelder hier setzen, falls benötigt

            // Set up click listeners for edit and delete actions
            binding.editTodoItemButton.setOnClickListener { onItemEdit(toDoItem) }
            binding.deleteTodoItemButton.setOnClickListener { onItemDelete(toDoItem) }

            binding.todoItemCheckbox.isChecked = toDoItem.isCompleted


            // Set up the main item click listener

        }
    }

    companion object ToDoDiffCallback : DiffUtil.ItemCallback<ToDoItemRelocation>() {
        override fun areItemsTheSame(oldItem: ToDoItemRelocation, newItem: ToDoItemRelocation): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ToDoItemRelocation, newItem: ToDoItemRelocation): Boolean = oldItem == newItem
    }
}