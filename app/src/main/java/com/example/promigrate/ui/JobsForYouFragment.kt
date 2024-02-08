package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentJobsForYouBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class JobsForYouFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jobs_for_you, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentJobsForYouBinding.bind(view)
        binding.floatingActionButton.setOnClickListener {
            fetchJobs("token", "Softwareentwickler", "Berlin", "Informatik")
        }

    }

    //TODO Couroutine Scope für Netzwerkanfragen



    private fun fetchOAuthToken() {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", "c003a37f-024f-462a-b36d-b001be4cd24a")
            .add("client_secret", "32a39620-32b3-4307-9aa1-511e3d7f48a8")
            .build()
        val request = Request.Builder()
            .url("https://rest.arbeitsagentur.de/oauth/gettoken_cc")
            .post(requestBody)
            .build()

        var token: String? = null

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OAuthTokenFetch", "Fehler beim Abrufen des Tokens: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonObject = responseBody?.let { JSONObject(it) }
                if (jsonObject != null) {
                    token = jsonObject.getString("access_token")
                }
                Log.d("OAuthTokenFetch", "Token erfolgreich abgerufen: $token")
            }
        })
    }

    fun fetchJobs(token: String, was: String?, wo: String?, berufsfeld: String?) {
        val client = OkHttpClient()
        val urlBuilder = "https://rest.arbeitsagentur.de/jobboerse/jobsuche-service/pc/v4/jobs".toHttpUrlOrNull()!!.newBuilder()

        // Füge die optionalen Parameter hinzu, wenn sie nicht null sind
        was?.let { urlBuilder.addQueryParameter("was", it) }
        wo?.let { urlBuilder.addQueryParameter("wo", it) }
        berufsfeld?.let { urlBuilder.addQueryParameter("berufsfeld", it) }

        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "eyJhbGciOiJIUzUxMiJ9.eyAic3ViIjogIk1uMFNkV1JyOEQyR241eERVMjBVVnJDaks2WT0iLCAiaXNzIjogIk9BRyIsICJpYXQiOiAxNzA3MzY4ODM3LCAiZXhwIjogMS43MDczNzI0MzdFOSwgImF1ZCI6IFsgIk9BRyIgXSwgIm9hdXRoLnNjb3BlcyI6ICJ3ZWJzc29fbG9naW4sIGFhc19hYXMsIHBvcnRhbF9mZWVkYmFjay1zZXJ2aWNlLCBwb3J0YWxfbWV0YXN1Z2dlc3Qtc2VydmljZSwgam9iYm9lcnNlX2thdGFsb2dlLXNlcnZpY2UsIGlkYWFzX2lkLWFhcy1zZXJ2aWNlLCBhcG9rX2tvbnRha3Qtc2VydmljZSwgYWFzX2FwaSwgbW9iaWxlLWFwcHNfam9ic3VjaGUtZ2VvZGF0ZW4sIGFwb2tfbWV0YXN1Z2dlc3QsIGpvYmJvZXJzZV9zdWdnZXN0LXNlcnZpY2UsIGFwb2tfaGYtdjIsIHZlcm1pdHRsdW5nX2FnLWRhcnN0ZWxsdW5nLXNlcnZpY2UsIGlkYWFzX2lkLWFhcy1hcGksIGt1c29zX2t1c29zLXB1YmxpYy1zZXJ2aWNlLCBqb2Jib2Vyc2Vfam9ic3VjaGUtc2VydmljZSwgYXBva19oZiwgaGVhZGVyZm9vdGVyX2hmLXY0LCBqb2Jib2Vyc2VfcHJvZmlsLXNlcnZpY2UsIGFwb2tfaGYtdjMsIG1vYmlsZWFwcHNfam9ic3VjaGUtZ2VvZGF0ZW4sIGFwb2tfaGYtdjQsIGhlYWRlcmZvb3Rlcl9oZi12MyIsICJvYXV0aC5jbGllbnRfaWQiOiAiYzAwM2EzN2YtMDI0Zi00NjJhLWIzNmQtYjAwMWJlNGNkMjRhIiB9.Ve8rLRhvH3UZX-0y_OlFktTX6XDwM50cbph7tuBs4z0d77SsOY0_quR9VwTJ5rQ6Eqx-gzsDIkSfKNCfeKFqXw") // Der Token wird im Authorization-Header gesendet
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("JobFetch", "Fehler beim Abrufen der Jobs: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("JobFetch", "Jobs erfolgreich abgerufen: $responseBody")
                } else {
                    Log.e("JobFetch", "HTTP-Fehler beim Abrufen der Jobs: ${response.code}")
                }
            }
        })
    }



}
