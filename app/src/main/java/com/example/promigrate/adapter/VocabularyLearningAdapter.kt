package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.IndexCard
import com.example.promigrate.databinding.VocabularyLearningItemBinding

class VocabularyLearningAdapter(
    private val onEdit: (IndexCard) -> Unit
) : ListAdapter<IndexCard, VocabularyLearningAdapter.VocabularyLearningViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyLearningViewHolder {
        val binding = VocabularyLearningItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VocabularyLearningViewHolder(binding, onEdit)
    }

    override fun onBindViewHolder(holder: VocabularyLearningViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class VocabularyLearningViewHolder(
        private val binding: VocabularyLearningItemBinding,
        private val onEdit: (IndexCard) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(indexCard: IndexCard) {
            binding.cardFront.text = indexCard.frontText
            binding.cardBack.text = indexCard.backText
            // Set visibility based on the state of the index card
            binding.cardFront.visibility = if (indexCard.isFlipped) View.GONE else View.VISIBLE
            binding.cardBack.visibility = if (indexCard.isFlipped) View.VISIBLE else View.GONE

            binding.editFlashcardButton.setOnClickListener { onEdit(indexCard) }

            binding.root.setOnClickListener {
                indexCard.isFlipped = !indexCard.isFlipped
                bind(indexCard)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<IndexCard>() {
        override fun areItemsTheSame(oldItem: IndexCard, newItem: IndexCard) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: IndexCard, newItem: IndexCard) = oldItem == newItem
    }
}