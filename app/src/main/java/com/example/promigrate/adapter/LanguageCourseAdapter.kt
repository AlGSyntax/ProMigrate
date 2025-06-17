package com.example.promigrate.adapter

import android.animation.ObjectAnimator
import android.content.Intent
import android.icu.text.SimpleDateFormat
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
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.LanguageCourseItemBinding
import java.util.Date
import java.util.Locale


/**
 * LanguageCourseAdapter ist ein Adapter für die RecyclerView, der eine Liste von TerminResponse-Objekten verwaltet.
 * Jedes Element stellt einen Sprachkurs dar und zeigt verschiedene relevante Informationen an,
 * wie Titel, Anbieter, Adresse, Kosten, Abschlussart, Zielgruppe und Anmeldeschluss.
 *
 * Klickbare Links in der Kursbeschreibung werden automatisch erkannt und sind interaktiv.
 * Durch einen Klick auf den Titel des Kurses kann der Benutzer die Detailansicht expandieren oder einklappen.
 * Eine Animation sorgt für einen visuellen Effekt beim Einblenden der Kursdetails.
 *
 * Die Daten werden über DiffUtil effizient aktualisiert.
 */
class LanguageCourseAdapter :
    ListAdapter<TerminResponse, LanguageCourseAdapter.LanguageCourseViewHolder>(DiffCallback) {


    /**
     * Erstellt einen neuen ViewHolder für ein Kurselement.
     *
     * @param parent Das übergeordnete ViewGroup-Element, in das der neue View eingefügt wird.
     * @param viewType Der Typ des Views (wird hier nicht verwendet).
     * @return Ein neuer LanguageCourseViewHolder mit dem zugehörigen Binding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageCourseViewHolder {
        val binding = LanguageCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageCourseViewHolder(binding)
    }

    /**
     * Bindet die Daten eines Kurs-Elements an den ViewHolder.
     *
     * @param holder Der ViewHolder, der die Kursdaten hält.
     * @param position Die Position des Elements in der Liste.
     */
    override fun onBindViewHolder(holder: LanguageCourseViewHolder, position: Int) {
        val kurs = getItem(position)
        holder.bind(kurs)
    }


    /**
     * ViewHolder zur Darstellung eines einzelnen Sprachkurs-Items.
     * Bindet die jeweiligen Felder aus TerminResponse an die zugehörigen Views und sorgt für Interaktivität.
     *
     * @property binding Das Binding-Objekt für die Item-View.
     */
    inner class LanguageCourseViewHolder(
        private val binding: LanguageCourseItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        /**
         * Bindet die Daten eines TerminResponse-Objekts an die Views.
         * - Zeigt Titel, Anbieter, Adresse, Kosten, Abschlussart, Zielgruppe, Prüfung, Beginn, Ende und Anmeldeschluss an.
         * - Setzt klickbare Links in der Kursbeschreibung.
         * - Handhabt das Expandieren/Einklappen der Detailansicht mit Bounce-Animation.
         *
         * @param kurs Das TerminResponse-Objekt mit den Kursdaten.
         *//**
         * Bindet die Daten eines TerminResponse-Objekts an die Views.
         * - Zeigt Titel, Anbieter, Adresse, Kosten, Abschlussart, Zielgruppe, Prüfung, Beginn, Ende und Anmeldeschluss an.
         * - Setzt klickbare Links in der Kursbeschreibung.
         * - Handhabt das Expandieren/Einklappen der Detailansicht mit Bounce-Animation.
         *
         * @param kurs Das TerminResponse-Objekt mit den Kursdaten.
         */
        fun bind(kurs: TerminResponse) {
            // Kurstitel
            binding.langcourseTextView.text = kurs.angebot?.titel ?: "N/A"

            // Anbietername
            kurs.angebot?.bildungsanbieter?.name?.let { name ->
                binding.bildungsanbieterTextView.text = binding.root.context.getString(R.string.provider, name)
            } ?: run {
                binding.bildungsanbieterTextView.text = binding.root.context.getString(R.string.provider, "N/A")
            }

            // Addresse
            kurs.angebot?.bildungsanbieter?.adresse?.let { addressObj ->
                val streetName = addressObj.ortStrasse.name  // OrtStrasse name property
                binding.adresseTextView.text = binding.root.context.getString(R.string.address, streetName)
            } ?: run {
                binding.adresseTextView.text = binding.root.context.getString(R.string.address, "N/A")
            }

            // Prüfende Stelle
            kurs.pruefendeStelle?.let {
                binding.pruefendeStelleTextView.text = binding.root.context.getString(R.string.examining_authority, it)
            } ?: run {
                binding.pruefendeStelleTextView.text = binding.root.context.getString(R.string.examining_authority, "N/A")
            }

            // Kursbeginn
            kurs.beginn?.let {
                binding.beginnTextView.text = binding.root.context.getString(
                    R.string.begin,
                    SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(it)
                )
            } ?: run {
                binding.beginnTextView.text = binding.root.context.getString(R.string.begin, "N/A")
            }

            // Kursende
            kurs.ende?.let {
                binding.endeTextView.text = binding.root.context.getString(
                    R.string.end,
                    SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(it)
                )
            } ?: run {
                binding.endeTextView.text = binding.root.context.getString(R.string.end, "N/A")
            }

            // Kursbeschreibung mit klickbaren Links
            kurs.angebot?.inhalt?.let { rawHtml ->
                val spannableContent = SpannableString(Html.fromHtml(rawHtml, Html.FROM_HTML_MODE_COMPACT))
                Patterns.WEB_URL.matcher(spannableContent).apply {
                    while (find()) {
                        val url = spannableContent.substring(start(), end())
                        spannableContent.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                widget.context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                            }
                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                ds.isUnderlineText = true
                            }
                        }, start(), end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                binding.contentTextView.text = spannableContent
                binding.contentTextView.movementMethod = LinkMovementMethod.getInstance()
            } ?: run {
                binding.contentTextView.text = ""
            }

            // Kosten
            kurs.kostenWert?.let { kosten ->
                val kostenText = "${kosten}€"
                binding.kostenTextView.text = binding.root.context.getString(R.string.cost, kostenText)
            } ?: run {
                binding.kostenTextView.text = binding.root.context.getString(R.string.cost, "N/A")
            }

            // // Förderung
            binding.foerderungTextView.text = if (kurs.foerderung == true) {
                binding.root.context.getString(R.string.funded)
            } else {
                binding.root.context.getString(R.string.not_funded)
            }

            // Abschlussart
            kurs.angebot?.abschlussart?.let { abschlussHtml ->
                val formatted = Html.fromHtml(abschlussHtml, Html.FROM_HTML_MODE_COMPACT)
                binding.abschlussArtTextView.text = binding.root.context.getString(R.string.typeofdegree, formatted)
            } ?: run {
                binding.abschlussArtTextView.text = binding.root.context.getString(R.string.typeofdegree, "N/A")
            }

            // Zielgruppe
            kurs.angebot?.zielgruppe?.let { zielHtml ->
                val formatted = Html.fromHtml(zielHtml, Html.FROM_HTML_MODE_COMPACT)
                binding.zielGruppeTextView.text = binding.root.context.getString(R.string.targetgroup, formatted)
            } ?: run {
                binding.zielGruppeTextView.text = binding.root.context.getString(R.string.targetgroup, "N/A")
            }

            // Anmeldeschluss
            kurs.anmeldeschluss?.let { timestamp ->
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
                val date = Date(timestamp)
                val formattedDate = sdf.format(date)
                binding.anmeldeSchlussTextView.text = binding.root.context.getString(
                    R.string.anmeldeschluss_format, formattedDate
                )
            } ?: run {
                binding.anmeldeSchlussTextView.text = binding.root.context.getString(R.string.anmeldeschluss_format, "N/A")
            }

            // Expand/Collapse Detailbereich bei Klick auf den Kurstitel
            binding.langcourseTextView.setOnClickListener {
                binding.expandableView.visibility = if (binding.expandableView.isVisible) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            // Animation beim Erscheinen des Items
            animateBounce()
        }

        /**
         * Führt eine Bounce-Animation für das Item aus.
         */
        private fun animateBounce() {
            ObjectAnimator.ofFloat(itemView, "translationY", -100f, 0f).apply {
                duration = 1000  // animation duration in ms
                interpolator = BounceInterpolator()
                start()
            }
        }
    }

    /**
     * DiffUtil Callback zur effizienten Aktualisierung der Liste.
     * Vergleicht die Elemente anhand ihrer ID und ihres Inhalts.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<TerminResponse>() {
        override fun areItemsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem == newItem
        }
    }
}