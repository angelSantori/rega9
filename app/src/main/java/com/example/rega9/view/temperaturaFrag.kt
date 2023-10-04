package com.example.rega9.view

import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
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
import com.example.rega9.bluetoothIn
import com.example.rega9.handlerState
import org.eazegraph.lib.charts.ValueLineChart
import java.io.IOException
import java.util.*

class temperaturaFrag : Fragment() {
    private var startPoint = 0
    private var endpoint = 0
    private var mediaPlayer: MediaPlayer? = null
    private var timer: CountDownTimer? = null

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
        var btnDuchaMilitar = view.findViewById<Button>(R.id.btnDuchaMilitar)
        mediaPlayer =
            MediaPlayer.create(requireContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))



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

                // Agrega un retraso de 2 segundos (2000 milisegundos) antes de habilitar el botón nuevamente
                btnDefTempe.isEnabled = false
                Handler().postDelayed({
                    btnDefTempe.isEnabled = true
                }, 2000)
            }
        }

        btnDuchaMilitar.setOnClickListener {
            startTimer()
        }
    }

    private fun startTimer() {
        // Verifica si mediaPlayer se ha inicializado antes de usarlo
        if (mediaPlayer != null) {
            // Inicia un temporizador de 1.5 minutos (90,000 ms)
            timer = object : CountDownTimer(90000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // No se muestra el tiempo restante
                }

                override fun onFinish() {
                    // Cuando termina el temporizador, reproduce el sonido durante 10 segundos
                    mediaPlayer?.start()
                    // Establece otro temporizador de 1.5 minutos para volver a sonar
                    startSecondTimer()
                }
            }

            (timer as CountDownTimer).start()
        } else {
            // mediaPlayer no se ha inicializado, maneja el error adecuadamente
        }
    }

    private fun startSecondTimer() {
        // Verifica si mediaPlayer se ha inicializado antes de usarlo
        if (mediaPlayer != null) {
            // Inicia un segundo temporizador de 1.5 minutos (90,000 ms)
            timer = object : CountDownTimer(90000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // No se muestra el tiempo restante
                }

                override fun onFinish() {
                    // Cuando termina el segundo temporizador, reproduce el sonido nuevamente
                    mediaPlayer?.start()
                    // Puedes repetir este proceso si deseas que el sonido siga sonando
                }
            }

            (timer as CountDownTimer).start()
        } else {
            // mediaPlayer no se ha inicializado, maneja el error adecuadamente
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegúrate de detener el temporizador y liberar los recursos cuando el fragmento se destruya
        timer?.cancel()
        mediaPlayer?.release()
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