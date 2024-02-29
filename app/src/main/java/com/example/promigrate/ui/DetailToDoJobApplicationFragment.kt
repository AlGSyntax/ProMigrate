package com.example.promigrate.ui

import android.os.Bundle
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
            { jobId, todoId, isCompleted -> viewModel.updateToDoItemForJob(userId, jobId, todoId, isCompleted, "Item Text") },
            { jobId -> addToDoItem(userId, jobId) }
        )
    }

    private fun editToDoItem(userId: String, jobId: String, todoId: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_todo_item, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextToDoEdit)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.edit_todo_hint))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newText = editText.text.toString()
                viewModel.updateToDoText(userId, jobId, todoId, newText)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addToDoItem(userId: String, jobId: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_todo_item, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextToDoAdd)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.add_todo_hint))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add_todo_hint)) { _, _ ->
                val newText = editText.text.toString()
                val newToDoId = generateNewToDoId()
                viewModel.updateToDoItemForJob(userId, jobId, newToDoId, false, newText)
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
                viewModel.getToDoItemsForJob(userId, jobTitle).observe(viewLifecycleOwner) { toDoItems ->
                    // Aktualisiere die Map mit den neuen ToDoItems
                    jobWithToDoItemsMap[jobTitle] = toDoItems

                    // Erstelle eine Liste von JobWithToDoItems basierend auf der aktualisierten Map
                    val jobWithToDoItemsList = jobWithToDoItemsMap.map { entry ->
                        JobWithToDoItems(entry.key, entry.value)
                    }

                    // Aktualisiere die Liste im Adapter
                    adapter.submitList(jobWithToDoItemsList)
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
}