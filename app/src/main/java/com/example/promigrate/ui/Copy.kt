
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