package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.data.model.IndexCard

class VocabularyLearningAdapter(
    private val onEdit: (IndexCard) -> Unit
) : ListAdapter<IndexCard, VocabularyLearningAdapter.VocabularyLearningViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyLearningViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vocabulary_learning_item, parent, false)
        return VocabularyLearningViewHolder(view, onEdit)
    }

    override fun onBindViewHolder(holder: VocabularyLearningViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class VocabularyLearningViewHolder(
        itemView: View,
        private val onEdit: (IndexCard) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val frontTextView: TextView = itemView.findViewById(R.id.card_front)
        private val backTextView: TextView = itemView.findViewById(R.id.card_back)
        private val editButton: ImageButton = itemView.findViewById(R.id.editFlashcardButton)

        fun bind(indexCard: IndexCard) {
            frontTextView.text = indexCard.frontText
            backTextView.text = indexCard.backText
            // Setzt die Sichtbarkeit basierend auf dem Zustand der Karteikarte
            frontTextView.visibility = if (indexCard.isFlipped) View.GONE else View.VISIBLE
            backTextView.visibility = if (indexCard.isFlipped) View.VISIBLE else View.GONE

            editButton.setOnClickListener { onEdit(indexCard) }

            itemView.setOnClickListener {
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
