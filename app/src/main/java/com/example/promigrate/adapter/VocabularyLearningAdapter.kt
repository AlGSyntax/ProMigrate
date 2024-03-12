package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.FlashCard
import com.example.promigrate.databinding.VocabularyLearningItemBinding

/**
 * Ein Adapter für eine RecyclerView, der Flashcards für das Vokabellernen anzeigt.
 *
 * @property onEdit: Eine Funktion, die aufgerufen wird, wenn eine Flashcard bearbeitet wird.
 */
class VocabularyLearningAdapter(
    private val onEdit: (FlashCard) -> Unit
) : ListAdapter<FlashCard, VocabularyLearningAdapter.VocabularyLearningViewHolder>(DiffCallback) {

    /**
     * Erstellt einen neuen ViewHolder, der das Layout für eine Flashcard verwaltet.
     * Diese Methode wird von der RecyclerView aufgerufen, wenn ein neuer ViewHolder
     * benötigt wird, um ein Listenelement darzustellen.
     *
     * @param parent: Der ViewGroup, in dem die neue Ansicht hinzugefügt wird.
     * @param viewType: Der View-Typ des neuen Views. In diesem Fall wird er nicht verwendet,
     * da der Adapter nur einen Typ von ViewHolder verwendet.
     * @return: Eine neue Instanz von VocabularyLearningViewHolder, der das Layout für eine Indexkarte verwaltet.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VocabularyLearningViewHolder {
        val binding = VocabularyLearningItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // Ein neuer ViewHolder wird instanziiert und zurückgegeben.
        return VocabularyLearningViewHolder(binding, onEdit)
    }

    /**
     * Bindet die Daten einer Flashcard an einen VocabularyLearningViewHolder.
     *
     * @param holder: Der ViewHolder, der die Indexkarte darstellen soll.
     * @param position: Die Position des Items in der Datenliste.
     */
    override fun onBindViewHolder(holder: VocabularyLearningViewHolder, position: Int) {
        val item = getItem(position)
        // Bindet die Flashcard an den ViewHolder.
        holder.bind(item)
    }

    /**
     * Ein ViewHolder, der das Layout für eine Indexkarte verwaltet.
     *
     * @property binding: Das Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
     * @property onEdit: Eine Funktion, die aufgerufen wird, wenn eine Flashcard bearbeitet wird.
     */
    class VocabularyLearningViewHolder(
        private val binding: VocabularyLearningItemBinding,
        private val onEdit: (FlashCard) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet die Daten von einer Flashcard an den ViewHolder.
         * Hier werden die Vorder- und Rückseite der Karte an die jeweiligen UI-Komponenten gebunden.
         *
         * @param flashCard: Eine Indexkarte, die Vorder- und Rücktext enthält.
         */
        fun bind(flashCard: FlashCard) {
            binding.cardFront.text = flashCard.frontText
            binding.cardBack.text = flashCard.backText
            // Die Sichtbarkeit der Vorder- und Rückseite wird basierend auf dem Flipped-Status der Karte festgelegt.
            binding.cardFront.visibility = if (flashCard.isFlipped) View.GONE else View.VISIBLE
            binding.cardBack.visibility = if (flashCard.isFlipped) View.VISIBLE else View.GONE

            // Setzt einen OnClickListener für den Button zum Bearbeiten einer Indexkarte.
            binding.editFlashcardButton.setOnClickListener { onEdit(flashCard) }

            // Setzt einen OnClickListener für die gesamte Indexkarte, um sie umzudrehen.
            binding.root.setOnClickListener {
                flashCard.isFlipped = !flashCard.isFlipped
                bind(flashCard)
            }
        }
    }

    /**
     * Ein DiffUtil.ItemCallback, der zwei Indexkarten vergleicht, um effiziente Updates in der RecyclerView zu ermöglichen.
     * Dieser Callback optimiert die Aktualisierungen, indem er nur die geänderten Elemente neu rendert.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<FlashCard>() {
        /**
         * Prüft, ob zwei Indexkarten dieselbe ID repräsentieren.
         * Wird verwendet, um zu bestimmen, ob ein Item ersetzt wurde oder ob sich sein Inhalt geändert hat.
         *
         * @param oldItem Die Indexkarte in der alten Liste.
         * @param newItem Die Indexkarte in der neuen Liste.
         * @return True, wenn die Indexkarten dieselbe ID haben, andernfalls false.
         */
        override fun areItemsTheSame(oldItem: FlashCard, newItem: FlashCard) =
            oldItem.id == newItem.id

        /**
         * Prüft, ob die Inhalte zweier Indexkarten identisch sind.
         * Wird verwendet, um zu bestimmen, ob eine Indexkarte aktualisiert werden muss.
         *
         * @param oldItem Die Indexkarte in der alten Liste.
         * @param newItem Die Indexkarte in der neuen Liste.
         * @return True, wenn die Inhalte der Indexkarten identisch sind, andernfalls false.
         */
        override fun areContentsTheSame(oldItem: FlashCard, newItem: FlashCard) = oldItem == newItem
    }
}