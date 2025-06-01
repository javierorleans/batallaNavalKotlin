package com.example.batallanaval

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AyudaActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ayuda)
        val btn_volver = findViewById<Button>(R.id.btn_volver)

        btn_volver.setOnClickListener{
            this.finish()
        }
    }


}