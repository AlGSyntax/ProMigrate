package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.Job
import com.example.promigrate.databinding.ToDoResearchItemBinding



class DetailToDoJobResearchAdapter(private val onItemClicked: (String) -> Unit) : ListAdapter<Job, DetailToDoJobResearchAdapter.DetailToDoJobResearchViewHolder>(
    JobDiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailToDoJobResearchViewHolder {
        val binding = ToDoResearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailToDoJobResearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailToDoJobResearchViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }

    inner class DetailToDoJobResearchViewHolder(private val binding: ToDoResearchItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.textViewTitle.text = job.titel ?: "Unbekannter Titel"

            binding.hiddenView.visibility = View.GONE

            binding.root.setOnClickListener {
                binding.hiddenView.visibility = if (binding.hiddenView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                onItemClicked(job.hashId)
            }
        }
    }

    companion object JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.hashId == newItem.hashId
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}

