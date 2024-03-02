package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.ToDoListJobApplicationItemBinding



class ToDoListAdapter(
    private val onItemEdit: (String, String, String) -> Unit  // Änderung hier: zusätzlicher String-Parameter für den Text){}
) : ListAdapter<ToDoItem, ToDoListAdapter.ToDoViewHolder>(ToDoDiffCallback) {

    class ToDoViewHolder(
        private val binding: ToDoListJobApplicationItemBinding,
        private val onItemEdit: (String, String, String) -> Unit,  // Parameter für die Funktion angepasst
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(toDoItem: ToDoItem) {
            binding.todoItemTextView.text = toDoItem.text
            binding.todoItemCheckbox.isChecked = toDoItem.isCompleted

            binding.editTodoItemButton.setOnClickListener {
                onItemEdit(toDoItem.id, toDoItem.text, binding.todoItemTextView.text.toString())  // Text als Parameter hinzugefügt
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = ToDoListJobApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding, onItemEdit)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object ToDoDiffCallback : DiffUtil.ItemCallback<ToDoItem>() {
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem == newItem
    }
}