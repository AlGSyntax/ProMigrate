package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.ToDoApplicationItemBinding

class DetailToDoJobApplicationAdapter(private val onItemChecked: (String, Boolean) -> Unit) : ListAdapter<String, DetailToDoJobApplicationAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ToDoApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onItemChecked)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobTitle = getItem(position)
        holder.bind(jobTitle, holder.binding.itemCheckbox.isChecked)

        holder.binding.itemCheckbox.setOnClickListener {
            val isChecked = holder.binding.itemCheckbox.isChecked
            onItemChecked(jobTitle, isChecked)
        }
    }

    class JobViewHolder(val binding: ToDoApplicationItemBinding, private val onItemChecked: (String, Boolean) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String, isChecked: Boolean) {
            binding.jobTitleTextView.text = jobTitle
            binding.itemCheckbox.isChecked = isChecked

            // Entferne den alten Click-Listener, um Doppelaufrufe zu vermeiden
            binding.itemCheckbox.setOnClickListener(null)

            // Setze einen neuen Click-Listener, der den aktuellen Zustand der Checkbox zurückgibt
            binding.itemCheckbox.setOnClickListener {
                val currentChecked = binding.itemCheckbox.isChecked
                onItemChecked(jobTitle, currentChecked)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}