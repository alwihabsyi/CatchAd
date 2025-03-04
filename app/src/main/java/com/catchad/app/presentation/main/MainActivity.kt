package com.catchad.app.presentation.main

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchad.app.databinding.ActivityMainBinding
import com.catchad.app.util.allowScanning
import com.catchad.app.util.permissions
import com.catchad.app.util.toast
import com.catchad.core.data.bluetooth.BleScanService
import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.domain.model.EmptyData
import com.catchad.core.domain.model.WifiDeviceData
import com.catchad.core.ui.DeviceListAdapter
import com.catchad.core.ui.WifiDeviceListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val deviceListAdapter by lazy { DeviceListAdapter() }
    private val wifiDeviceListAdapter by lazy { WifiDeviceListAdapter() }
    private val vm: MainViewModel by viewModel()
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

        startBle()
    }

    private var isReceiverRegistered = false

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED, WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    if (allowScanning()) {
                        startBleService()
                    } else {
                        toast("Bluetooth or wifi is off, make sure it's on to proceed scanning")
                    }
                }
            }
        }
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
        } else startBle()

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

    private fun startBle() = vm.getRssiLimit().observe(this) { limit ->
        if (limit == null) {
            showAskRssiLimitDialog()
            return@observe
        }

        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }

        if (!isReceiverRegistered) {
            registerReceiver(stateReceiver, intentFilter)
            isReceiverRegistered = true
        }

        if (allowScanning()) startBleService()
    }

    private fun startBleService() {
        val intent = Intent(this, BleScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun showAskRssiLimitDialog() {
        val inputField = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            hint = "Enter RSSI Limit"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Set RSSI Limit")
            .setView(inputField)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val input = inputField.text.toString()
                if (input.isNotBlank() && input.isNotEmpty()) {
                    vm.setRssiLimit("-$input")
                } else {
                    toast("Please fill the minimum signal strength (rssi)")
                    showAskRssiLimitDialog()
                }
            }
            .show()
    }

    private fun hasPermissions(): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) unregisterReceiver(stateReceiver)
        _binding = null
    }
}