package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.ToDoApplicationItemBinding

class DetailToDoJobApplicationAdapter(
    private val onToDoItemChanged: (String, String, Boolean) -> Unit,
    private val onItemAdd: (String) -> Unit,
    private val onItemEdit: (String, String, String) -> Unit  // Änderung hier: zusätzlicher String-Parameter für den Text
) : ListAdapter<JobWithToDoItems, DetailToDoJobApplicationAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ToDoApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onToDoItemChanged, onItemAdd, onItemEdit)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class JobViewHolder(
        private val binding: ToDoApplicationItemBinding,
        private val onToDoItemChanged: (String, String, Boolean) -> Unit,
        private val onItemAdd: (String) -> Unit,
        private val onItemEdit: (String, String, String) -> Unit  // Parameter für die Funktion angepasst
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isListVisible = false

        fun bind(jobWithToDoItems: JobWithToDoItems) {
            binding.jobTitleTextView.text = jobWithToDoItems.jobTitle
            val toDoListAdapter = ToDoListAdapter(
                { todoId, isChecked -> onToDoItemChanged(jobWithToDoItems.jobTitle, todoId, isChecked) },
                { todoId, _, text -> onItemEdit(jobWithToDoItems.jobTitle, todoId, text) }
            )
            binding.jobTitleTextView.setOnClickListener {
                toggleToDoListVisibility()
            }

            binding.addTodoItemButton.setOnClickListener {
                onItemAdd(jobWithToDoItems.jobTitle)
            }

            binding.todoListRecyclerView.adapter = toDoListAdapter
            binding.todoListRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            toDoListAdapter.submitList(jobWithToDoItems.toDoItems)
        }

        private fun toggleToDoListVisibility() {
            isListVisible = !isListVisible
            binding.todoListRecyclerView.visibility = if (isListVisible) View.VISIBLE else View.GONE
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<JobWithToDoItems>() {
        override fun areItemsTheSame(oldItem: JobWithToDoItems, newItem: JobWithToDoItems): Boolean = oldItem.jobTitle == newItem.jobTitle
        override fun areContentsTheSame(oldItem: JobWithToDoItems, newItem: JobWithToDoItems): Boolean = oldItem == newItem
    }
}

data class JobWithToDoItems(
    val jobTitle: String,
    val toDoItems: List<ToDoItem>
)