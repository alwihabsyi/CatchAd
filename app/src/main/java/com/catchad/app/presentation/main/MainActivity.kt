package com.catchad.app.presentation.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.catchad.core.ui.NotificationAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModel()
    private val notificationAdapter by lazy { NotificationAdapter() }
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
        setRecyclerView()
        observeContent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)

        if (!hasPermissions()) {
            requestPermissionsLauncher.launch(permissions)
            return
        }

        setActions()
        setRecyclerView()
        startBleService()
        observeContent()
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