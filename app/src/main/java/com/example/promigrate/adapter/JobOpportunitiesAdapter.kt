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
import com.example.promigrate.databinding.JobOportunitiesItemBinding

/**
 * Ein Adapter für die RecyclerView, der Jobmöglichkeiten darstellt. Jeder Job wird als Paar von Strings dargestellt,
 * wobei das erste Element der Jobtitel und das zweite Element die Referenznummer ist.
 *
 * @param onItemChecked: Eine Callback-Funktion, die aufgerufen wird, wenn ein Item angekreuzt oder abgewählt wird.
 *                      Diese Funktion nimmt den Jobtitel, die Referenznummer und einen Boolean (isChecked) entgegen.
 */
class JobOpportunitiesAdapter(private val onItemChecked: (String, String, Boolean) -> Unit) :
    ListAdapter<Pair<String, String>, JobOpportunitiesAdapter.JobViewHolder>(DiffCallback) {


    /**
     * Erstellt einen neuen ViewHolder, wenn kein wiederverwendbarer ViewHolder vorhanden ist.
     *
     * @param parent: Das ViewGroup-Objekt, in das die neue Ansicht eingefügt wird.
     * @param viewType: Der View-Typ des neuen Views.
     * @return: Eine neue Instanz von JobViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding =
            JobOportunitiesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onItemChecked)
    }

    /**
     * Bindet die Daten an den ViewHolder, um den Inhalt für das Item an der gegebenen Position zu füllen.
     * Diese Methode wird für jedes sichtbare Item in der RecyclerView aufgerufen.
     *
     * @param holder: Der ViewHolder, der die Daten halten soll.
     * @param position: Die Position des Items im Adapter.
     */
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobPair = getItem(position)
        // Bindet den Jobtitel und die Jobreferenznummer an den ViewHolder.
        holder.bind(jobPair.first, jobPair.second, holder.binding.itemCheckbox.isChecked)
        // Wendet Animationen auf jedes Item-View an.
        setScaleAndFadeAnimation(holder.itemView)
    }

    /**
     * Wendet eine Kombination aus Skalierungs- und Fade-Animation auf eine View an.
     * Diese Methode erhöht die visuelle Anziehungskraft der Listenelemente beim Einblenden.
     *
     * @param view: Die View, auf die die Animation angewendet werden soll.
     */
    private fun setScaleAndFadeAnimation(view: View) {
        // Erstellt eine Skalierungsanimation, die die View von 20% ihrer Größe auf 100% vergrößert.
        val scaleAnimation = ScaleAnimation(
            0.2f, 1f, 0.2f, 1f,
            // Definiert den Punkt, um den die View skaliert werden soll.
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        // Erstellt eine Fade-Animation, die die Sichtbarkeit der View von 20% auf 100% ändert.
        val fadeAnimation = AlphaAnimation(0.2f, 1.0f)
        // Kombiniert beide Animationen in einem AnimationSet.
        val animationSet = AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(fadeAnimation)
            duration = 700 // Setzt die Dauer der gesamten Animation auf 700 Millisekunden.
        }
        // Startet die Animation auf der angegebenen View.
        view.startAnimation(animationSet)
    }

    /**
     * Ein ViewHolder für Job-Listenelemente im RecyclerView. Diese Klasse definiert das Layout
     * und das Verhalten jedes Listenelements.
     *
     * @param binding: Das Binding-Objekt, das Zugriff auf die UI-Komponenten des Listenelements bietet.
     * @param onItemChecked: Ein Callback, der ausgelöst wird, wenn die Checkbox eines Items geändert wird.
     */
    class JobViewHolder(
        val binding: JobOportunitiesItemBinding,
        private val onItemChecked: (String, String, Boolean) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet Daten an die Ansichten im ViewHolder.
         *
         * @param jobTitle: Der Titel des Jobs, der im TextView angezeigt werden soll.
         * @param refNr: Die Referenznummer des Jobs, die beim Checkbox-Event verwendet wird.
         * @param isChecked: Der aktuelle Zustand der Checkbox.
         */
        fun bind(jobTitle: String, refNr: String, isChecked: Boolean) {
            // Setzt den Jobtitel und den Status der Checkbox basierend auf den übergebenen Parametern.
            binding.jobTitleTextView.text = jobTitle
            binding.itemCheckbox.isChecked = isChecked

            // Definiert einen ClickListener für die Checkbox. Wenn die Checkbox angeklickt wird,
            // wird der onItemChecked Callback mit dem aktuellen Zustand der Checkbox aufgerufen.
            binding.itemCheckbox.setOnClickListener {
                val currentChecked = binding.itemCheckbox.isChecked
                onItemChecked(jobTitle, refNr, currentChecked)
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
        override fun areItemsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            // Vergleicht die ersten Elemente des Paares (Jobtitel), um die Identität zu bestimmen.
            return oldItem.first == newItem.first
        }

        /**
         * Überprüft, ob zwei Items denselben Inhalt haben.
         *
         * @param oldItem: Das Item in der alten Liste.
         * @param newItem: Das Item in der neuen Liste.
         * @return: True, wenn die Inhalte der Items identisch sind, sonst False.
         */
        override fun areContentsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            // Vergleicht die gesamten Pairs, um festzustellen, ob sich der Inhalt geändert hat.
            return oldItem == newItem
        }
    }
}


