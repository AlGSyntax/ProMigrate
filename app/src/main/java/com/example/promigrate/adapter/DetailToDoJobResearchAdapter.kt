package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.ToDoResearchItemBinding

class DetailToDoJobResearchAdapter(private val onItemClicked: (String) -> Unit) :
    ListAdapter<Pair<String, String>, DetailToDoJobResearchAdapter.DetailToDoJobResearchViewHolder>(
        JobDiffCallback
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailToDoJobResearchViewHolder {
        val binding = ToDoResearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailToDoJobResearchViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: DetailToDoJobResearchViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }

    inner class DetailToDoJobResearchViewHolder(
        private val binding: ToDoResearchItemBinding,
        private val onItemClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Pair<String, String>) {
            binding.textViewTitle.text = job.first

            binding.root.setOnClickListener {
                // Toggle visibility of the hidden view
                binding.hiddenView.visibility = if (binding.hiddenView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            // Click listener for the whole card to fetch details
            binding.hiddenView.setOnClickListener {
                onItemClicked(job.second) // Übermittle die hashId des angeklickten Jobs
            }
        }
    }

    companion object JobDiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.second == newItem.second // Vergleiche basierend auf hashId
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem
        }
    }
}



