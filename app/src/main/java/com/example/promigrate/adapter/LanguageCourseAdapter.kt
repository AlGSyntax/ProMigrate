package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.LanguageCourseItemBinding

class LanguageCourseAdapter(private val onItemChecked: (TerminResponse, Boolean) -> Unit) : ListAdapter<TerminResponse, LanguageCourseAdapter.LanguageCourseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageCourseViewHolder {
        val binding = LanguageCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageCourseViewHolder(binding, onItemChecked)
    }

    override fun onBindViewHolder(holder: LanguageCourseViewHolder, position: Int) {
        val kurs = getItem(position)
        holder.bind(kurs)
    }

    class LanguageCourseViewHolder(val binding: LanguageCourseItemBinding, private val onItemChecked: (TerminResponse, Boolean) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kurs: TerminResponse) {
            binding.langcourseTextView.text = kurs.angebot?.titel // Stellen Sie sicher, dass kursTitel existiert in TerminResponse
            binding.itemCheckbox.isChecked =
                kurs.isChecked == true // Stellen Sie sicher, dass isChecked existiert in TerminResponse

            // Setzen des ClickListeners
            binding.itemCheckbox.setOnClickListener {
                val currentChecked = binding.itemCheckbox.isChecked
                kurs.isChecked = currentChecked
                onItemChecked(kurs, currentChecked)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TerminResponse>() {
        override fun areItemsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem == newItem
        }
    }
}
