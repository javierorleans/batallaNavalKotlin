package com.example.batallanaval

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val btn_ingresar = findViewById<Button>(R.id.btn_ingresar);
        val enlace_ayuda = findViewById<TextView>(R.id.ayuda_text);

        btn_ingresar.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

        enlace_ayuda.setOnClickListener {
            val i = Intent(this, AyudaActivity::class.java)
            startActivity(i)
        }

    }
}