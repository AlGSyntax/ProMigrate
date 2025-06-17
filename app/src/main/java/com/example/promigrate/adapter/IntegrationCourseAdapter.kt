// IntegrationCourseAdapter.kt   »  vollständig ersetzen
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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.IntegrationCourseItemBinding
import java.util.Date
import java.util.Locale
import androidx.core.view.isVisible


/**
 * IntegrationCourseAdapter ist ein Adapter für die RecyclerView, der eine Liste von TerminResponse-Objekten verwaltet.
 * Jedes Element stellt einen Integrationskurs dar und zeigt verschiedene relevante Informationen an,
 * wie Titel, Anbieter, Adresse, Kosten, Abschlussart, Zielgruppe und Anmeldeschluss.
 *
 * Klickbare Links in der Kursbeschreibung werden automatisch erkannt und sind interaktiv.
 * Durch einen Klick auf den Titel des Kurses kann der Benutzer die Detailansicht expandieren oder einklappen.
 * Eine Animation sorgt für einen visuellen Effekt beim Einblenden der Kursdetails.
 *
 * Die Daten werden über DiffUtil effizient aktualisiert.
 */
class IntegrationCourseAdapter :
    ListAdapter<TerminResponse, IntegrationCourseAdapter.ViewHolder>(Diff) {



    /**
     * Erstellt einen neuen ViewHolder für ein Kurselement.
     *
     * @param parent Das übergeordnete ViewGroup-Element, in das der neue View eingefügt wird.
     * @param viewType Der Typ des Views (wird hier nicht verwendet).
     * @return Ein neuer ViewHolder mit dem zugehörigen Binding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IntegrationCourseItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    /**
     * Bindet die Daten eines Kurs-Elements an den ViewHolder.
     *
     * @param holder Der ViewHolder, der die Kursdaten hält.
     * @param position Die Position des Elements in der Liste.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))


    /**
     * ViewHolder zur Darstellung eines einzelnen Integrationskurs-Items.
     * Bindet die jeweiligen Felder aus TerminResponse an die zugehörigen Views und sorgt für Interaktivität.
     *
     * @property b Das Binding-Objekt für die Item-View.
     */
    inner class ViewHolder(private val b: IntegrationCourseItemBinding) :
        RecyclerView.ViewHolder(b.root) {


        /**
         * Bindet die Daten eines TerminResponse-Objekts an die Views.
         * - Zeigt Titel, Anbieter, Adresse, Kosten, Abschlussart, Zielgruppe, Prüfung, Beginn, Ende und Anmeldeschluss an.
         * - Setzt klickbare Links in der Kursbeschreibung.
         * - Handhabt das Expandieren/Einklappen der Detailansicht mit Bounce-Animation.
         *
         * @param k Das TerminResponse-Objekt mit den Kursdaten.
         */
        fun bind(k: TerminResponse) {

            // Titel und Anbieter
            b.intecourseTextView.text = k.angebot?.titel ?: "N/A"
            b.bildungsanbieterTextView.text = b.root.context
                .getString(R.string.provider, k.angebot?.bildungsanbieter?.name ?: "N/A")
            b.adresseTextView.text = b.root.context
                .getString(R.string.address,
                    k.angebot?.bildungsanbieter?.adresse?.ortStrasse?.name ?: "N/A")
            b.pruefendeStelleTextView.text = b.root.context
                .getString(R.string.examining_authority, k.pruefendeStelle ?: "N/A")

            // Datumsausgabe
            val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
            b.beginnTextView.text  = b.root.context.getString(
                R.string.begin, k.beginn?.let { fmt.format(Date(it)) } ?: "N/A")
            b.endeTextView.text    = b.root.context.getString(
                R.string.end,   k.ende?.let   { fmt.format(Date(it)) } ?: "N/A")

            // Beschreibung mit klickbaren Links
            k.angebot?.inhalt?.let { raw ->
                val span = SpannableString(Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT))
                Patterns.WEB_URL.matcher(span).apply {
                    while (find()) {
                        val url = span.substring(start(), end())
                        span.setSpan(object : ClickableSpan() {
                            override fun onClick(w: View) =
                                w.context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                            override fun updateDrawState(tp: TextPaint) { tp.isUnderlineText = true }
                        }, start(), end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                b.contentTextView.text = span
                b.contentTextView.movementMethod = LinkMovementMethod.getInstance()
            }

            // Kosten und Förderung
            val costText = k.kostenWert?.let { "${it}€" } ?: "N/A"
            b.kostenTextView.text = b.root.context.getString(R.string.cost, costText)
            b.foerderungTextView.text = if (k.foerderung == true)
                b.root.context.getString(R.string.funded)
            else
                b.root.context.getString(R.string.not_funded)

            // Abschlussart und Zielgruppe
            b.abschlussArtTextView.text = b.root.context.getString(
                R.string.typeofdegree,
                Html.fromHtml(k.angebot?.abschlussart ?: "N/A", Html.FROM_HTML_MODE_COMPACT)
            )
            b.zielGruppeTextView.text = b.root.context.getString(
                R.string.targetgroup,
                Html.fromHtml(k.angebot?.zielgruppe ?: "N/A", Html.FROM_HTML_MODE_COMPACT)
            )

            // Anmeldeschluss
            b.anmeldeSchlussTextView.text = b.root.context.getString(
                R.string.anmeldeschluss_format,
                k.anmeldeschluss?.let { fmt.format(Date(it)) } ?: "N/A"
            )

            // Expand/Collapse Detailbereich bei Klick auf den Kurstitel
            b.intecourseTextView.setOnClickListener {
                b.expandableView.visibility =
                    if (b.expandableView.isVisible) View.GONE else View.VISIBLE
            }

            // Animation beim Erscheinen des Items
            ObjectAnimator.ofFloat(itemView, "translationY", -100f, 0f).apply {
                duration = 1000; interpolator = BounceInterpolator(); start()
            }
        }
    }

    /**
     * DiffUtil Callback zur effizienten Aktualisierung der Liste.
     * Vergleicht die Elemente anhand ihrer ID und ihres Inhalts.
     */
    private companion object Diff : DiffUtil.ItemCallback<TerminResponse>() {
        override fun areItemsTheSame(o: TerminResponse, n: TerminResponse) = o.id == n.id
        override fun areContentsTheSame(o: TerminResponse, n: TerminResponse) = o == n
    }
}