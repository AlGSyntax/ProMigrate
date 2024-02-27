package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.ToDoListJobApplicationItemBinding

data class ToDoItem(val id: String, val text: String)

class ToDoListAdapter(private val onItemCheckedChanged: (String, Boolean) -> Unit) : ListAdapter<ToDoItem, ToDoListAdapter.ToDoViewHolder>(ToDoDiffCallback) {

    class ToDoViewHolder(private val binding: ToDoListJobApplicationItemBinding, private val onItemCheckedChanged: (String, Boolean) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(toDoItem: ToDoItem) {
            binding.todoItemTextView.text = toDoItem.text
            binding.todoItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onItemCheckedChanged(toDoItem.id, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = ToDoListJobApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding, onItemCheckedChanged)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object ToDoDiffCallback : DiffUtil.ItemCallback<ToDoItem>() {
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem == newItem
    }
}