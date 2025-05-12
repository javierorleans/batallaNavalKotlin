package com.example.batallanaval

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // vinculamos variables con elementos del xml
        EstadisticasLayout = findViewById(R.id.Estadisticas)
        gridLayout = findViewById(R.id.gridLayout)
        restartButton = findViewById(R.id.restartButton)
        // inicializa como lista vacia
        buttons = Array(36) { Button(this) } // Inicializa con botones vacíos por ahora. lo que está entre { es una funcion lambda}

        if (savedInstanceState == null) {
            inicializarJuego()
        } else {
            reconstruirBotonera()
        }

        restartButton.setOnClickListener {
            inicializarJuego()
        }
    }


    //para salvar los estados
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("barcosTotales", barcosTotales)
        outState.putInt("movimientos", movimientos)
        outState.putInt("aciertos", aciertos)
        outState.putIntegerArrayList("barcos", ArrayList(shipPositions))
        //guardar botones deshabilitados
        val desactivados = ArrayList<Int>()
        val colores = ArrayList<Int>()
        for (i in buttons.indices) {
            if (!buttons[i].isEnabled) desactivados.add(i)
            val color = (buttons[i].background as? ColorDrawable)?.color ?: Color.TRANSPARENT
            colores.add(color)
        }
        outState.putIntegerArrayList("botonesDesactivados", desactivados)
        outState.putIntegerArrayList("coloresBotones", colores)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        barcosTotales = savedInstanceState.getInt("barcosTotales")
        movimientos = savedInstanceState.getInt("movimientos")
        aciertos = savedInstanceState.getInt("aciertos")

        val barcos = savedInstanceState.getIntegerArrayList("barcos")
        if (barcos != null) shipPositions = barcos.toSet()

        val desactivados = savedInstanceState.getIntegerArrayList("botonesDesactivados") ?: arrayListOf()
        val colores = savedInstanceState.getIntegerArrayList("coloresBotones") ?: arrayListOf()

        // Reconstrucción de la grilla con los valores guardados
        gridLayout.post {
            for (i in buttons.indices) {
                val button = buttons[i]
                val color = colores.getOrElse(i) { Color.TRANSPARENT }
                button.setBackgroundColor(color)
                button.isEnabled = i !in desactivados
            }
            actualizarEstadisticas()
        }
    }

    //convierte 1cm en pixeles
    private fun cmToPx(cm: Float): Int {
        val metrics = resources.displayMetrics
        return (cm * metrics.xdpi / 2.54f).toInt()
    }


    private fun inicializarVariablesDelJuego() {
        shipPositions = generarBarcosAleatorios() //crea nuevo conjunto de barcos aleatorios con sus posiciones
        barcosTotales = shipPositions.size // Tamaño del arreglo creado anteriormente
        movimientos = 0
        aciertos = 0
        actualizarEstadisticas()
        // Aunque la primera vez estan en cero, si reinicio el juego sin llamar a esta fun, no se ven en cero hasta que se pulsa una celda
    }


    private fun crearGrillaConBotones() {
        gridLayout.removeAllViews() //borra elementos en el tablero

        //bucle para crear los 36 botones iniciales
        for (i in buttons.indices) {
            val button = Button(this) //creo boton
            button.text = ""
            button.setBackgroundColor(Color.parseColor("#607D8B"))

            val sizePx = cmToPx(1f) // le doy tamaño

            //diseño del boton
            val params = GridLayout.LayoutParams()
            params.width = sizePx
            params.height = sizePx
            params.setMargins(2, 2, 2, 2)
            //asignamos fila y columnas en función de i
            val row = i / 6  //en que fila va
            val col = i % 6  //en que columna va
            params.rowSpec = GridLayout.spec(row)  //establecer la fila
            params.columnSpec = GridLayout.spec(col)  //establecer la columna
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
                    EstadisticasLayout.text = "¡Ganaste! Movimientos totales: $movimientos"
                }
                // Si se encontraron todos los barcos, se anuncia
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
            posiciones.add((0 until 36).random()) //until indica que no incluye el 36, si debo incluirlos debe ir (0..36)
        } //genero posiciones
        return posiciones //devuelve posiciones generadas
    }

    private fun actualizarEstadisticas(){
        EstadisticasLayout.text = "Movimientos: $movimientos | Aciertos: $aciertos | Restantes: ${barcosTotales - aciertos}"
    }
}