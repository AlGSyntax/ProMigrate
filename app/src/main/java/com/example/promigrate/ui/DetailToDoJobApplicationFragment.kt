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
import com.example.promigrate.data.model.JobWithToDoItems
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.FragmentDetailToDoJobApplicationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID



class DetailToDoJobApplicationFragment : Fragment() {


    private val viewModel: MainViewModel by activityViewModels()
    private  var binding: FragmentDetailToDoJobApplicationBinding? = null

    private lateinit var adapter: DetailToDoJobApplicationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailToDoJobApplicationBinding.inflate(inflater, container, false)
        initAdapter()
        return binding!!.root
    }


    private fun initAdapter() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        adapter = DetailToDoJobApplicationAdapter(
            { jobId -> addToDoItem(userId, jobId) },
            { jobId, todoId, currentText -> editToDoItem(userId, jobId, todoId, currentText) },
            { jobId, _ -> deleteJobItemItem(jobId) }
        )
    }

    private fun deleteJobItemItem(jobId: String) {
        MaterialAlertDialogBuilder(requireContext(),R.style.CustomAlertDialog)
            .setMessage(getString(R.string.confirmdeletion))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                // Benutzer bestätigt die Löschung, rufe die Löschmethode im ViewModel auf
                viewModel.deleteJobSelection(jobId)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }



    private fun editToDoItem(userId: String, jobId: String, todoId: String, currentText: String) {
        val editText = EditText(context).apply { setText(currentText) }

        MaterialAlertDialogBuilder(requireContext(),R.style.CustomAlertDialog)
            .setTitle(getString(R.string.edit_todo_hint))
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                viewModel.updateToDoText(userId, jobId, todoId, editText.text.toString())
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addToDoItem(userId: String, jobId: String) {
        val editText = EditText(context)

        MaterialAlertDialogBuilder(requireContext(),R.style.CustomAlertDialog)
            .setTitle(getString(R.string.add_todo_hint))
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newText = editText.text.toString()
                val newToDoId = generateNewToDoId()
                viewModel.updateToDoItemForJob(userId, jobId, newToDoId, false, newText)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.rvJobs.layoutManager = LinearLayoutManager(context)
        binding!!.rvJobs.adapter = adapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        viewModel.userProfileData.observe(viewLifecycleOwner) { profile ->
            val jobTitles = profile?.selectedJobs?.keys?.toList() ?: emptyList()

            // Initialisiere eine Map, um die ToDoItems für jeden Job zu speichern
            val jobWithToDoItemsMap = mutableMapOf<String, List<ToDoItem>>()

            jobTitles.forEach { refNr ->
                viewModel.getToDoItemsForJob(userId, refNr).observe(viewLifecycleOwner) { toDoItems ->
                    // Aktualisiere die Map mit den neuen ToDoItems
                    jobWithToDoItemsMap[refNr] = toDoItems

                    // Erstelle eine Liste von JobWithToDoItems basierend auf der aktualisierten Map
                    val jobWithToDoItemsList = jobWithToDoItemsMap.map { entry ->
                        JobWithToDoItems(entry.key, entry.value)
                    }

                    // Aktualisiere die Liste im Adapter
                    adapter.submitList(jobWithToDoItemsList)
                }
            }

        }


        binding!!.restartOnboardingButton.setOnClickListener {
            findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_viewPagerFragment)
        }

        binding!!.backtodashbtn.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.detailToDoJobApplicationFragment) {
                findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_dashboardFragment)
            }
        }
    }

    private fun generateNewToDoId(): String {
        // Implementieren Sie eine Methode, um eine eindeutige ID zu generieren
        return UUID.randomUUID().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}