package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.data.model.JobDetailsResponse
import com.example.promigrate.databinding.ToDoResearchItemBinding

class DetailToDoJobResearchAdapter(private val onItemClicked: (String) -> Unit) :
    ListAdapter<Pair<String, String>, DetailToDoJobResearchAdapter.DetailToDoJobResearchViewHolder>(
        JobDiffCallback
    ) {

    private var jobDetailsMap = mutableMapOf<String, JobDetailsResponse>()

    fun setJobDetails(hashId: String, details: JobDetailsResponse) {
        jobDetailsMap[hashId] = details
        val position = currentList.indexOfFirst { it.second == hashId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailToDoJobResearchViewHolder {
        val binding = ToDoResearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailToDoJobResearchViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: DetailToDoJobResearchViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job, jobDetailsMap[job.second])
    }

    inner class DetailToDoJobResearchViewHolder(
        private val binding: ToDoResearchItemBinding,
        private val onItemClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Pair<String, String>, details: JobDetailsResponse?) {
            binding.textViewTitle.text = job.first
            details?.let {
                binding.textViewEmployer.text = binding.root.context.getString(R.string.employer, it.arbeitgeber ?: "N/A")
                binding.textViewLocation.text = binding.root.context.getString(R.string.location, it.arbeitgeberAdresse?.ort ?: "N/A")
                binding.textViewWorkModel.text = binding.root.context.getString(R.string.work_model, it.arbeitszeitmodelle?.joinToString(", ") ?: "N/A")
                binding.textViewContract.text = binding.root.context.getString(R.string.contract, getBefristung(details.befristung))
                binding.textViewSalary.text = binding.root.context.getString(R.string.salary, it.verguetung ?: "N/A")
                binding.textViewDescription.text = binding.root.context.getString(R.string.description, it.stellenbeschreibung ?: "N/A")
                binding.textViewBranch.text = binding.root.context.getString(R.string.branch, it.branche ?: "N/A")
                binding.textViewJob.text = binding.root.context.getString(R.string.job, it.beruf ?: "N/A")
                // Füge weitere Details nach Bedarf hinzu.
            }



            // Initial set the visibility to GONE
            binding.root.setOnClickListener {
                // Toggle die Sichtbarkeit der Detailansicht.
                binding.hiddenView.visibility = if (binding.hiddenView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                onItemClicked(job.second)  // Callback für zusätzliche Aktionen.
            }
        }
        private fun getBefristung(befristungsCode: String?): String {
            return when (befristungsCode) {
                "UNBEFRISTET" -> binding.root.context.getString(R.string.unlimited)
                "BEFRISTET" -> binding.root.context.getString(R.string.limited)
                else -> binding.root.context.getString(R.string.unknown)
            }
        }


    }

    companion object JobDiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.second == newItem.second
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem
        }
    }
}


