package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.databinding.JobItemBinding

/**
 * Ein Adapter für die RecyclerView, der Jobangebote darstellt. Jedes Jobangebot wird als Paar von Strings dargestellt,
 * wobei das erste Element der Jobtitel und das zweite Element die Referenznummer ist.
 *
 * @param onItemChecked: Eine Callback-Funktion, die aufgerufen wird, wenn ein Item angekreuzt oder abgewählt wird.
 *                      Diese Funktion nimmt den Jobtitel, die Referenznummer und einen Boolean (isChecked) entgegen.
 */
class JobOffersSelectionAdapter(private val onItemChecked: (String, String, Boolean) -> Unit) :
    ListAdapter<Pair<String, String>, JobOffersSelectionAdapter.JobViewHolder>(DiffCallback) {

    /**
     * Erstellt einen neuen ViewHolder, wenn kein wiederverwendbarer ViewHolder vorhanden ist.
     *
     * @param parent: Das ViewGroup-Objekt, in das die neue Ansicht eingefügt wird.
     * @param viewType: Der View-Typ des neuen Views.
     * @return: Eine neue Instanz von JobViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    /**
     * Bindet die Daten an den ViewHolder, um den Inhalt für das Item an der gegebenen Position zu füllen.
     * Diese Methode wird für jedes sichtbare Item in der RecyclerView aufgerufen.
     *
     * @param holder: Der ViewHolder, der die Daten halten soll.
     * @param position: Die Position des Items im Adapter.
     */
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job) { isChecked ->
            onItemChecked(job.first, job.second, isChecked)
        }

        // Wendet eine Kombination aus Skalierungs- und Fade-Animation auf das Item-View an.
        setScaleAndFadeAnimation(holder.itemView)
    }

    /**
     * Wendet eine Kombination aus Skalierungs- und Fade-Animation auf eine View an.
     * Diese Methode erhöht die visuelle Anziehungskraft der Listenelemente beim Einblenden.
     *
     * @param view: Die View, auf die die Animation angewendet werden soll.
     */
    private fun setScaleAndFadeAnimation(view: View) {
        val scaleAnimation = ScaleAnimation(
            0.2f, 1f, 0.2f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        val fadeAnimation = AlphaAnimation(0.2f, 1.0f)
        val animationSet = AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(fadeAnimation)
            duration = 700
        }
        view.startAnimation(animationSet)
    }

    /**
     * Ein ViewHolder für Job-Listenelemente im RecyclerView. Diese Klasse definiert das Layout
     * und das Verhalten jedes Listenelements.
     *
     * @param binding: Das Binding-Objekt, das Zugriff auf die UI-Komponenten des Listenelements bietet.
     */
    class JobViewHolder(private val binding: JobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet Daten an die Ansichten im ViewHolder.
         *
         * @param job: Das Job-Paar, das im TextView angezeigt werden soll.
         * @param onCheckedChanged: Ein Callback, der ausgelöst wird, wenn die Checkbox eines Items geändert wird.
         */
        fun bind(job: Pair<String, String>, onCheckedChanged: (Boolean) -> Unit) {
            binding.jobTitleTextView.text = job.first
            binding.itemCheckbox.setOnCheckedChangeListener(null)
            binding.itemCheckbox.isChecked = false
            binding.itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChanged(isChecked)
            }
        }
    }

    /**
     * Ein Callback für die Berechnung der Differenz zwischen zwei nicht-null Elementen in einer Liste.
     *
     * Diese Implementierung von DiffUtil.ItemCallback vergleicht Pairs von Strings,
     * wobei jedes Pair aus einem Jobtitel und einer Referenznummer besteht.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        /**
         * Überprüft, ob zwei Objekte das gleiche Item repräsentieren.
         *
         * @param oldItem: Das Item in der alten Liste.
         * @param newItem: Das Item in der neuen Liste.
         * @return: True, wenn die beiden Items das gleiche Datenobjekt repräsentieren, sonst False.
         */
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.first == newItem.first
        }

        /**
         * Überprüft, ob zwei Items denselben Inhalt haben.
         *
         * @param oldItem: Das Item in der alten Liste.
         * @param newItem: Das Item in der neuen Liste.
         * @return: True, wenn die Inhalte der Items identisch sind, sonst False.
         */
        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem
        }
    }
}