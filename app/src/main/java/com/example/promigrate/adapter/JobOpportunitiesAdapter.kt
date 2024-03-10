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

class JobOpportunitiesAdapter(private val onItemChecked: (String, String, Boolean) -> Unit) :
    ListAdapter<Pair<String, String>, JobOpportunitiesAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobOportunitiesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onItemChecked)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobPair = getItem(position)
        holder.bind(jobPair.first, jobPair.second, holder.binding.itemCheckbox.isChecked)
        setScaleAndFadeAnimation(holder.itemView)
    }


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

    class JobViewHolder(val binding: JobOportunitiesItemBinding, private val onItemChecked: (String, String, Boolean) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String, refNr: String, isChecked: Boolean) {
            binding.jobTitleTextView.text = jobTitle
            binding.itemCheckbox.isChecked = isChecked

            // Setze einen neuen Click-Listener, der den aktuellen Zustand der Checkbox und die Hash-ID zurückgibt
            binding.itemCheckbox.setOnClickListener {
                val currentChecked = binding.itemCheckbox.isChecked
                onItemChecked(jobTitle, refNr, currentChecked)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.first == newItem.first // Vergleiche nur die Jobtitel
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem // Vergleiche die gesamten Paare
        }
    }
}


