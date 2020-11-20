package com.utn.segundoparcial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.fragment_add_dialog.*

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setDisplayShowHomeEnabled(true)
    }


}