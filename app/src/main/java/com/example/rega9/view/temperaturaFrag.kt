package com.example.rega9.view

import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.rega9.R
import com.example.rega9.blueFrag
import org.eazegraph.lib.charts.ValueLineChart
import java.io.IOException
import java.util.*

class temperaturaFrag : Fragment() {
    private var startPoint = 0
    private var endpoint = 0

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address: String
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
        return inflater.inflate(R.layout.fragment_temperatura, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var btnDefTempe = view.findViewById<Button>(R.id.btnDefTempe)
        var txtTemp = view.findViewById<TextView>(R.id.txtTemp)
        var barraTemp = view.findViewById<SeekBar>(R.id.seekBarTemp)

        barraTemp.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtTemp.text = progress.toString();
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (barraTemp != null){
                    startPoint = barraTemp.progress
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (barraTemp != null){
                    endpoint = barraTemp.progress
                }
            }
        })

        btnDefTempe.setOnClickListener{
            if(txtTemp.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "El control no puede estar vacío", Toast.LENGTH_SHORT)
            }else{
                var mensaje_2: String = txtTemp.text.toString()
                //MyConexionBT?.write(mensaje_2) // otra manera de enviar datos
                sendCommand(mensaje_2) //pero con esta se envia de una manera mas rapida
                Log.d(ContentValues.TAG, "<<<<<<<< --------------- ENVIANDO UN MENSAJE ------------------->>>>>>>>>")
            }
        }
    }

    private fun sendCommand(input: String) {
        if (blueFrag.m_bluetoothSocket != null) {
            try{
                blueFrag.m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }


}