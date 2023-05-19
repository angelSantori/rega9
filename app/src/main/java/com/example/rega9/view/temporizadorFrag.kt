package com.example.rega9.view

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.rega9.R


class TemporizadorFrag : Fragment() {

    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var timerTextView: TextView
    private lateinit var timerEditText: EditText
    private lateinit var countDownTimer: CountDownTimer
    private var timeInMillis: Long = 0
    private var isTimerRunning = false
    private lateinit var mediaPlayer: MediaPlayer
    private var timeRemaining: Long = 0

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
        return inflater.inflate(R.layout.fragment_temporizador, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        startButton = view.findViewById(R.id.startButton)
        pauseButton = view.findViewById(R.id.pauseButton)
        stopButton = view.findViewById(R.id.stopButton)
        timerEditText = view.findViewById(R.id.timerEditText)
        timerTextView = view.findViewById(R.id.timerTextView)
        //
        // spinner = view.findViewById(R.id.timerSpinner)
        mediaPlayer =
            MediaPlayer.create(requireContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        // Configurar el adaptador del Spinner
        val times = arrayOf("10 segundos", "5 minutos", "10 minutos", "20 minutos", "30 minutos")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, times)


        // Asignar clics de botón
        startButton.setOnClickListener { startTimer() }
        pauseButton.setOnClickListener { pauseTimer() }
        stopButton.setOnClickListener { stopTimer() }
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            val timeText = timerEditText.text.toString()
            if (timeText.isNotEmpty()) {
                val timeInMinutes = timeText.toInt() * 60 * 1000

                // Si hay tiempo restante, se inicia el temporizador con ese tiempo
                if (timeRemaining > 0) {
                    countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            timeInMillis = millisUntilFinished
                            updateTimerText()
                        }

                        override fun onFinish() {
                            playAlarm()
                            stopTimer()
                        }
                    }
                } else {
                    countDownTimer = object : CountDownTimer(timeInMinutes.toLong(), 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            timeInMillis = millisUntilFinished
                            updateTimerText()
                        }

                        override fun onFinish() {
                            playAlarm()
                            stopTimer()
                        }
                    }
                }

                countDownTimer.start()
                isTimerRunning = true
                updateButtons()
            }
        }
    }

    private fun pauseTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel()
            isTimerRunning = false
            timeRemaining = timeInMillis // Almacena el tiempo restante
            updateButtons()
            startButton.text = "Reanudar"
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel()
            isTimerRunning = false
            timeInMillis = 0
            updateTimerText()
            updateButtons()
        }
    }

    private fun updateTimerText() {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        timerTextView.text = "%02d:%02d".format(minutes, seconds)
    }

    private fun updateButtons() {
        startButton.isEnabled = !isTimerRunning
        pauseButton.isEnabled = isTimerRunning
        stopButton.isEnabled = isTimerRunning
    }

    private fun playAlarm() {
        mediaPlayer = MediaPlayer.create(requireContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        mediaPlayer.isLooping = true // Corregir la configuración de isLooping
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTimerRunning) {
            countDownTimer.cancel()
        }
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}