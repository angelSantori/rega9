package com.example.rega9.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rega9.R
import com.example.rega9.bluetoothIn
import com.example.rega9.handlerState
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class TemporizadorFrag : Fragment() {

    //cccccccccccccccccccccccccccccccccccccccccccccccccc
    //BluetoothAdapter
    private val PERMISSION_REQUEST_LOCATION = 1001

    lateinit var mBtAdapter: BluetoothAdapter
    private var MyConexionBT: ConnectedThread? = null
    private val recDataString = StringBuilder()
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? = null

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }
    //cccccccccccccccccccccccccccccccccccccccccccccccccc

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
        return inflater.inflate(R.layout.fragment_blue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
        mAddressDevices = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)



        //-------------------Variables---------------------
        //val btnEnviar = view.findViewById<Button>(R.id.btnEnviar)
        //val edtNumero = view.findViewById<EditText>(R.id.edtNumero)
        val btnDispBT = view.findViewById<Button>(R.id.btnDisBT)

        val spinDisp = view.findViewById<Spinner>(R.id.spinDisp)
        val btnConect = view.findViewById<Button>(R.id.bntConnect)
        //val txtRecibe = view.findViewById<TextView>(R.id.txtRecibe)
        //--------------------------------------------------

        /*bluetoothIn = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == handlerState) {
                    val readMessage = msg.obj as String

                    recDataString.append(readMessage)

                    val endOfLineIndex: Int = recDataString.indexOf("#")
                    if (endOfLineIndex > 0) {

                        // Log.i("recDataString -> -> ", recDataString.toString())
                        val dataInPrint: String = recDataString.substring(0, endOfLineIndex)
                        txtRecibe.setText("$dataInPrint")

                        recDataString.delete(0, recDataString.length)
                    }
                }
            }
        }*/

        //--------------------------------------------------
        //Inicializacion del bluetooth adapter
        mBtAdapter = (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        //Checar si esta encendido o apagado
        if (mBtAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth no está disponible en este dipositivo", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Bluetooth está disponible en este dispositivo", Toast.LENGTH_LONG).show()
        }
        //--------------------------------------------------

        //Boton dispositivos emparejados
        /*btnDispBT.setOnClickListener {

            if (mBtAdapter.isEnabled) {

                val pairedDevices: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
                mAddressDevices!!.clear()
                mNameDevices!!.clear()

                pairedDevices?.forEach { device ->

                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mAddressDevices!!.add(deviceHardwareAddress)
                    //........... EN ESTE PUNTO GUARDO LOS NOMBRE A MOSTRARSE EN EL COMBO BOX
                    mNameDevices!!.add(deviceName)
                    //mNameDevices!!.add(" \n Dispositivo: ${deviceName} , ${deviceHardwareAddress} \n---------------\n")
                }

                //ACTUALIZO LOS DISPOSITIVOS
                spinDisp.setAdapter(mNameDevices)
                //Toast.makeText(this, mNameDevices.toString(), Toast.LENGTH_LONG).show()

            } else {
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(requireContext(), "Primero vincule un dispositivo bluetooth", Toast.LENGTH_LONG).show()
            }
        }*/

        //---------------------------Zona pruebas-------------------------------------------------
        btnDispBT.setOnClickListener {
            if (mBtAdapter.isEnabled) {
                // Verificar si tienes permisos de ubicación
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    // Tienes permisos, obtener dispositivos emparejados
                    val pairedDevices: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
                    mAddressDevices!!.clear()
                    mNameDevices!!.clear()

                    pairedDevices?.forEach { device ->
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address
                        mAddressDevices!!.add(deviceHardwareAddress)
                        mNameDevices!!.add(deviceName)
                    }

                    spinDisp.setAdapter(mNameDevices)
                } else {
                    // No tienes permisos, solicitarlos
                    ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_LOCATION)
                }
            } else {
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(requireContext(), "Primero vincule un dispositivo bluetooth", Toast.LENGTH_LONG).show()
            }
        }









        //---------------------------Zona pruebas-------------------------------------------------
        //-----Boton conextar
        btnConect.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {

                    val IntValSpin = spinDisp.selectedItemPosition

                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(requireContext(),m_address,Toast.LENGTH_LONG).show()
                    // Cancel discovery because it otherwise slows down the connection.
                    mBtAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                    MyConexionBT = ConnectedThread(m_bluetoothSocket!!)
                    MyConexionBT!!.start()

                }

                Toast.makeText(requireContext(),"CONEXION EXITOSA",Toast.LENGTH_LONG).show()
                Log.i("data", "CONEXION EXITOSA")

            } catch (e: IOException) {
                //connectSuccess = false
                e.printStackTrace()
                Toast.makeText(requireContext(),"ERROR DE CONEXION",Toast.LENGTH_LONG).show()
                Log.i("data", "ERROR DE CONEXION")
            }
        }

        /*btnEnviar.setOnClickListener{
            if(edtNumero.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "El control no puede estar vacío", Toast.LENGTH_SHORT)
            }else{
                var mensaje_2: String = edtNumero.text.toString()
                //MyConexionBT?.write(mensaje_2) // otra manera de enviar datos
                sendCommand(mensaje_2) //pero con esta se envia de una manera mas rapida
                Log.d(ContentValues.TAG, "<<<<<<<< --------------- ENVIANDO UN MENSAJE ------------------->>>>>>>>>")
            }
        }*/
        //--------------------------------------------------
        //ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
    }

    //eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        override fun run() {
            val buffer = ByteArray(256)
            var bytes: Int

            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)
                    val readMessage = String(buffer, 0, bytes)
                    Log.i("readMessage -> -> ", readMessage.toString())
                    bluetoothIn?.obtainMessage(handlerState, bytes, -1, readMessage)?.sendToTarget()
                } catch (e: IOException) {
                    break
                }
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }
    //eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
}