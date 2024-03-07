/**
 *
 *package com.example.promigrate.data.remote
 *
 * import android.content.Context
 * import com.example.promigrate.data.model.BildungsangebotResponse
 * import com.squareup.moshi.Moshi
 * import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
 * import okhttp3.OkHttpClient
 * import okhttp3.logging.HttpLoggingInterceptor
 * import retrofit2.Response
 * import retrofit2.Retrofit
 * import retrofit2.converter.moshi.MoshiConverterFactory
 * import retrofit2.http.GET
 * import retrofit2.http.Query
 *
 * interface ProMigrateLangLearnAPIService {
 *
 *     @GET("infosysbub/sprachfoerderung/pc/v1/bildungsangebot")
 *     suspend fun getBildungsangebot(
 *         @Query("systematiken") systematiken: String,
 *         @Query("orte") orte: String,
 *         @Query("sprachniveau") sprachniveau: String,
 *     ): Response<BildungsangebotResponse>
 *
 *
 * }
 *
 * object ProMigrateLangLearnAPI {
 *
 *
 *     private const val BASE_URL = "https://rest.arbeitsagentur.de/"
 *
 *
 *     private lateinit var okHttpClient: OkHttpClient
 *
 *     private lateinit var moshi: Moshi
 *
 *     lateinit var retrofitService: ProMigrateLangLearnAPIService
 *
 *     fun init(context: Context) {
 *         val loggingInterceptor = HttpLoggingInterceptor().apply {
 *             level = HttpLoggingInterceptor.Level.BODY
 *         }
 *
 *         moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
 *
 *         okHttpClient = OkHttpClient.Builder()
 *             .addInterceptor(loggingInterceptor)
 *             .addInterceptor(LanguageCourseAuthTokenInterceptor(context.applicationContext))
 *             .build()
 *
 *         val retrofit = Retrofit.Builder()
 *             .baseUrl(BASE_URL)
 *             .client(okHttpClient)
 *             .addConverterFactory(MoshiConverterFactory.create(moshi))
 *             .build()
 *
 *
 *         retrofitService = retrofit.create(ProMigrateLangLearnAPIService::class.java)
 *     }
 * }
 * ,
 *
 * package com.example.promigrate.data.model
 *
 * data class BildungsangebotResponse(
 *     val _embedded: EmbeddedResponse,
 *     val page: PageResponse
 * )
 *
 * data class EmbeddedResponse(
 *     val termine: List<TerminResponse>
 * )
 *
 * data class TerminResponse(
 *     val id: Long?,
 *     val unterrichtsform: UnterrichtsformResponse,
 *     val beginn: Long?,
 *     val ende: Long?,
 *     val angebot: AngebotResponse?,
 *     var isChecked: Boolean
 * )
 *
 * data class UnterrichtsformResponse(
 *     val id: Int,
 *     val bezeichnung: String
 * )
 *
 * data class AngebotResponse(
 *     val id: Long?,
 *     val titel: String?,
 *     val bildungsanbieter: BildungsanbieterResponse
 * )
 *
 * data class BildungsanbieterResponse(
 *     val id: Long?,
 *     val name: String?,
 *     val adresse: AdresseResponse
 * )
 *
 * data class AdresseResponse(
 *     val ortStrasse: OrtStrasseResponse
 * )
 *
 * data class OrtStrasseResponse(
 *     val name: String?
 * )
 *
 * data class PageResponse(
 *     val size: Int?,
 *     val totalElements: Int?,
 *     val totalPages: Int?,
 *     val number: Int?
 * )
 *,
 *
 * package com.example.promigrate.data.remote
 *
 * import android.content.Context
 * import android.util.Log
 * import okhttp3.FormBody
 * import okhttp3.Interceptor
 * import okhttp3.OkHttpClient
 * import okhttp3.Request
 * import okhttp3.Response
 * import org.json.JSONObject
 * import java.io.IOException
 *
 * class LanguageCourseAuthTokenInterceptor(context: Context) : Interceptor {
 *
 *     private val applicationContext = context.applicationContext
 *
 *     override fun intercept(chain: Interceptor.Chain): Response {
 *         synchronized(this) {
 *             val tokenShouldRefresh = checkIfTokenNeedsRefresh(applicationContext)
 *             if (tokenShouldRefresh) {
 *                 refreshTokenSynchronously(applicationContext)
 *                 Log.d("LanguageCourseAuthTokenInterceptor", "Token was refreshed.")
 *             }
 *         }
 *
 *         val request = chain.request().newBuilder()
 *             .addHeader("Authorization", "Bearer ${getToken(applicationContext)}")
 *             .build()
 *         return chain.proceed(request)
 *     }
 *
 *     private fun checkIfTokenNeedsRefresh(context: Context): Boolean {
 *         val sharedPref = context.getSharedPreferences("LanguageCourseTokenPrefs", Context.MODE_PRIVATE)
 *         val expiryDateMillis = sharedPref.getLong("languageCourseTokenExpiryDateMilli", 0)
 *         // Wenn das aktuelle Datum größer ist als das Ablaufdatum, muss der Token erneuert werden
 *         return System.currentTimeMillis() > expiryDateMillis
 *     }
 *
 *
 *     private fun refreshTokenSynchronously(context: Context) {
 *         val client = OkHttpClient()
 *         val requestBody = FormBody.Builder()
 *             .add("grant_type", "client_credentials")
 *             .add("client_id", "bd24f42e-ad0b-4005-b834-23bb6800dc6c")
 *             .add("client_secret", "6776b89e-5728-4643-8cd5-c93aefb5314b")
 *             .build()
 *         val request = Request.Builder()
 *             .url("https://rest.arbeitsagentur.de/oauth/gettoken_cc")
 *             .post(requestBody)
 *             .build()
 *
 *         try {
 *             val response = client.newCall(request).execute() // Führe den Call synchron aus
 *             if (response.isSuccessful) {
 *                 val responseBody = response.body?.string()
 *                 val jsonObject = JSONObject(responseBody ?: "")
 *                 val token = jsonObject.getString("access_token")
 *                 val expiresIn = jsonObject.getLong("expires_in")
 *                 val expiryDateMillis = System.currentTimeMillis() + expiresIn * 3000
 *                 saveTokenAndExpiryDate(context, token, expiryDateMillis)
 *             } else {
 *                 // Handle Fehlerfall
 *                 Log.e("AuthTokenInterceptor", "Token konnte nicht erneuert werden: ${response.message}")
 *             }
 *         } catch (e: IOException) {
 *             // Handle IO Fehler
 *             Log.e("AuthTokenInterceptor", "Fehler beim Erneuern des Tokens: ${e.message}")
 *         }
 *     }
 *
 *
 *     private fun getToken(context: Context): String {
 *         val sharedPref = context.getSharedPreferences("LanguageCourseTokenPrefs", Context.MODE_PRIVATE)
 *         return sharedPref.getString("languageCourseToken", "") ?: ""
 *     }
 *
 *     private fun saveTokenAndExpiryDate(context: Context, token: String, expiryDateMillis: Long) {
 *         val sharedPref = context.getSharedPreferences("LanguageCourseTokenPrefs", Context.MODE_PRIVATE)
 *         with(sharedPref.edit()) {
 *             putString("languageCourseToken", token)
 *             putLong("languageCourseTokenExpiryDateMillis", expiryDateMillis)
 *             apply()
 *         }
 *     }
 *
 * }










/**
package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.DetailToDoJobApplicationAdapter
import com.example.promigrate.adapter.JobWithToDoItems
import com.example.promigrate.adapter.ToDoListAdapter
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.FragmentDetailToDoJobApplicationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

const val TAG2 = "DetailToDoJobApplication"

class DetailToDoJobApplicationFragment : Fragment() {

    private lateinit var binding: FragmentDetailToDoJobApplicationBinding
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var adapter: DetailToDoJobApplicationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailToDoJobApplicationBinding.inflate(inflater, container, false)
        initAdapter()
        return binding.root
    }

    private fun initAdapter() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        adapter = DetailToDoJobApplicationAdapter(
            { refNr -> addToDoItem(userId, refNr) },
            { jobTitle, todoId,text -> editToDoItem( jobTitle,todoId,text) }
        )
    }

    private fun editToDoItem(jobTitle:String, todoId:String,newtext:String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_todo_item, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextToDoEdit)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.edit_todo_hint))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newText = editText.text.toString()
                // Stelle sicher, dass die übergebene todoId die ID des Dokuments ist, nicht der Textinhalt
                viewModel.updateToDoItemForJob(jobTitle, todoId,false, newText)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }



    private fun addToDoItem(userId: String, refNr: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_todo_item, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextToDoAdd)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.add_todo_hint))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add_todo_hint)) { _, _ ->
                val newText = editText.text.toString()
                val newToDoId = generateNewToDoId()
                // Ensure refNr does not contain any '/' characters
                val sanitizedRefNr = refNr.replace("/", "_")
                // viewModel.updateToDoItemForJob(userId, sanitizedRefNr, newToDoId, false, newText)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = adapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        viewModel.userProfileData.observe(viewLifecycleOwner) { profile ->
            val jobTitles = profile?.selectedJobs?.keys?.toList() ?: emptyList()

            // Initialisiere eine Map, um die ToDoItems für jeden Job zu speichern
            val jobWithToDoItemsMap = mutableMapOf<String, List<ToDoItem>>()

            jobTitles.forEach { jobTitle ->
                Log.e(TAG2, "JobTitle: $jobTitle")
                viewModel.getToDoItemsForJob(userId, jobTitle).observe(viewLifecycleOwner) { toDoItems ->
                    // Aktualisiere die Map mit den neuen ToDoItems
                    jobWithToDoItemsMap[jobTitle] = toDoItems

                    // Erstelle eine Liste von JobWithToDoItems basierend auf der aktualisierten Map
                    val jobWithToDoItemsList = jobWithToDoItemsMap.map { entry ->

                        JobWithToDoItems(entry.key, entry.value)
                    }

                    Log.d("DetailToDoJobApplication", "JobWithToDoItemsList: $jobWithToDoItemsList) ")

                    // Aktualisiere die Liste im Adapter
                    adapter.submitList(jobWithToDoItemsList)
                    // Aktualisiere den Adapter mit den neuen ToDoItems
                }

            }

        }




        binding.restartOnboardingButton.setOnClickListener {
            findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_viewPagerFragment)
        }

        binding.backtodashbtn.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.detailToDoJobApplicationFragment) {
                findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_dashboardFragment)
            }
        }
    }

    private fun generateNewToDoId(): String {
        // Implementieren Sie eine Methode, um eine eindeutige ID zu generieren
        return UUID.randomUUID().toString()
    }
}///

package com.example.promigrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.ToDoApplicationItemBinding

class DetailToDoJobApplicationAdapter(

    private val onItemAdd: (String) -> Unit,
    private val onItemEdit: (String, String,String) -> Unit
) : ListAdapter<JobWithToDoItems, DetailToDoJobApplicationAdapter.JobViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ToDoApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding,  onItemAdd, onItemEdit)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JobViewHolder(
        private val binding: ToDoApplicationItemBinding,
        private val onItemAdd: (String) -> Unit,
        private val onItemEdit: (String,String,String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isListVisible = false

        fun bind(jobWithToDoItems: JobWithToDoItems) {
            binding.jobTitleTextView.text = jobWithToDoItems.jobTitle
            val toDoListAdapter = ToDoListAdapter(
                jobWithToDoItems.jobTitle

            ) { jobTitle, todoId, text -> onItemEdit(jobTitle, todoId, text) }

            binding.jobTitleTextView.setOnClickListener {
                toggleToDoListVisibility()
            }

            binding.addTodoItemButton.setOnClickListener {
                onItemAdd(jobWithToDoItems.jobTitle)
            }

            binding.todoListRecyclerView.adapter = toDoListAdapter
            binding.todoListRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            toDoListAdapter.submitList(jobWithToDoItems.toDoItems)
        }

        private fun toggleToDoListVisibility() {
            isListVisible = !isListVisible
            binding.todoListRecyclerView.visibility = if (isListVisible) View.VISIBLE else View.GONE
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<JobWithToDoItems>() {
        override fun areItemsTheSame(oldItem: JobWithToDoItems, newItem: JobWithToDoItems): Boolean = oldItem.jobTitle == newItem.jobTitle
        override fun areContentsTheSame(oldItem: JobWithToDoItems, newItem: JobWithToDoItems): Boolean = oldItem == newItem
    }


}



data class JobWithToDoItems(
    val jobTitle: String,
    val toDoItems: List<ToDoItem>
)




package com.example.promigrate.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.ToDoListJobApplicationItemBinding



class ToDoListAdapter(
    private val jobTitle : String,
    private val onItemEdit: (String, String,String) -> Unit // Geändert zu (ID, neuer Text)
) : ListAdapter<ToDoItem, ToDoListAdapter.ToDoViewHolder>(ToDoDiffCallback) {

    class ToDoViewHolder(
        private val binding: ToDoListJobApplicationItemBinding,
        private val jobTitle: String,
        private val onItemEdit: (String, String,String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(toDoItem: ToDoItem) {
            Log.d("ToDoListAdapter", "Binding item with todoId: ${toDoItem.id}")
            binding.todoItemTextView.text = toDoItem.text
            binding.todoItemCheckbox.isChecked = toDoItem.isCompleted


            binding.editTodoItemButton.setOnClickListener {
                Log.d("ToDoListAdapter", "Edit button clicked. ToDo ID: ${toDoItem.id}, Text: ${toDoItem.text}")
                // Öffne hier nicht direkt den Dialog, sondern rufe den Callback auf
                onItemEdit(jobTitle,toDoItem.id ,toDoItem.text) // ID und aktueller Text als Parameter
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = ToDoListJobApplicationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding,jobTitle,  onItemEdit)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object ToDoDiffCallback : DiffUtil.ItemCallback<ToDoItem>() {
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean = oldItem == newItem
    }



}*/