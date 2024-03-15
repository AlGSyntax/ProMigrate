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
import com.example.promigrate.adapter.LanguageCourseAdapter.DiffCallback
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.LanguageCourseItemBinding
import java.util.Date
import java.util.Locale

/**
 * LanguageCourseAdapter ist eine Unterklasse von ListAdapter.
 * Dieser Adapter ist verantwortlich für die Darstellung und Interaktion mit der Liste der Sprachkurse in der Anwendung.
 * Er stellt eine Liste von TerminResponse-Objekten dar, die die Daten für jeden Sprachkurs enthalten.
 * Die Daten für die Sprachkurse werden von einem ViewModel bereitgestellt.
 *
 * @property DiffCallback: Ein DiffUtil.ItemCallback, der zwei TerminResponse-Objekte vergleicht, um effiziente Updates in der RecyclerView zu ermöglichen.
 * Dieser Callback optimiert die Aktualisierungen, indem er nur die geänderten Elemente neu rendert.
 */
class LanguageCourseAdapter :
    ListAdapter<TerminResponse, LanguageCourseAdapter.LanguageCourseViewHolder>(DiffCallback) {

    /**
     * Diese Methode ist Teil des RecyclerView.Adapter-Lebenszyklus und wird verwendet, um einen neuen ViewHolder zu erstellen.
     * Sie bläht das Layout für jedes Element der RecyclerView auf und erstellt einen ViewHolder, um das aufgeblähte Layout zu halten.
     * Der ViewHolder wird dann verwendet, um die Daten für jedes Element zu füllen.
     *
     * @param parent: Die ViewGroup, in die die neue Ansicht hinzugefügt wird, nachdem sie an eine Adapterposition gebunden wurde.
     * @param viewType: Der Ansichtstyp der neuen Ansicht.
     * @return LanguageCourseViewHolder: Gibt einen neuen ViewHolder zurück, der die Ansicht für jedes Element hält.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageCourseViewHolder {
        val binding =
            LanguageCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageCourseViewHolder(binding)
    }


    /**
     * Diese Methode ist Teil des RecyclerView.Adapter-Lebenszyklus und wird verwendet, um die Daten eines Elements an einen ViewHolder zu binden.
     * Sie wird aufgerufen, um die Daten für ein neues Element, das in der RecyclerView angezeigt wird, zu füllen.
     *
     * @param holder: Der ViewHolder, der die Daten für das Element hält.
     * @param position: Die Position des Elements in der Datenliste.
     */
    override fun onBindViewHolder(holder: LanguageCourseViewHolder, position: Int) {
        val kurs = getItem(position)
        holder.bind(kurs)
    }

    /**
     * LanguageCourseViewHolder ist eine Unterklasse von RecyclerView.ViewHolder.
     * Ein ViewHolder hält die Ansicht für ein RecyclerView-Element und ermöglicht es, die Daten für dieses Element zu füllen.
     *
     * @property binding: Das Binding-Objekt, das Zugriff auf die Ansichten im Layout ermöglicht.
     */
    inner class LanguageCourseViewHolder(
        val binding: LanguageCourseItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        /**
         * Diese Methode wird verwendet, um die Daten eines Sprachkurses an einen ViewHolder zu binden.
         * Sie füllt die Ansichten im ViewHolder mit den Daten aus dem Sprachkurs.
         *
         * @param kurs: Der Sprachkurs, dessen Daten an den ViewHolder gebunden werden.
         */
        fun bind(kurs: TerminResponse) {

            // Setzt den Kurs-Titel. Wenn kein Titel vorhanden ist, wird "N/A" angezeigt.
            binding.langcourseTextView.text = kurs.angebot?.titel ?: "N/A"


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
                binding.langcourseTextView.setOnClickListener {
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
         * Diese Methode wird aufgerufen, um eine Bounce-Animation auf dem ViewHolder auszuführen.
         * Die Animation verändert die Y-Position des ViewHolders, um einen Bounce-Effekt zu erzeugen.
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
     * Dies ist ein Begleitobjekt namens DiffCallback, das DiffUtil.ItemCallback<TerminResponse> erweitert.
     * DiffUtil ist eine Hilfsklasse, die den Unterschied zwischen zwei Listen berechnet und eine Liste von Update-Operationen ausgibt,
     * die die erste Liste in die zweite umwandelt.
     * Es kann verwendet werden, um Updates für einen RecyclerView Adapter zu berechnen.
     * DiffUtil verwendet Eugene W. Myers's Differenzalgorithmus, um die minimale Anzahl von Updates zu berechnen, die benötigt werden, um eine Liste in eine andere umzuwandeln.
     *
     */
    companion object DiffCallback : DiffUtil.ItemCallback<TerminResponse>() {

        /**
         * Wird aufgerufen, um zu überprüfen, ob zwei Objekte das gleiche Element darstellen.
         * Wenn Ihre Elemente beispielsweise eindeutige IDs haben, sollte diese Methode ihre ID-Gleichheit überprüfen.
         *
         * @param oldItem Das TerminResponse-Element aus der alten Liste.
         * @param newItem Das TerminResponse-Element aus der neuen Liste.
         * @return True, wenn die beiden Elemente das gleiche Objekt darstellen, oder false, wenn sie unterschiedlich sind.
         */
        override fun areItemsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Wird aufgerufen, um zu überprüfen, ob zwei Elemente die gleichen Daten haben.
         * Diese Information wird verwendet, um zu erkennen, ob sich der Inhalt eines Elements geändert hat.
         * Diese Methode wird nur aufgerufen, wenn areItemsTheSame(T, T) für diese Elemente true zurückgibt.
         *
         * @param oldItem Das TerminResponse-Element aus der alten Liste.
         * @param newItem Das TerminResponse-Element aus der neuen Liste.
         * @return True, wenn der Inhalt der Elemente gleich ist, oder false, wenn sie unterschiedlich sind.
         */
        override fun areContentsTheSame(oldItem: TerminResponse, newItem: TerminResponse): Boolean {
            return oldItem == newItem
        }
    }
}