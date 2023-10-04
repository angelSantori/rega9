package com.example.rega9

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class HomeFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtMensaje = view.findViewById<TextView>(R.id.txtMensaje)

        val mensaje = arrayOf(
            "Cuando proteges el agua proteges la vida.",
            "Tomar agua nos da vida, pero tomar conciencia no dará agua.",
            "Toda el agua que desperdicies hoy, es la que necesitaras mañana.",
            "Ahorra hoy el agua de mañana.",
            "Salva nuestro planeta. Cuída el agua."
        )

        val mensajeAleatroio = mensaje.random()

        txtMensaje.text = mensajeAleatroio

    }
}