package com.example.promigrate.adapter

import android.animation.ObjectAnimator
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
import android.view.animation.BounceInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.IntegrationCourseItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IntegrationCourseAdapter : ListAdapter<TerminResponse, IntegrationCourseAdapter.IntegrationCourseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntegrationCourseViewHolder {
        val binding = IntegrationCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IntegrationCourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IntegrationCourseViewHolder, position: Int) {
        val kurs = getItem(position)
        holder.bind(kurs)
    }
    inner class IntegrationCourseViewHolder(
        val binding: IntegrationCourseItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(kurs: TerminResponse) {
            // Titel
            binding.intecourseTextView.text = kurs.angebot?.titel ?: "N/A"

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
            kurs.angebot?.inhalt?.let { description ->
                val spannableContent = SpannableString(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT))
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


                kurs.kostenWert?.let { kosten ->
                    val kostenText = "${kosten}€"
                    binding.kostenTextView.text = binding.root.context.getString(R.string.cost, kostenText)
                } ?: run {
                    binding.kostenTextView.text = binding.root.context.getString(R.string.cost, "N/A")
                }




                binding.foerderungTextView.text = if (kurs.foerderung == true) {
                    binding.root.context.getString(R.string.funded)
                } else {
                    binding.root.context.getString(R.string.not_funded)
                }

                kurs.angebot.abschlussart?.let { abschlussart ->
                    binding.abschlussArtTextView.text = binding.root.context.getString(R.string.typeofdegree,abschlussart)
                } ?: run {
                    binding.abschlussArtTextView.text = binding.root.context.getString(R.string.typeofdegree, "N/A")
                }

                kurs.angebot.zielgruppe?.let { zielgruppe ->
                    binding.zielGruppeTextView.text = binding.root.context.getString(R.string.targetgroup, zielgruppe)
                } ?: run {
                    binding.zielGruppeTextView.text = binding.root.context.getString(R.string.targetgroup, "N/A")
                }






                kurs.anmeldeschluss?.let {
                    val anmeldeschlussString = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(it))
                    binding.anmeldeSchlussTextView.text = binding.root.context.getString(R.string.anmeldeschluss_format, anmeldeschlussString)
                } ?: run {
                    binding.anmeldeSchlussTextView.text = binding.root.context.getString(R.string.anmeldeschluss_format, "N/A")
                }



                // Toggle-Verhalten für die Erweiterungsansicht
                binding.intecourseTextView.setOnClickListener {
                    binding.expandableView.visibility = if (binding.expandableView.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }

            }
            animateBounce()
        }

        private fun animateBounce() {
            // Animiere die Y-Position des ViewHolders
            ObjectAnimator.ofFloat(itemView, "translationY", -100f, 0f).apply {
                duration = 1000  // Dauer der Animation in Millisekunden
                interpolator = BounceInterpolator()  // Verwendet den BounceInterpolator für den Bounce-Effekt
                start()
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