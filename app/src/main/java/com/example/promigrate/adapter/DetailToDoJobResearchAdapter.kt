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

    private var jobDetailsMap = mutableMapOf<String, String>()

    fun setJobDetails(hashId: String, details: String) {
        jobDetailsMap[hashId] = details
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailToDoJobResearchViewHolder {
        val binding = ToDoResearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailToDoJobResearchViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: DetailToDoJobResearchViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job, jobDetailsMap[job.second] ?: "")
    }

    inner class DetailToDoJobResearchViewHolder(
        private val binding: ToDoResearchItemBinding,
        private val onItemClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Pair<String, String>, details: String) {
            binding.textViewTitle.text = job.first

            if (details.isNotEmpty()) {
                binding.textviewdetails.text = details
                binding.hiddenView.visibility = View.VISIBLE
            } else {
                binding.hiddenView.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                val showDetails = binding.hiddenView.visibility != View.VISIBLE
                if (showDetails) {
                    onItemClicked(job.second)
                } else {
                    binding.hiddenView.visibility = View.GONE
                }
            }
        }
    }

    companion object JobDiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.second == newItem.second
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem
        }
    }
}




