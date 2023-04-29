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
const val REQUEST_ENABLE_BT = 1

//bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
private var lastReceivedValue: String? = null


class blueFrag : Fragment() {

    //cccccccccccccccccccccccccccccccccccccccccccccccccc
    //BluetoothAdapter
    private val PERMISSION_REQUEST_LOCATION = 1001

    lateinit var mBtAdapter: BluetoothAdapter
    private var MyConexionBT: ConnectedThread? = null
    private val recDataString = StringBuilder()
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? = null

    //private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val MY_UUID: UUID = m_myUUID
    private val REQUEST_BLUETOOTH_PERMISSION = 1
    private val REQUEST_BLUETOOTH_SCAN_PERMISSION = 3


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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Si no se tiene el permiso concedido, se solicita.
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_BLUETOOTH_SCAN_PERMISSION)
        } else {
            // Si ya se tiene el permiso concedido, se realiza la operación que requiere el permiso.
            startDiscovery()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
            }
        }

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

    /*private class ConnectedThread(socket: BluetoothSocket) : Thread() {
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
    }*/
    //eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN
    )


    private val REQUEST_CODE_PERMISSIONS = 1

    private fun requestPermissionsIfNecessary() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startDiscovery() {
        mBtAdapter.startDiscovery()
        Toast.makeText(requireContext(), "Scanning for devices...", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_BLUETOOTH_SCAN_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Si se concedió el permiso, se realiza la operación que requiere el permiso.
                    startDiscovery()
                } else {
                    // Si no se concedió el permiso, se muestra un mensaje al usuario.
                    Toast.makeText(requireContext(), "Bluetooth scan permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private var running: Boolean = false
        private val MESSAGE_READ = 1

        val mHandler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                // handle different message types here
                else -> false
            }
        }


        override fun run() {
            val buffer = ByteArray(1024) // buffer store for the stream

            // Keep listening to the InputStream until an exception occurs
            while (running) {
                try {
                    // Read from the InputStream
                    val bytes = mmInStream.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)

                    // Send the obtained bytes to the UI activity
                    // so that it can be displayed in the TextView
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, incomingMessage)
                        .sendToTarget()
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                running = false
                mmSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        init {
            running = true
        }
    }
}