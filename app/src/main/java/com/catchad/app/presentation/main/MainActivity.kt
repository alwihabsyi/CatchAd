package com.catchad.app.presentation.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchad.app.databinding.ActivityMainBinding
import com.catchad.app.presentation.webview.WebViewActivity
import com.catchad.app.util.hide
import com.catchad.app.util.permissions
import com.catchad.app.util.show
import com.catchad.core.data.bluetooth.BleScanService
import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.ui.DeviceListAdapter
import com.catchad.core.ui.NotificationAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModel()
    private val notificationAdapter by lazy { NotificationAdapter() }
    private val deviceListAdapter by lazy { DeviceListAdapter() }
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
//        setRecyclerView()
//        observeContent()
    }

    private var oldList = mutableListOf<BluetoothDeviceData>()
    private val deviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val devices = intent?.getParcelableArrayListExtra<BluetoothDeviceData>("devices")
            devices?.takeIf { it != oldList }?.let {
                deviceListAdapter.submitList(it)
                oldList = it
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)

        registerBleReceiver()
        setDeviceRv()

        if (!hasPermissions()) {
            requestPermissionsLauncher.launch(permissions)
            return
        } else startBleService()

//        setActions()
//        setRecyclerView()
//        observeContent()

    }

    private fun setDeviceRv() {
        binding.rvDevice.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceListAdapter
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerBleReceiver() {
        val filter = IntentFilter("com.catchad.core.BLUETOOTH_DEVICES_DETECTED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) registerReceiver(
            deviceReceiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )
        else registerReceiver(deviceReceiver, filter)
    }

    private fun setActions() {
        binding.apply {
            btnDelete.setOnClickListener {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Delete All")
                builder.setMessage("Are you sure you want to delete all notifications?")

                builder.setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteNotifications()
                    notificationAdapter.differ.submitList(emptyList())
                    dialog.dismiss()
                }

                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setRecyclerView() {
        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = notificationAdapter
        }
        notificationAdapter.onItemClick = {
            startActivity(
                Intent(this@MainActivity, WebViewActivity::class.java).apply {
                    putExtra("contentUrl", it.contentUrl)
                }
            )
        }
    }

    private fun startBleService() {
        val intent = Intent(this, BleScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun observeContent() {
        lifecycleScope.launch {
            viewModel.getAllContent().collectLatest { contents ->
                if (contents.isNotEmpty()) {
                    binding.tvNoContent.hide()
                    notificationAdapter.differ.submitList(contents)
                } else binding.tvNoContent.show()
            }
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