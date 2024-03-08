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
import com.example.promigrate.adapter.RelocationToDoListAdapter
import com.example.promigrate.data.model.ToDoItemRelocation
import com.example.promigrate.databinding.FragmentRelocationAndIntegrationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class RelocationAndIntegrationFragment : Fragment() {

    private lateinit var binding: FragmentRelocationAndIntegrationBinding
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var toDoListAdapter: RelocationToDoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRelocationAndIntegrationBinding.inflate(inflater, container, false)
        initToDoListAdapter()
        return binding.root
    }

    private fun initToDoListAdapter() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""
        toDoListAdapter = RelocationToDoListAdapter(
            onItemClicked = { /* Hier könnte eine Detailansicht für das To-Do-Item implementiert werden */ },
            onItemEdit = { toDoItem -> editToDoItem(userId, toDoItem) },
            onItemDelete = { toDoItem -> viewModel.deleteToDoItem(userId, toDoItem.id) }
        )

        binding.rvtodoreloc.layoutManager = LinearLayoutManager(context)
        binding.rvtodoreloc.adapter = toDoListAdapter
    }

    private fun editToDoItem(userId: String, toDoItem: ToDoItemRelocation) {
        val editText = EditText(context)
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.edit_todo_hint))
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val todoId = toDoItem.id
                viewModel.updateToDoTextRelocation(userId, todoId, editText.text.toString())
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addToDoItem(userId: String) {
        val editText = EditText(context)
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.add_todo_hint))
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newText = editText.text.toString()
                val newToDoId = UUID.randomUUID().toString()
                val isCompleted = false // or true, depending on your logic
                viewModel.updateToDoItem(userId, newToDoId, isCompleted, newText)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        viewModel.getToDoItems(userId).observe(viewLifecycleOwner) { toDoItems ->
            toDoListAdapter.submitList(toDoItems)
        }

        binding.addtodoButton.setOnClickListener {
            addToDoItem(userId)
        }

        binding.findIntegrationCourseButton.setOnClickListener {
            findNavController().navigate(RelocationAndIntegrationFragmentDirections.actionRelocationAndIntegrationFragmentToIntegrationCourseFragment())
        }

        binding.backButton.setOnClickListener {
            val action = RelocationAndIntegrationFragmentDirections.actionRelocationAndIntegrationFragmentToDashboardFragment()
            findNavController().navigate(action)
        }
    }
}