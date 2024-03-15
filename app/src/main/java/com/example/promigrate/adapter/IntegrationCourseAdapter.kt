package com.example.promigrate.adapter

import android.animation.ObjectAnimator
import android.content.Intent
import android.icu.text.SimpleDateFormat
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
import java.util.Date
import java.util.Locale

/**
 * Wird aufgerufen, wenn die View-Hierarchie des Fragments zerstört wird.
 * Hier wird das Binding-Objekt auf null gesetzt, um Memory Leaks zu vermeiden,
 * da das Binding-Objekt eine Referenz auf die View hält, welche nicht länger existiert.
 */
class IntegrationCourseAdapter :
    ListAdapter<TerminResponse, IntegrationCourseAdapter.IntegrationCourseViewHolder>(DiffCallback) {

    /**
     * Erstellt einen neuen ViewHolder für die Integrationskursliste.
     *
     * @param parent: Die übergeordnete ViewGroup, in die der neue ViewHolder eingefügt wird.
     * @param viewType: Der ViewTyp des neuen ViewHolders.
     * @return :Der erstellte ViewHolder für die Integrationskursliste.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntegrationCourseViewHolder {
        val binding =
            IntegrationCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IntegrationCourseViewHolder(binding)
    }

    /**
     * Bindet die Daten des Integrationskurses an den ViewHolder.
     *
     * @param holder: Der ViewHolder, an den die Daten gebunden werden.
     * @param position: Die Position des Integrationskurses in der Liste.
     */
    override fun onBindViewHolder(holder: IntegrationCourseViewHolder, position: Int) {
        val kurs = getItem(position)
        holder.bind(kurs)
    }

    /**
     * Ein ViewHolder für die Integrationskursliste.
     * Es hält die Referenz zu den UI-Elementen und bindet die Daten des Integrationskurses an diese Elemente.
     *
     * @param binding: Das Binding-Objekt, das Zugriff auf die UI-Elemente ermöglicht.
     */
    inner class IntegrationCourseViewHolder(
        val binding: IntegrationCourseItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bindet die Daten des Integrationskurses an die UI-Elemente des ViewHolders.
         *
         * @param kurs: Der Integrationskurs, dessen Daten an die UI-Elemente gebunden werden.
         */
        fun bind(kurs: TerminResponse) {

            // Setzt den Kurs-Titel. Wenn kein Titel vorhanden ist, wird "N/A" angezeigt.
            binding.intecourseTextView.text = kurs.angebot?.titel ?: "N/A"

            kurs.angebot?.bildungsanbieter?.name?.let {
                binding.bildungsanbieterTextView.text = binding.root.context.getString(R.string.provider, it)
            } ?: run {
                binding.bildungsanbieterTextView.text = binding.root.context.getString(R.string.provider, "N/A")
            }

            kurs.angebot?.bildungsanbieter?.adresse?.let {
                val ortStrasseName =
                    it.ortStrasse.name // Zugriff auf die `name` Eigenschaft von `ortStrasse`
                binding.adresseTextView.text = binding.root.context.getString(R.string.address, ortStrasseName)
            } ?: run {
                binding.adresseTextView.text = binding.root.context.getString(R.string.address, "N/A")
            }

            kurs.pruefendeStelle?.let {
                binding.pruefendeStelleTextView.text = binding.root.context.getString(R.string.examining_authority, it)
            } ?: run {
                binding.pruefendeStelleTextView.text = binding.root.context.getString(R.string.examining_authority, "N/A")
            }

            kurs.beginn?.let {

                binding.beginnTextView.text =
                    binding.root.context.getString(R.string.begin, SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(it))
            } ?: run {
                binding.beginnTextView.text = binding.root.context.getString(R.string.begin, "N/A")
            }


            kurs.ende?.let {

                binding.endeTextView.text = binding.root.context.getString(R.string.end, SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(it))
            } ?: run {
                binding.endeTextView.text = binding.root.context.getString(R.string.end, "N/A")
            }

            // Anzeige der Kursbeschreibung. Links werden interaktiv gestaltet.
            kurs.angebot?.inhalt?.let { bemerkung ->
                val spannableContent =
                    SpannableString(Html.fromHtml(bemerkung, Html.FROM_HTML_MODE_COMPACT))
                Patterns.WEB_URL.matcher(spannableContent).apply {
                    while (find()) {
                        val url = spannableContent.substring(start(), end())
                        spannableContent.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) = widget.context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url)
                                )
                            )

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


                // Setzt den Kostenwert des Kurses. Wenn kein Wert vorhanden ist, wird "N/A" angezeigt.
                kurs.kostenWert?.let { kosten ->
                    val kostenText = "${kosten}€"
                    binding.kostenTextView.text =
                        binding.root.context.getString(R.string.cost, kostenText)
                } ?: run {
                    binding.kostenTextView.text =
                        binding.root.context.getString(R.string.cost, "N/A")
                }


                // Zeigt an, ob der Kurs gefördert wird oder nicht.
                binding.foerderungTextView.text = if (kurs.foerderung == true) {
                    binding.root.context.getString(R.string.funded)
                } else {
                    binding.root.context.getString(R.string.not_funded)
                }


                // Anzeige der Art des Abschlusses und der Zielgruppe, HTML-Format wird berücksichtigt.
                kurs.angebot.abschlussart?.let { abschlussart ->
                    val formattedHtml = Html.fromHtml(abschlussart, Html.FROM_HTML_MODE_COMPACT)
                    binding.abschlussArtTextView.text =
                        binding.root.context.getString(R.string.typeofdegree, formattedHtml)
                } ?: run {
                    binding.abschlussArtTextView.text =
                        binding.root.context.getString(R.string.typeofdegree, "N/A")
                }

                kurs.angebot.zielgruppe?.let { zielgruppe ->
                    val formattedHtml = Html.fromHtml(zielgruppe, Html.FROM_HTML_MODE_COMPACT)
                    binding.zielGruppeTextView.text =
                        binding.root.context.getString(R.string.targetgroup, formattedHtml)
                } ?: run {
                    binding.zielGruppeTextView.text =
                        binding.root.context.getString(R.string.targetgroup, "N/A")
                }


                kurs.anmeldeschluss?.let {
                    // Erstellen eines SimpleDateFormat-Objekts mit deutschem Datumsformat
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)

                    // Umwandlung des Unix-Zeitstempels (in Millisekunden) in ein Date-Objekt
                    val date = Date(it.toLong())

                    // Formatieren des Datum-Objekts zu einem String
                    val formattedDate = sdf.format(date)

                    // Setzen des formatierten Datums in die TextView
                    binding.anmeldeSchlussTextView.text = binding.root.context.getString(
                        R.string.anmeldeschluss_format, formattedDate
                    )
                } ?: run {
                    // Fallback, wenn kein Anmeldeschluss vorhanden ist
                    binding.anmeldeSchlussTextView.text =
                        binding.root.context.getString(R.string.anmeldeschluss_format, "N/A")
                }



                // Setzt einen OnClickListener auf den TextView, der den Kursnamen anzeigt
                binding.intecourseTextView.setOnClickListener {
                    binding.expandableView.visibility =
                        if (binding.expandableView.visibility == View.VISIBLE) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                }

            }


            // Aufruf der Methode 'animateBounce()'
            animateBounce()
        }

        /**
         * Führt eine Bounce-Animation auf dem ViewHolder aus.
         */
        private fun animateBounce() {
            // Animiere die Y-Position des ViewHolders
            ObjectAnimator.ofFloat(itemView, "translationY", -100f, 0f).apply {
                duration = 1000  // Dauer der Animation in Millisekunden
                interpolator =
                    BounceInterpolator()  // Verwendet den BounceInterpolator für den Bounce-Effekt
                start()
            }
        }

    }

    /**
     * Ein Callback für die DiffUtil, der bestimmt, ob zwei Integrationskurse die gleichen sind.
     * Es wird verwendet, um die Änderungen in der Integrationskursliste effizient zu berechnen.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<TerminResponse>() {

        /**
         * Überprüft, ob zwei Integrationskurse die gleiche ID haben.
         *
         * @param oldItem: Der alte Integrationskurs.
         * @param newItem: Der neue Integrationskurs.
         * @return :True, wenn die beiden Integrationskurse die gleiche ID haben, sonst false.
         */
        override fun areItemsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Überprüft, ob zwei Integrationskurse die gleichen Daten haben.
         *
         * @param oldItem: Der alte Integrationskurs.
         * @param newItem: Der neue Integrationskurs.
         * @return :True, wenn die beiden Integrationskurse die gleichen Daten haben, sonst false.
         */
        override fun areContentsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem == newItem
        }
    }
}