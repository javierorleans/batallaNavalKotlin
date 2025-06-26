package com.example.batallanaval

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class RankingActivity : AppCompatActivity() {

    private lateinit var listViewRanking: ListView
    private lateinit var btnVolverInicio: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        btnVolverInicio = findViewById(R.id.btn_volverInicio)

        btnVolverInicio.setOnClickListener {
            val i = Intent(this, InicioActivity::class.java)
            startActivity(i)
            finish()
        }

        mostrarRanking()
    }

    private fun mostrarRanking() {
        val archivo = File(filesDir, "ranking.txt")
        val contenedor = findViewById<LinearLayout>(R.id.listaRanking)
        contenedor.removeAllViews() // Limpia por si se recarga

        if (archivo.exists()) {
            val lineas = archivo.readLines()
            if (lineas.isEmpty()) {
                agregarTexto(contenedor, getString(R.string.no_jugadores_ranking))
                return
            }

            lineas.forEachIndexed { index, linea ->
                val partes = linea.split("::")
                if (partes.size == 2) {
                    val nombre = partes[0]
                    val puntaje = partes[1]
                    agregarTexto(contenedor, "${index + 1}. $nombre - $puntaje ${getString(R.string.puntos)}")
                }
            }
        } else {
            agregarTexto(contenedor, getString(R.string.no_ranking_disponible))
        }
    }

    private fun agregarTexto (contenedor: LinearLayout, texto: String) {
        val tv = TextView(this)
        tv.text = texto
        tv.textSize = 18f
        tv.setPadding(0, 12, 0, 12)
        tv.gravity = Gravity.CENTER
        contenedor.addView(tv)
    }

}