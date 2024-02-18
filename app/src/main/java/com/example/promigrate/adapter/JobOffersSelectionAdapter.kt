package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.JobItemBinding

class JobOffersSelectionAdapter(private val onItemChecked: (String, Boolean) -> Unit) :
    ListAdapter<String, JobOffersSelectionAdapter.JobViewHolder>(DiffCallback) {

    private val selectedJobs = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobTitle = getItem(position)
        holder.bind(jobTitle, selectedJobs.contains(jobTitle)) { isChecked ->
            if (isChecked) {
                selectedJobs.add(jobTitle)
            } else {
                selectedJobs.remove(jobTitle)
            }
            onItemChecked(jobTitle, isChecked)
        }
    }

    class JobViewHolder(private val binding: JobItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String, isChecked: Boolean, onCheckedChanged: (Boolean) -> Unit) {
            binding.jobTitleTextView.text = jobTitle
            binding.itemCheckbox.isChecked = isChecked
            binding.itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChanged(isChecked)
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
