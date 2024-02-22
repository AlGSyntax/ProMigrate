package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.ToDoResearchItemBinding

class DetailToDoJobResearchAdapter(private val onItemClicked: (String) -> Unit) : ListAdapter<String, DetailToDoJobResearchAdapter.DetailToDoJobResearchViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailToDoJobResearchViewHolder {
        val binding = ToDoResearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailToDoJobResearchViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: DetailToDoJobResearchViewHolder, position: Int) {
        val jobTitle = getItem(position)
        holder.bind(jobTitle)
    }

    class DetailToDoJobResearchViewHolder(private val binding: ToDoResearchItemBinding, private val onItemClicked: (String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String) {
            binding.textViewTitle.text = jobTitle

            // Versteckte Details anfangs ausblenden
            binding.hiddenView.visibility = ViewGroup.GONE

            // Setze den OnClickListener für das gesamte Item
            binding.root.setOnClickListener {
                // Schalte die Sichtbarkeit der Details um
                binding.hiddenView.visibility = if (binding.hiddenView.visibility == ViewGroup.VISIBLE) ViewGroup.GONE else ViewGroup.VISIBLE
                onItemClicked(jobTitle) // Lambda-Ausdruck wird aufgerufen
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

