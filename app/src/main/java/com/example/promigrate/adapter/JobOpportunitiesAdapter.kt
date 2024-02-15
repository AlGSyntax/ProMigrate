package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.JobItemBinding

class JobOpportunitiesAdapter(private val onItemChecked: (String, Boolean) -> Unit) : ListAdapter<String, JobOpportunitiesAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onItemChecked)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobTitle = getItem(position)
        // Rufe hier die bind-Methode auf, ohne den isChecked Zustand zu übergeben
        holder.bind(jobTitle)
    }

    class JobViewHolder(val binding: JobItemBinding, private val onItemChecked: (String, Boolean) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String) {
            binding.jobTitleTextView.text = jobTitle

            // Setze den initialen Zustand der Checkbox, falls notwendig
            binding.itemCheckbox.isChecked = false

            // Setze den Click-Listener, der den aktuellen Zustand der Checkbox zurückgibt
            binding.itemCheckbox.setOnClickListener {
                // Der isChecked Zustand wird hier direkt abgefragt
                val isChecked = binding.itemCheckbox.isChecked
                onItemChecked(jobTitle, isChecked)
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
