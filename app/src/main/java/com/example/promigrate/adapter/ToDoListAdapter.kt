package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.ToDoListJobApplicationItemBinding



class ToDoListAdapter(
    private val onItemCheckedChanged: (String, Boolean) -> Unit,
    private val onItemEdit: (String) -> Unit
) : ListAdapter<ToDoItem, ToDoListAdapter.ToDoViewHolder>(ToDoDiffCallback) {

    class ToDoViewHolder(
        private val binding: ToDoListJobApplicationItemBinding,
        private val onItemCheckedChanged: (String, Boolean) -> Unit,
        private val onItemEdit: (String) -> Unit,

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(toDoItem: ToDoItem) {
            binding.todoItemTextView.text = toDoItem.text
            binding.todoItemCheckbox.isChecked = toDoItem.isCompleted
            binding.todoItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onItemCheckedChanged(toDoItem.id, isChecked)
                toDoItem.isCompleted = isChecked
            }

            // Set up listeners for your edit and add buttons
            binding.editTodoItemButton.setOnClickListener {
                onItemEdit(toDoItem.id)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = ToDoListJobApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding, onItemCheckedChanged, onItemEdit)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object ToDoDiffCallback : DiffUtil.ItemCallback<ToDoItem>() {
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem == newItem
    }
}