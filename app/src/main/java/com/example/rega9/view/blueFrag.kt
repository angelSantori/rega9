package com.example.rega9

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import org.eazegraph.lib.charts.ValueLineChart
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.result.ActivityResultLauncher

//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rega9.R
import java.util.*

//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

//bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
val handlerState = 0
var bluetoothIn: Handler? = null

//bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
private var lastReceivedValue: String? = null


class blueFrag : Fragment() {

    private val REQUEST_LOCATION_PERMISSION = 100

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
        val btnDispBT = view.findViewById<Button>(R.id.btnDisBT)
        val spinDisp = view.findViewById<Spinner>(R.id.spinDisp)
        val btnConect = view.findViewById<Button>(R.id.bntConnect)
        //--------------------------------------------------

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
            1
        )


        // Inicialización del BluetoothAdapter
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        //Inicializacion del bluetooth adapter
        mBtAdapter =
            (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        //Permisos coneccted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        }

        //Permiso scan
// Verificar si el permiso de ubicación ya ha sido otorgado


        //--------------------------------------------------------

        //---------------------------Zona pruebas-------------------------------------------------
        btnDispBT.setOnClickListener {
            if (mBtAdapter.isEnabled) {
                // Verificar si tienes permisos de ubicación
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
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
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_LOCATION
                    )
                }
            } else {
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(
                    requireContext(),
                    "Primero vincule un dispositivo bluetooth",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        //---------------------------Zona pruebas-------------------------------------------------

        btnConect.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    val IntValSpin = spinDisp.selectedItemPosition
                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(requireContext(), m_address, Toast.LENGTH_LONG).show()
                    // Cancel discovery because it otherwise slows down the connection.
                    mBtAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                    MyConexionBT = ConnectedThread(m_bluetoothSocket!!)
                    MyConexionBT!!.start()
                    m_isConnected = true // se estableció la conexión, se actualiza la variable
                }

                Toast.makeText(requireContext(), "CONEXION EXITOSA", Toast.LENGTH_LONG).show()
                Log.i("data", "CONEXION EXITOSA")

            } catch (e: IOException) {
                //connectSuccess = false
                e.printStackTrace()
                Toast.makeText(requireContext(), "ERROR DE CONEXION", Toast.LENGTH_LONG).show()
                Log.i("data", "ERROR DE CONEXION")
            }
        }

    }

    //eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
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