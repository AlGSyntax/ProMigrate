package com.example.promigrate.adapter

import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.R
import com.example.promigrate.data.model.JobDetailsResponse
import com.example.promigrate.databinding.ToDoResearchItemBinding
import androidx.core.view.isVisible
import io.noties.markwon.Markwon


/**
 * Ein Adapter für die RecyclerView, der eine Liste von Paaren, bestehend aus einem Jobtitel und einer Referenznummer,
 * verwaltet. Jeder Eintrag in der Liste repräsentiert einen Job, auf den der Benutzer klicken kann, um weitere
 * Details zu erhalten.
 *
 * @param onItemClicked: Ein Lambda-Ausdruck, der aufgerufen wird, wenn auf ein Item in der Liste geklickt wird.
 *                      Der Lambda-Ausdruck übergibt die Referenznummer des angeklickten Jobs.
 */
class DetailToDoJobResearchAdapter(private val onItemClicked: (String) -> Unit) :
    ListAdapter<Pair<String, String>, DetailToDoJobResearchAdapter.DetailToDoJobResearchViewHolder>(
        JobDiffCallback
    ) {

    // Diese Map speichert die Details zu den Jobangeboten, die aus der API abgerufen wurden.
    // Der Schlüssel ist die Referenznummer des Jobangebots (refNr), und der Wert ist ein JobDetailsResponse-Objekt,
    // das alle relevanten Details zu dem spezifischen Jobangebot enthält.
    private var jobDetailsMap = mutableMapOf<String, JobDetailsResponse>()

    /**
     * Aktualisiert die Detailinformationen für einen spezifischen Job im Adapter. Wenn die Referenznummer
     * eines Jobs in der aktuellen Liste gefunden wird, werden die zugehörigen Details aktualisiert und
     * die Ansicht für diesen spezifischen Eintrag in der RecyclerView wird benachrichtigt, um die Änderungen anzuzeigen.
     *
     * @param refNr: Die Referenznummer des Jobs, dessen Details aktualisiert werden sollen.
     * @param details: Die detaillierten Informationen zum Job, die aktualisiert werden sollen.
     */
    fun setJobDetails(refNr: String, details: JobDetailsResponse) {
        jobDetailsMap[refNr] = details
        val position = currentList.indexOfFirst { it.second == refNr }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    /**
     * Erstellt einen neuen ViewHolder für den Adapter. Diese Methode wird von der RecyclerView aufgerufen,
     * wenn ein neuer ViewHolder benötigt wird. Dies passiert, wenn die RecyclerView zum ersten Mal angezeigt wird
     * oder wenn neue Items in die Liste eingefügt werden.
     *
     * @param parent: Die ViewGroup, zu der die neue View hinzugefügt wird, nachdem sie an eine Position gebunden wurde.
     * @param viewType: Der View-Typ der neuen View.
     * @return: Eine neue Instanz des DetailToDoJobResearchViewHolder, die die gebundene Ansicht enthält.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailToDoJobResearchViewHolder {
        val binding =
            ToDoResearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailToDoJobResearchViewHolder(binding, onItemClicked)
    }

    /**
     * Bindet die Daten eines Job-Elements an den gegebenen ViewHolder, einschließlich der Aktualisierung
     * mit spezifischen Jobdetails, falls verfügbar.
     *
     * @param holder:   Der ViewHolder, der die Job-Daten halten soll.
     * @param position: Die Position des Job-Elements in der Datenliste.
     */
    override fun onBindViewHolder(holder: DetailToDoJobResearchViewHolder, position: Int) {
        val job = getItem(position)
        // Bindet den Jobtitel und die Referenznummer an den ViewHolder.
        holder.bind(job, jobDetailsMap[job.second])
    }


    /**
     * Der ViewHolder für ein Jobelement im DetailToDoJobResearchAdapter.
     * Diese innere Klasse bindet die Jobdaten und Jobdetails an die entsprechenden Views.
     *
     * @property binding: Das Binding-Objekt für das Item-Layout, das den Zugriff auf die UI-Komponenten ermöglicht.
     * @property onItemClicked: Ein Callback, das ausgelöst wird, wenn auf ein Element geklickt wird.
     */
    inner class DetailToDoJobResearchViewHolder(
        private val binding: ToDoResearchItemBinding,
        private val onItemClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val markwon: Markwon = Markwon.create(binding.root.context)

        /**
         * Bindet die Daten eines Jobs und die zugehörigen Details (falls vorhanden) an die Ansicht.
         *
         * @param job: Die Basisinformationen des Jobs (Titel und Referenznummer).
         * @param details: Die Details des Jobs, wie z.B. Arbeitgeber, Ort, Arbeitszeitmodell usw.
         */
        fun bind(job: Pair<String, String>, details: JobDetailsResponse?) {
            Log.d(
                "Adapter",
                "arbeitsorte=${details?.arbeitsorte?.size}  ort=${
                    details?.arbeitsorte?.firstOrNull()?.adresse?.ort
                }  str=${details?.arbeitsorte?.firstOrNull()?.adresse?.strasse}"
            )
            binding.textViewTitle.text = job.first

            binding.hiddenView.visibility = if (details != null) View.VISIBLE else View.GONE
            details?.let {
                val firstOrt = it.arbeitsorte?.firstOrNull()

                val streetCombined = when {
                    firstOrt?.adresse?.strasse     != null &&
                            firstOrt.adresse.strasseHausnummer    != null -> "${firstOrt.adresse.strasse} ${firstOrt.adresse.strasseHausnummer}"
                    firstOrt?.adresse?.strasse     != null ->  firstOrt.adresse.strasse
                    else -> null
                }


                binding.textViewEmployer.text =
                    binding.root.context.getString(R.string.employer, it.arbeitgeber ?: "N/A")
                binding.textViewLocation.text = binding.root.context.getString(
                    R.string.location,
                    firstOrt?.adresse?.ort ?: "N/A"
                )
                binding.textViewEmployerAddress.text = binding.root.context.getString(
                    R.string.employeraddress,
                    streetCombined ?: "N/A"
                )
                binding.textViewWorkModel.text = binding.root.context.getString(
                    R.string.work_model,
                    it.arbeitszeitmodelle?.joinToString(", ") ?: "N/A"
                )
                binding.textViewContract.text = binding.root.context.getString(
                    R.string.contract,
                    getBefristung(details.befristung)
                )
                binding.textViewSalary.text =
                    binding.root.context.getString(R.string.salary, it.verguetung ?: "N/A")
                val markdownDesc = it.stellenbeschreibung ?: "N/A"
                markwon.setMarkdown(binding.textViewDescription, markdownDesc)
                binding.textViewDescription.movementMethod = LinkMovementMethod.getInstance()
                binding.textViewBranch.text =
                    binding.root.context.getString(R.string.branch, it.branche ?: "N/A")
                binding.textViewJob.text =
                    binding.root.context.getString(R.string.job, it.beruf ?: "N/A")
            }


            /**
             * Setzt einen OnClickListener für das gesamte Item, um auf Klickereignisse zu reagieren.
             * Neues Verhalten: Sichtbarkeit toggeln nur, wenn Details bereits geladen, sonst Placeholder anzeigen.
             */
            binding.root.setOnClickListener {
                val hasDetails = jobDetailsMap.containsKey(job.second)

                if (hasDetails) {
                    // Daten bereits geladen - es wird die Ansicht toggeln und die Details anzeigen/verstecken
                    binding.hiddenView.visibility =
                        if (binding.hiddenView.isVisible) View.GONE else View.VISIBLE
                } else {
                    // Daten noch nicht geladen - es wird ein Placeholder angezeigt
                    binding.textViewEmployer.text = binding.root.context.getString(R.string.welcome_text)
                }

                // In jedem Fall Details anfordern
                onItemClicked(job.second)
            }
        }


        /**
         * Übersetzt den Befristungscode eines Jobs in einen für den Benutzer lesbaren Text.
         *
         * @param befristungsCode Der Befristungscode, der von der API oder Datenquelle bereitgestellt wird.
         * @return Der lesbare Text, der die Befristung des Jobs beschreibt.
         */
        private fun getBefristung(befristungsCode: String?): String {
            // Überprüft den Befristungscode und gibt den entsprechenden lesbaren Text zurück.
            return when (befristungsCode) {
                "UNBEFRISTET" -> binding.root.context.getString(R.string.unlimited)// Unbefristet.
                "BEFRISTET" -> binding.root.context.getString(R.string.limited)// Befristet.
                else -> binding.root.context.getString(R.string.unknown)// Unbekannt, falls der Code nicht erkannt wird.
            }
        }


    }

    /**
     * Ein Callback für die Berechnung der Differenz zwischen zwei nicht-null Elementen in einer Liste.
     *
     * Diese Implementierung von DiffUtil.ItemCallback vergleicht Pairs von Strings,
     * wobei jedes Pair aus einem Jobtitel und einer Referenznummer besteht.
     */
    companion object JobDiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {


        /**
         * Überprüft, ob zwei Objekte das gleiche Item repräsentieren.
         *
         * @param oldItem: Das Item in der alten Liste.
         * @param newItem: Das Item in der neuen Liste.
         * @return: True, wenn die beiden Items das gleiche Datenobjekt repräsentieren, sonst False.
         */
        override fun areItemsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            // Vergleicht die ersten Elemente des Paares (Jobtitel), um die Identität zu bestimmen.
            return oldItem.second == newItem.second
        }

        /**
         * Überprüft, ob zwei Items denselben Inhalt haben.
         *
         * @param oldItem: Das Item in der alten Liste.
         * @param newItem: Das Item in der neuen Liste.
         * @return: True, wenn die Inhalte der Items identisch sind, sonst False.
         */
        override fun areContentsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            // Vergleicht die gesamten Pairs, um festzustellen, ob sich der Inhalt geändert hat.
            return oldItem == newItem
        }
    }
}
