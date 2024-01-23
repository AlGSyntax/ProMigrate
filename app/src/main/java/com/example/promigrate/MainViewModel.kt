package com.example.promigrate

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class MainViewModel:ViewModel() {

    val auth = Firebase.auth

    val firestor = Firebase.firestore
    val storage = Firebase.storage

    fun register(email: String, password:String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            if(it.isSuccessful){

            }else{

            }
        }
    }




    fun login (email: String,password: String){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
            if(it.isSuccessful){

            }else{

            }
        }
    }
}