package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.IndexCard
import com.example.promigrate.databinding.VocabularyLearningItemBinding

/**
 * Ein Adapter für eine RecyclerView, der Indexkarten für das Vokabellernen anzeigt.
 *
 * @property onEdit: Eine Funktion, die aufgerufen wird, wenn eine Indexkarte bearbeitet wird.
 */
class VocabularyLearningAdapter(
    private val onEdit: (IndexCard) -> Unit
) : ListAdapter<IndexCard, VocabularyLearningAdapter.VocabularyLearningViewHolder>(DiffCallback) {

    /**
     * Erstellt einen neuen ViewHolder, der das Layout für eine Indexkarte verwaltet.
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
     * Bindet die Daten einer Indexkarte an einen VocabularyLearningViewHolder.
     *
     * @param holder: Der ViewHolder, der die Indexkarte darstellen soll.
     * @param position: Die Position des Items in der Datenliste.
     */
    override fun onBindViewHolder(holder: VocabularyLearningViewHolder, position: Int) {
        val item = getItem(position)
        // Bindet die Indexkarte an den ViewHolder.
        holder.bind(item)
    }

    /**
     * Ein ViewHolder, der das Layout für eine Indexkarte verwaltet.
     *
     * @property binding: Das Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
     * @property onEdit: Eine Funktion, die aufgerufen wird, wenn eine Indexkarte bearbeitet wird.
     */
    class VocabularyLearningViewHolder(
        private val binding: VocabularyLearningItemBinding,
        private val onEdit: (IndexCard) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet die Daten von einer Indexkarte an den ViewHolder.
         * Hier werden die Vorder- und Rückseite der Karte an die jeweiligen UI-Komponenten gebunden.
         *
         * @param indexCard: Eine Indexkarte, die Vorder- und Rücktext enthält.
         */
        fun bind(indexCard: IndexCard) {
            binding.cardFront.text = indexCard.frontText
            binding.cardBack.text = indexCard.backText
            // Die Sichtbarkeit der Vorder- und Rückseite wird basierend auf dem Flipped-Status der Karte festgelegt.
            binding.cardFront.visibility = if (indexCard.isFlipped) View.GONE else View.VISIBLE
            binding.cardBack.visibility = if (indexCard.isFlipped) View.VISIBLE else View.GONE

            // Setzt einen OnClickListener für den Button zum Bearbeiten einer Indexkarte.
            binding.editFlashcardButton.setOnClickListener { onEdit(indexCard) }

            // Setzt einen OnClickListener für die gesamte Indexkarte, um sie umzudrehen.
            binding.root.setOnClickListener {
                indexCard.isFlipped = !indexCard.isFlipped
                bind(indexCard)
            }
        }
    }

    /**
     * Ein DiffUtil.ItemCallback, der zwei Indexkarten vergleicht, um effiziente Updates in der RecyclerView zu ermöglichen.
     * Dieser Callback optimiert die Aktualisierungen, indem er nur die geänderten Elemente neu rendert.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<IndexCard>() {
        /**
         * Prüft, ob zwei Indexkarten dieselbe ID repräsentieren.
         * Wird verwendet, um zu bestimmen, ob ein Item ersetzt wurde oder ob sich sein Inhalt geändert hat.
         *
         * @param oldItem Die Indexkarte in der alten Liste.
         * @param newItem Die Indexkarte in der neuen Liste.
         * @return True, wenn die Indexkarten dieselbe ID haben, andernfalls false.
         */
        override fun areItemsTheSame(oldItem: IndexCard, newItem: IndexCard) =
            oldItem.id == newItem.id

        /**
         * Prüft, ob die Inhalte zweier Indexkarten identisch sind.
         * Wird verwendet, um zu bestimmen, ob eine Indexkarte aktualisiert werden muss.
         *
         * @param oldItem Die Indexkarte in der alten Liste.
         * @param newItem Die Indexkarte in der neuen Liste.
         * @return True, wenn die Inhalte der Indexkarten identisch sind, andernfalls false.
         */
        override fun areContentsTheSame(oldItem: IndexCard, newItem: IndexCard) = oldItem == newItem
    }
}