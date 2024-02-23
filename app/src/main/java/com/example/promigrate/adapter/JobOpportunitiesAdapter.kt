package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.ToDoApplicationItemBinding

class JobOpportunitiesAdapter(private val onItemChecked: (String, String, Boolean) -> Unit) :
    ListAdapter<Pair<String, String>, JobOpportunitiesAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ToDoApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onItemChecked)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobPair = getItem(position)
        holder.bind(jobPair.first, jobPair.second, holder.binding.itemCheckbox.isChecked)
    }

    class JobViewHolder(val binding: ToDoApplicationItemBinding, private val onItemChecked: (String, String, Boolean) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String, hashId: String, isChecked: Boolean) {
            binding.jobTitleTextView.text = jobTitle
            binding.itemCheckbox.isChecked = isChecked

            // Setze einen neuen Click-Listener, der den aktuellen Zustand der Checkbox und die Hash-ID zurückgibt
            binding.itemCheckbox.setOnClickListener {
                val currentChecked = binding.itemCheckbox.isChecked
                onItemChecked(jobTitle, hashId, currentChecked)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.first == newItem.first // Vergleiche nur die Jobtitel
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem // Vergleiche die gesamten Paare
        }
    }
}


