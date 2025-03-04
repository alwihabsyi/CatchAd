package com.catchad.app.presentation.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchad.app.databinding.ActivityMainBinding
import com.catchad.app.util.permissions
import com.catchad.core.data.bluetooth.BleScanService
import com.catchad.core.domain.helpers.ConnectivityHelper
import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.domain.model.EmptyData
import com.catchad.core.domain.model.WifiDeviceData
import com.catchad.core.ui.DeviceListAdapter
import com.catchad.core.ui.WifiDeviceListAdapter
import org.koin.android.ext.android.inject

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val deviceListAdapter by lazy { DeviceListAdapter() }
    private val wifiDeviceListAdapter by lazy { WifiDeviceListAdapter() }
    private val connectivityHelper: ConnectivityHelper by inject()
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val isGranted = it.value
            if (!isGranted && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(
                    this, "Harap berikan izin bluetooth untuk melanjutkan", Toast.LENGTH_SHORT
                ).show()
                return@registerForActivityResult
            }
        }

        startBleService()
    }

    private val deviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getParcelableArrayListExtra<Parcelable>("devices")?.let { devices ->
                when (val d = devices.firstOrNull()) {
                    is BluetoothDeviceData -> {
                        deviceListAdapter.differ.submitList(
                            devices.filterIsInstance<BluetoothDeviceData>()
                                .sortedByDescending { it.rssi })
                    }

                    is WifiDeviceData -> {
                        wifiDeviceListAdapter.differ.submitList(
                            devices.filterIsInstance<WifiDeviceData>()
                                .sortedByDescending { it.rssi })
                    }

                    is EmptyData -> {
                        if (d.type == "Wifi") wifiDeviceListAdapter.differ.submitList(emptyList())
                        else deviceListAdapter.differ.submitList(emptyList())
                    }

                    else -> Log.e("DeviceReceiver", "Received unknown device type")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)

        setDeviceRv()
        registerReceiver()

        if (!hasPermissions()) {
            requestPermissionsLauncher.launch(permissions)
            return
        } else startBleService()

//        connectivityHelper.connectToSSID("Hpmerah", "12345678")
    }

    private fun setDeviceRv() {
        binding.rvWifiDevices.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = wifiDeviceListAdapter
        }

        binding.rvDevice.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceListAdapter
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerReceiver() {
        val filter = IntentFilter("com.catchad.core.DEVICES_DETECTED")
        if (Build.VERSION.SDK_INT >= 34) registerReceiver(
            deviceReceiver,
            filter,
            Context.RECEIVER_EXPORTED
        )
        else registerReceiver(deviceReceiver, filter)
    }

    private fun startBleService() {
        val intent = Intent(this, BleScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun hasPermissions(): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}