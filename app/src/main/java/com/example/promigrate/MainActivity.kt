package com.example.promigrate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.promigrate.data.remote.ProMigrateAPI
import com.example.promigrate.data.remote.ProMigrateLangLearnAPI

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProMigrateAPI.init(this)
        ProMigrateLangLearnAPI.init(this)
        setContentView(R.layout.activity_main)


    }
}
