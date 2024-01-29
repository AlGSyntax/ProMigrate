package com.example.promigrate.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentLogInBinding


class LoginFragment : Fragment() {

    private lateinit var viewmodel: MainViewModel
    private lateinit var binding: FragmentLogInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewmodel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.loginBTN.setOnClickListener {
            viewmodel.login(
                binding.emailET.text.toString(),
                binding.passwordET.text.toString()
            )
        }

        binding.registerBTN.setOnClickListener {
            viewmodel.register(
                binding.emailET.text.toString(),
                binding.passwordET.text.toString()
            )
        }

        /**
        viewmodel.user.observe(viewLifecycleOwner){
            if(it != null){
                //User ist eingeloggt
                findNavController().navigate(R.id.loginBTN)
            }
        }
**/


    }

}