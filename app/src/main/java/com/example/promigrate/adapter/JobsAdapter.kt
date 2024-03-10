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


class JobsAdapter(private val onItemChecked: (String, Boolean) -> Unit) : ListAdapter<String, JobsAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onItemChecked)
    }


    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val jobTitle = getItem(position)
        holder.bind(jobTitle, holder.binding.itemCheckbox.isChecked)

        setScaleAndFadeAnimation(holder.itemView)

        holder.binding.itemCheckbox.setOnClickListener {
            val isChecked = holder.binding.itemCheckbox.isChecked
            onItemChecked(jobTitle, isChecked)
        }



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


    class JobViewHolder(val binding: JobItemBinding, private val onItemChecked: (String, Boolean) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jobTitle: String, isChecked: Boolean) {
            binding.jobTitleTextView.text = jobTitle
            binding.itemCheckbox.isChecked = isChecked




            // Entferne den alten Click-Listener, um Doppelaufrufe zu vermeiden
            binding.itemCheckbox.setOnClickListener(null)

            // Setze einen neuen Click-Listener, der den aktuellen Zustand der Checkbox zurückgibt
            binding.itemCheckbox.setOnClickListener {
                val currentChecked = binding.itemCheckbox.isChecked
                onItemChecked(jobTitle, currentChecked)
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

