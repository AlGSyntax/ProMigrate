package com.example.promigrate.adapter

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
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
    inner class LanguageCourseViewHolder(
        val binding: LanguageCourseItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(kurs: TerminResponse) {
            // Titel
            binding.langcourseTextView.text = kurs.angebot?.titel ?: "N/A"

            // Beginn und Ende
            kurs.beginn?.let {
                val beginnString = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(it))
                binding.beginnTextView.text = binding.root.context.getString(R.string.begin, beginnString)
            } ?: run {
                binding.beginnTextView.text = binding.root.context.getString(R.string.begin, "N/A")
            }

            kurs.ende?.let {
                val endeString = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(it))
                binding.endeTextView.text = binding.root.context.getString(R.string.end, endeString)
            } ?: run {
                binding.endeTextView.text = binding.root.context.getString(R.string.end, "N/A")
            }

            // Bemerkung mit möglichen Links
            kurs.angebot?.inhalt?.let { bemerkung ->
                val spannableContent = SpannableString(Html.fromHtml(bemerkung, Html.FROM_HTML_MODE_COMPACT))
                Patterns.WEB_URL.matcher(spannableContent).apply {
                    while (find()) {
                        val url = spannableContent.substring(start(), end())
                        spannableContent.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) = widget.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                ds.isUnderlineText = true
                            }
                        }, start(), end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                binding.contentTextView.apply {
                    text = spannableContent
                    movementMethod = LinkMovementMethod.getInstance()
                }

            // Toggle-Verhalten für die Erweiterungsansicht
            binding.langcourseTextView.setOnClickListener {
                binding.expandableView.visibility = if (binding.expandableView.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
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