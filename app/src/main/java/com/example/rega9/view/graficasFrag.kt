package com.example.rega9

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries
import java.util.*
import android.content.ContentValues
import java.text.SimpleDateFormat


import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.rega9.R
import com.example.rega9.view.MyDatabaseHelper
import java.io.IOException
import java.util.*


class graficasFrag : Fragment() {

    //Graficas
    private var ListStrHora: ArrayList<String?>? = null
    private var ListValAwa: ArrayList<Float>? = null
    private val DataLimite = 50
    //-----------------------------------------------

    //Sqlite
    private lateinit var dbHelper: MyDatabaseHelper
    //-----------------------------------------------

    private val recDataString = StringBuilder()

    companion object {
        fun newInstance(): graficasFrag {
            return graficasFrag()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graficas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //----------------------------
        val IdLineChartAwa = view.findViewById<ValueLineChart>(R.id.IdLineChartAwa)
        val IdBtnAwa = view.findViewById<Button>(R.id.IdBtnAwa)

        ListStrHora = java.util.ArrayList<String?>()
        ListValAwa = java.util.ArrayList<Float>()

        try {
            ListStrHora!!.clear()
            ListValAwa!!.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        dbHelper = MyDatabaseHelper(requireContext()) // Inicializar el dbHelper

        // Leer datos de la base de datos y agregarlos a la gráfica
        val db = dbHelper.readableDatabase
        val projection = arrayOf("time", "value")
        val cursor = db.query("data", projection, null, null, null, null, null)
        ListStrHora!!.clear()
        ListValAwa!!.clear()
        while (cursor.moveToNext()) {
            val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
            val value = cursor.getFloat(cursor.getColumnIndexOrThrow("value"))
            ListStrHora!!.add(time)
            ListValAwa!!.add(value)
        }
        cursor.close()
        db.close()

        // Agregar puntos a la gráfica
        val ValSeriesAwa = ValueLineSeries()
        ValSeriesAwa.color = ContextCompat.getColor(requireContext(), R.color.md_blue_900)
        for (i in ListStrHora!!.indices) {
            ValSeriesAwa.addPoint(ValueLinePoint(ListStrHora!![i], ListValAwa!![i]))
        }
        IdLineChartAwa.clearChart()
        IdLineChartAwa.addSeries(ValSeriesAwa)

        //------------------------------------------------------------------------------------------Aqui se reciben los datos que envía ESP32
        var txtRecibe = view.findViewById<TextView>(R.id.txtRecibe)
        var conStringToFloat = txtRecibe.text.toString()
        var registroAgua = conStringToFloat.toFloat()

        bluetoothIn = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == handlerState) {
                    val readMessage = msg.obj as String

                    recDataString.append(readMessage)

                    val endOfLineIndex: Int = recDataString.indexOf("#")
                    if (endOfLineIndex > 0) {

                        // Log.i("recDataString -> -> ", recDataString.toString())
                        val dataInPrint: String = recDataString.substring(0, endOfLineIndex)
                        txtRecibe.setText("$dataInPrint")
                        conStringToFloat = txtRecibe.text.toString()
                        registroAgua = conStringToFloat.toFloat()

                        recDataString.delete(0, recDataString.length)
                    }
                }
            }
        }
        //------------------------------------------------------------------------------------------Aqui se reciben los datos que envía ESP32


        IdBtnAwa.setOnClickListener {
            if (txtRecibe.text.equals("0") or txtRecibe.text.equals("0.0") or txtRecibe.text.equals("0.00")) {
                Toast.makeText(requireContext(), "Registro es 0", Toast.LENGTH_LONG).show()
            } else {
                //----------------------------------------------------------------------------------------------Envio de datos 0 para que se cumpla la condicion--------------------------------------
                var mensaje_2: String = "0"
                sendCommand(mensaje_2)
                //----------------------------------------------------------------------------------------------Envio de datos 0 para que se cumpla la condicion--------------------------------------


                //-------------------------------------------------------------------------------------- Agrega un retraso de 2 segundos (2000 milisegundos) antes de habilitar el botón nuevamente
                IdBtnAwa.isEnabled = false
                Handler().postDelayed({
                    IdBtnAwa.isEnabled = true
                }, 5000)
                //-------------------------------------------------------------------------------------- Agrega un retraso de 2 segundos (2000 milisegundos) antes de habilitar el botón nuevamente


                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                //val ValAwa = (Math.random() * 50).toFloat() + 1

                /*if (registroAgua != null) {
                    // El valor se convirtió correctamente a float
                    // Puedes usar valorFloat para realizar operaciones
                } else {
                    // El texto no se pudo convertir a float, manejar el caso de error si es necesario
                }*/


                //-------------------------------------------------------------------------------------- Insertar registro en la base de datos
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put("time", currentTime)
                    put("value", registroAgua)
                    //put("value", ValAwa) Se sustituyo el valor random generado
                }
                db.insert("data", null, values)
                //-------------------------------------------------------------------------------------- Insertar registro en la base de datos

                // Agregar punto a la gráfica
                ListStrHora!!.add(currentTime)
                ListValAwa!!.add(registroAgua)
                //ListValAwa!!.add(ValAwa)

                val ValSeriesAwa = ValueLineSeries()
                ValSeriesAwa.color = ContextCompat.getColor(requireContext(), R.color.md_blue_900)

                if (ListStrHora!!.size > 0) {
                    for (i in ListStrHora!!.indices) {
                        ValSeriesAwa.addPoint(ValueLinePoint(ListStrHora!![i], ListValAwa!![i]))
                    }
                }

                IdLineChartAwa.clearChart()
                IdLineChartAwa.addSeries(ValSeriesAwa)

                // Limitar el número de puntos en la gráfica
                if (ListStrHora!!.size > DataLimite) {
                    ListStrHora!!.removeAt(0)
                    ListValAwa!!.removeAt(0)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close() // Cerrar la conexión de la base de datos al destruir el fragmento
    }

    private fun sendCommand(input: String) {
        if (blueFrag.m_bluetoothSocket != null) {
            try {
                blueFrag.m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}