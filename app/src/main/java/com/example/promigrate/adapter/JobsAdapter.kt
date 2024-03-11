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
 * Ein Adapter für die RecyclerView, der eine Liste von Jobtiteln anzeigt.
 * Der Adapter verwendet das ViewHolder-Muster, um die Leistung durch Wiederverwendung von Views zu optimieren.
 *
 * @property onItemChecked: Eine Funktion, die aufgerufen wird, wenn ein Item angeklickt wird.
 */
class JobsAdapter(private val onItemChecked: (String, Boolean) -> Unit) :
    ListAdapter<String, JobsAdapter.JobViewHolder>(DiffCallback) {

    /**
     * Erstellt einen neuen ViewHolder, wenn der RecyclerView einen benötigt.
     *
     * @param parent: Die ViewGroup, in die die neue View eingefügt werden soll.
     * @param viewType: Der View-Typ der neuen View.
     * @return: Eine neue Instanz von JobViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onItemChecked)
    }

    /**
     * Bindet Daten an einen ViewHolder an einer bestimmten Position.
     *
     * @param holder: Der ViewHolder, der an Daten gebunden werden soll.
     * @param position: Die Position des Elements in der Datenliste.
     */
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobTitle = getItem(position)
        holder.bind(jobTitle, holder.binding.itemCheckbox.isChecked)

        // Wendet eine Kombination aus Skalierungs- und Fade-Animation auf die View an.
        setScaleAndFadeAnimation(holder.itemView)

        // Setzt einen Click-Listener auf die Checkbox, um den aktuellen Zustand zurückzugeben
        holder.binding.itemCheckbox.setOnClickListener {
            val isChecked = holder.binding.itemCheckbox.isChecked
            onItemChecked(jobTitle, isChecked)
        }


    }

    /**
     * Wendet eine Kombination aus Skalierungs- und Fade-Animation auf die übergebene View an.
     *
     * @param view Die View, auf die die Animationen angewendet werden sollen.
     */
    private fun setScaleAndFadeAnimation(view: View) {

        // Erstellt eine Skalierungs- und Fade-Animation und wendet sie auf die View an.
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
     * Ein ViewHolder für Job-Items. Hält Referenzen auf alle relevanten Views innerhalb des Item-Layouts.
     *
     * @property binding: Das Binding zur Job-Item-View.
     * @property onItemChecked: Eine Funktion, die aufgerufen wird, wenn das Item angeklickt wird.
     */
    class JobViewHolder(
        val binding: JobItemBinding,
        private val onItemChecked: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet den Jobtitel an die Views und setzt den Click-Listener für die Checkbox.
         *
         * @param jobTitle Der Jobtitel, der angezeigt werden soll.
         */
        fun bind(jobTitle: String, isChecked: Boolean) {

            binding.jobTitleTextView.text = jobTitle
            binding.itemCheckbox.isChecked = isChecked


            // Entfernt den alten Click-Listener, um zu verhindern, dass mehrere Listener hinzugefügt werden
            binding.itemCheckbox.setOnClickListener(null)

            // Setze einen neuen Click-Listener, der den aktuellen Zustand der Checkbox zurückgibt
            binding.itemCheckbox.setOnClickListener {
                val currentChecked = binding.itemCheckbox.isChecked
                onItemChecked(jobTitle, currentChecked)
            }


        }
    }

    /**
     * Eine DiffUtil-Callback-Implementierung, um zu bestimmen, ob und wie sich Item-Inhalte geändert haben.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}

