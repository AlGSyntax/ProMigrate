package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.databinding.ToDoApplicationItemBinding


class DetailToDoJobApplicationAdapter(private val onToDoItemChanged: (String, String, Boolean) -> Unit) : ListAdapter<String, DetailToDoJobApplicationAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ToDoApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onToDoItemChanged)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JobViewHolder(private val binding: ToDoApplicationItemBinding, private val onToDoItemChanged: (String, String, Boolean) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String) {
            binding.jobTitleTextView.text = jobTitle

            val staticToDoList = listOf(
                ToDoItem("1", binding.root.context.getString(R.string.todo_item_1)),
                ToDoItem("2", binding.root.context.getString(R.string.todo_item_2)),
                ToDoItem("3", binding.root.context.getString(R.string.todo_item_3))
            )

            val toDoListAdapter = ToDoListAdapter { todoId, isChecked ->
                onToDoItemChanged(jobTitle, todoId, isChecked)
            }

            binding.todoListRecyclerView.adapter = toDoListAdapter
            binding.todoListRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            toDoListAdapter.submitList(staticToDoList)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}