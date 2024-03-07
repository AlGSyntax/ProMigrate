package com.example.promigrate.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.LanguageCourseItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LanguageCourseAdapter : ListAdapter<TerminResponse, LanguageCourseAdapter.LanguageCourseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageCourseViewHolder {
        val binding = LanguageCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageCourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageCourseViewHolder, position: Int) {
        val kurs = getItem(position)
        holder.bind(kurs)
    }

    class LanguageCourseViewHolder(
        val binding: LanguageCourseItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(kurs: TerminResponse) {
            // Titel
            binding.langcourseTextView.text = kurs.angebot?.titel

            // Beginn und Ende
            kurs.beginn?.let {
                val beginnString = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(it))
                binding.beginnTextView.text = itemView.context.getString(R.string.begin, beginnString)
            }
            kurs.ende?.let {
                val endeString = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(it))
                binding.endeTextView.text = itemView.context.getString(R.string.end, endeString)
            }

            // Bemerkung
            kurs.angebot?.inhalt?.let { bemerkung ->
                val formattedText =
                    Html.fromHtml(bemerkung, Html.FROM_HTML_MODE_COMPACT)
                binding.contentTextView.text = formattedText
            }

            binding.expandableView.visibility = View.GONE

            binding.langcourseTextView.setOnClickListener {
                binding.expandableView.visibility = if (binding.expandableView.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
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