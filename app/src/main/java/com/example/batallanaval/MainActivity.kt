package com.example.batallanaval

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.PopupMenu
import android.view.MenuItem
import android.view.View
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var EstadisticasLayout : TextView // Variable para referenciar las estadisticas
    private lateinit var gridLayout: GridLayout //variable para referenciar la grilla
    private lateinit var buttons: Array<Button> //referencia a los botones de la grilla
    private lateinit var restartButton: Button //boton que reinicia el juego
    private var shipPositions: Set<Int> = emptySet() //posición de los barcos en el tablero, set(sin repeticiones)

    // Variables para controlar el juego
    private var barcosTotales = 0
    private var movimientos = 0
    private var aciertos = 0

    // Variables para crear el tablero, por defecto de 6x6
    private var filas: Int = 6
    private var columnas: Int = 6
    private var totalCeldas: Int = 36

    //Variables para crear el temporizador
    private lateinit var temporizador: CountDownTimer
    private var tiempoRestante: Long = 0

    private lateinit var nombreUsuario: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnMenu = findViewById<Button>(R.id.btnMenu)

        btnMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }


        // obtiene lo igresado en el input y lo agrega al navbar
        nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Invitado"
        title = "${getString(R.string.app_name)} | $nombreUsuario"

        // obtiene la opcion elegida en el spinner e inicializa el contador
        val tamañoTablero = intent.getStringExtra("tamaño_tablero") ?: "6x6"
        tiempoRestante = obtenerDuracionTimer(tamañoTablero)
        iniciarTemporizador()

        // vinculamos variables con elementos del xml
        EstadisticasLayout = findViewById(R.id.Estadisticas)
        gridLayout = findViewById(R.id.gridLayout)
        restartButton = findViewById(R.id.restartButton)

        // convierte en int lo ingresado en el spinner para armar tablero
        filas = tamañoTablero.substringBefore("x").toInt()
        columnas = tamañoTablero.substringAfter("x").toInt()
        totalCeldas = filas * columnas
        buttons = Array(totalCeldas) { Button(this) } // Inicializa con botones vacíos por ahora. lo que está entre { es una funcion lambda}

        if (savedInstanceState == null) {
            inicializarJuego()
        } else {
            reconstruirBotonera()
        }

        //boton de reiniciar partida
        restartButton.setOnClickListener {
            reiniciar()
            /*inicializarJuego() //reinicia todas las variables necesarias dela grilla

            val tamañoTablero = intent.getStringExtra("tamaño_tablero") ?: "6x6"
            tiempoRestante = obtenerDuracionTimer(tamañoTablero)

            iniciarTemporizador()*/
        }

    }


    //para salvar los valores y estados
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("barcosTotales", barcosTotales)
        outState.putInt("movimientos", movimientos)
        outState.putInt("aciertos", aciertos)
        outState.putIntegerArrayList("barcos", ArrayList(shipPositions))

        val desactivados = ArrayList<Int>() //crea lista para botones deshabilitados
        val colores = ArrayList<Int>() //crea lista para color de los botones
        // inicia bucle para recorrer los botones
        for (i in buttons.indices) {
            if (!buttons[i].isEnabled) desactivados.add(i) //si esta deshabilitado lo agrega a la lista de deshabilitados
            val color = (buttons[i].background as? ColorDrawable)?.color ?: Color.TRANSPARENT // guarda el color del boton
            colores.add(color) // lo agrega a la lista de colores
        }
        outState.putIntegerArrayList("botonesDesactivados", desactivados) //guarda a lista de btns desactivados
        outState.putIntegerArrayList("coloresBotones", colores) //guarda la lista de colores
    }

    //recuperar valores y estados
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // restaura valores guardados
        barcosTotales = savedInstanceState.getInt("barcosTotales")
        movimientos = savedInstanceState.getInt("movimientos")
        aciertos = savedInstanceState.getInt("aciertos")

        val barcos = savedInstanceState.getIntegerArrayList("barcos") //obtenemos lista de posiciones de barcos
        if (barcos != null) shipPositions = barcos.toSet() //si no esta vacia, la convierto en set para ShipPosition

        val desactivados = savedInstanceState.getIntegerArrayList("botonesDesactivados") ?: arrayListOf() //indices de btns deshabilitados
        val colores = savedInstanceState.getIntegerArrayList("coloresBotones") ?: arrayListOf() //colores de fondo de cada btn

        // Reconstrucción de la grilla con los valores guardados
        gridLayout.post {
            for (i in buttons.indices) { //recorro btns
                val button = buttons[i] //btn en posicion i
                val color = colores.getOrElse(i) { Color.TRANSPARENT } //obtengo color correspondiente al indice
                button.setBackgroundColor(color) //asigno color
                button.isEnabled = i !in desactivados //deshabilita o no el btn dependiendo si el indice esta en "desactivados"
            }
            actualizarEstadisticas()
        }
    }

    //convierte 1cm en pixeles
    private fun cmToPx(cm: Float): Int {
        val metrics = resources.displayMetrics
        return (cm * metrics.xdpi / 2.54f).toInt()
    }


    private fun  inicializarVariablesDelJuego() {
        shipPositions = generarBarcosAleatorios() //crea nuevo conjunto de barcos aleatorios con sus posiciones
        barcosTotales = shipPositions.size // Tamaño del arreglo creado anteriormente
        movimientos = 0
        aciertos = 0
        actualizarEstadisticas()
        // Aunque la primera vez estan en cero, si reinicio el juego sin llamar a esta fun, no se ven en cero hasta que se pulsa una celda
    }


    private fun crearGrillaConBotones() {
        gridLayout.removeAllViews() //borra elementos en el tablero
        gridLayout.rowCount = filas
        gridLayout.columnCount = columnas

        //bucle para crear los botones iniciales
        for (i in buttons.indices) {
            val button = Button(this) //creo boton
            button.text = ""
            button.setBackgroundColor(Color.parseColor("#607D8B"))

            val sizePx = cmToPx(1f) // le doy tamaño

            //diseño del boton
            // Configuro que el ancho de la grilla se adapte al ancho de la pantalla, tanto vertical como horizontal
            // Ya no se ven cuadrados los botones, sino rectangulares. evitamos usar HorizontalScrollView
            val params = GridLayout.LayoutParams().apply{
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }

            params.setMargins(2, 2, 2, 2)

            //asignamos fila y columna al boton en función de i
            val row = i / columnas  //en que fila va
            val col = i % columnas  //en que columna va
            params.rowSpec = GridLayout.spec(row)  //establecer la fila
            params.columnSpec = GridLayout.spec(col, 1f)  //establecer la columna: 1f: ocupá 1 columna y repartí equitativamente el espacio
            button.layoutParams = params //asigno al boton todos los paramentros


            //accion de los botones
            button.setOnClickListener {
                if (shipPositions.contains(i)) {
                    button.setBackgroundColor(Color.RED)    //si la pos de boton es barco, color rojo
                    aciertos++ // Incremento de aciertos
                } else {
                    button.setBackgroundColor(Color.CYAN)   //si la pos de boton eno s barco, color cyan
                }
                button.isEnabled = false // desactivo boton
                movimientos++ //incremento de movimientos

                actualizarEstadisticas() // actualizacion de valores de estadisticas

                if (aciertos == barcosTotales) {
                    // Si se encontraron todos los barcos, se anuncia estadistica
                    //val agua = movimientos - aciertos
                    // val mensajeFinal = getString(R.string.mensaje_final, aciertos, agua)
                    //EstadisticasLayout.text = mensajeFinal

                    // Deshabilitar todos los botones
                    for (btn in buttons) {
                        btn.isEnabled = false
                    }
                    mostrarDialogoVictoria()
                }
            }

            buttons[i] = button //guardamos en la posicion del array
            gridLayout.addView(button) //se muestra boton
        }
    }


    private fun inicializarJuego() {
        inicializarVariablesDelJuego()
        crearGrillaConBotones()
    }


    private fun reconstruirBotonera() {
        crearGrillaConBotones()
    }


    private fun generarBarcosAleatorios(): Set<Int> {
        val cantidad = (10..15).random() //cant de barcos aleatoria
        val posiciones = mutableSetOf<Int>() //conjunto de indices en una lista mutable. No permite repetidos
        while (posiciones.size < cantidad) {
            posiciones.add((0 until totalCeldas).random()) //until indica que no incluye el 36, si debo incluirlos debe ir (0..36)
        } //genero posiciones
        return posiciones //devuelve posiciones generadas
    }

    private fun actualizarEstadisticas(){
        val texto = getString(R.string.estadisticas_texto, movimientos, aciertos, barcosTotales - aciertos)
        EstadisticasLayout.text = texto
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_main, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_inicio -> {
                    this.finish()
                    true
                    /* También se puede usar: intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    * Si InicioActivity ya está en la pila de Activities, destruye todas las activities por encima y la lleva al frente.
                    *  Si no está en la pila, se crea una nueva.
                    *  El FLAG_ACTIVITY_SINGLE_TOP evita que se vuelva a crear si ya está al frente.*/

                }
                R.id.menu_ayuda -> {
                    val intent = Intent(this, AyudaActivity::class.java)
                    intent.putExtra("origen", "Main") // para saber de dónde vino
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // Funcion que va a ser llamada por el botón de Reiniciar como en Volver a Jugar del AlertDialog de fin de juego
    private fun reiniciar(){
        inicializarJuego() //reinicia todas las variables necesarias dela grilla

        val tamañoTablero = intent.getStringExtra("tamaño_tablero") ?: "6x6"
        tiempoRestante = obtenerDuracionTimer(tamañoTablero)

        iniciarTemporizador()
    }
    private fun obtenerDuracionTimer(tamanio: String): Long {
        return when (tamanio) {
            "6x6" -> 20_000L // 20 segundos
            "8x8" -> 25_000L // 25 segundos
            "10x10" -> 30_000L // 30 segundos
            else -> 20_000L // por defecto
        }
    }

    private fun iniciarTemporizador() {
        // cancela el temporizador actual si ya existe y está corriendo
        if (::temporizador.isInitialized) {
            temporizador.cancel()
        }

        temporizador = object : CountDownTimer(tiempoRestante, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tiempoRestante = millisUntilFinished
                actualizarVistaTimer()
            }

            override fun onFinish() {
                mostrarDialogoTiempoAgotado()
            }
        }.start()
    }

    private fun actualizarVistaTimer() {
        val segundos = tiempoRestante / 1000
        val timerText = findViewById<TextView>(R.id.timer_text)
        timerText.text = "⏱ $segundos s"
    }

    private fun mostrarDialogoTiempoAgotado() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_tiempo_agotado_title))
            .setMessage(getString(R.string.dialog_tiempo_agotado_message))
            .setPositiveButton(getString(R.string.jugar_nuevamente)) { _, _ ->
                inicializarJuego()
                recreate()
            }
            .setNegativeButton(getString(R.string.volver_inicio)) { _, _ ->
                finish() // o startActivity(Intent(this, InicioActivity::class.java))
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoVictoria() {
        if (::temporizador.isInitialized) {
            temporizador.cancel() // detener temporizador
        }

        val puntaje = calcularPuntaje()
        val entro = agregarYVerificarSiEntro(nombreUsuario, puntaje)

        val mensaje = if (entro) {
            getString(R.string.mensaje_entro_ranking, nombreUsuario, puntaje)
        } else {
            getString(R.string.mensaje_no_entro_ranking, nombreUsuario, puntaje)
        }

        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_titulo))
            .setMessage(mensaje)
            .setCancelable(false)

        if (entro) {
            builder.setPositiveButton(getString(R.string.ver_ranking)) { _, _ ->
                startActivity(Intent(this, RankingActivity::class.java))
                finish()
            }
            builder.setNegativeButton(getString(R.string.compartir_puntaje)) { _, _ ->
                compartirPuntaje(nombreUsuario, puntaje)
                finish()
            }
            builder.setNeutralButton(getString(R.string.jugar_nuevamente)) { _, _ ->
                reiniciar()
            }
        } else {
            builder.setPositiveButton(getString(R.string.jugar_nuevamente)) { _, _ ->
                reiniciar()
            }
            builder.setNegativeButton(getString(R.string.volver_inicio)) { _, _ ->
                finish()
            }
        }

        builder.show()

    }

    private fun calcularPuntaje(): Int {
        return if (movimientos == 0) 0 else ((aciertos.toDouble() / movimientos) * 100).toInt()
    }

    private fun agregarYVerificarSiEntro(nombre: String, puntaje: Int): Boolean {
        val archivo = File(filesDir, "ranking.txt")
        val ranking = mutableListOf<Pair<String, Int>>()

        if (archivo.exists()) {
            //archivo.delete() solo para resetear el ranking
            archivo.readLines().forEach {
                val partes = it.split("::")
                if (partes.size == 2) {
                    ranking.add(Pair(partes[0], partes[1].toIntOrNull() ?: 0))
                }
            }
        }

        ranking.add(Pair(nombre, puntaje))
        val top5 = ranking.sortedByDescending { it.second }.take(5)

        val nuevoRankingTexto = top5.joinToString("\n") { "${it.first}::${it.second}" }

        try {
            archivo.writeText(nuevoRankingTexto)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return top5.any { it.first == nombre && it.second == puntaje }
    }


    // destruye el temporizador cuando salimos de la activity
    override fun onDestroy() {
        if (::temporizador.isInitialized) {
            temporizador.cancel()
        }
        super.onDestroy()
    }

    private fun compartirPuntaje(nombre: String, puntaje: Int) {
        val mensaje = getString(R.string.mensaje_compartir, nombre, puntaje)
        val intent = Intent();
        intent.action = Intent.ACTION_SEND;
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, mensaje)
        if(intent.resolveActivity(packageManager) != null){
            startActivity(intent)
        }
    }

}