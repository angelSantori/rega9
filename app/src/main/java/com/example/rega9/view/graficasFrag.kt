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
import com.example.rega9.R
import com.example.rega9.view.MyDatabaseHelper
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
        ValSeriesAwa.color = ContextCompat.getColor(requireContext(), R.color.md_blue_400)
        for (i in ListStrHora!!.indices) {
            ValSeriesAwa.addPoint(ValueLinePoint(ListStrHora!![i], ListValAwa!![i]))
        }
        IdLineChartAwa.clearChart()
        IdLineChartAwa.addSeries(ValSeriesAwa)

        IdBtnAwa.setOnClickListener {

            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val ValAwa = (Math.random() * 50).toFloat() + 1

            // Insertar registro en la base de datos
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("time", currentTime)
                put("value", ValAwa)
            }
            db.insert("data", null, values)

            // Agregar punto a la gráfica
            ListStrHora!!.add(currentTime)
            ListValAwa!!.add(ValAwa)

            val ValSeriesAwa = ValueLineSeries()
            ValSeriesAwa.color = ContextCompat.getColor(requireContext(), R.color.md_blue_400)

            if (ListStrHora!!.size > 0) { for (i in ListStrHora!!.indices) {
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
        //----------------------------
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close() // Cerrar la conexión de la base de datos al destruir el fragmento
    }
}