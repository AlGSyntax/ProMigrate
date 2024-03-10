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

class JobOffersSelectionAdapter(private val onItemChecked: (String, String, Boolean) -> Unit) :
    ListAdapter<Pair<String, String>, JobOffersSelectionAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job) { isChecked ->
            onItemChecked(job.first, job.second, isChecked)
        }

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

    class JobViewHolder(private val binding: JobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Pair<String, String>, onCheckedChanged: (Boolean) -> Unit) {
            binding.jobTitleTextView.text = job.first
            binding.itemCheckbox.setOnCheckedChangeListener(null)
            binding.itemCheckbox.isChecked = false
            binding.itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChanged(isChecked)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem
        }
    }
}




