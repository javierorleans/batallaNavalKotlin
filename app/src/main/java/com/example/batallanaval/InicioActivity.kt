package com.example.batallanaval

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Oculta el ActionBar solo en esta activity
        supportActionBar?.hide()
        setContentView(R.layout.activity_inicio)

        val btn_ingresar = findViewById<Button>(R.id.btn_ingresar);
        val enlace_ayuda = findViewById<TextView>(R.id.ayuda_text);

        val nombreInput = findViewById<EditText>(R.id.input_usuario)
        val spinner = findViewById<Spinner>(R.id.spinner_tablero)

        btn_ingresar.setOnClickListener {
            val nombreUsuario = nombreInput.text.toString().trim()

            if (nombreUsuario.isEmpty()) {
                nombreInput.error = getString(R.string.nombre_obligatorio)
                return@setOnClickListener
            }

            val opcionSeleccionada = spinner.selectedItem.toString()

            val i = Intent(this, MainActivity::class.java)
            i.putExtra("nombre_usuario", nombreUsuario)
            i.putExtra("tamaño_tablero", opcionSeleccionada)
            startActivity(i)
        }

        enlace_ayuda.setOnClickListener {
            val i = Intent(this, AyudaActivity::class.java)
            startActivity(i)
        }

    }
}