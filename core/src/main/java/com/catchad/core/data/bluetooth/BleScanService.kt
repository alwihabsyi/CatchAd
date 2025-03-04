package com.catchad.core.data.bluetooth

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import com.catchad.core.R
import com.catchad.core.domain.constant.Constants.ACKNOWLEDGED_DEVICES
import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.domain.model.EmptyData
import com.catchad.core.domain.model.WifiDeviceData
import com.catchad.core.domain.repository.DeviceRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.android.ext.android.inject

@Suppress("DEPRECATION")
class BleScanService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val deviceRepository: DeviceRepository by inject()

    private var checkRssiJob: Job? = null
    private var checkRegisterJob: Job? = null

    private lateinit var rxBleClient: RxBleClient
    private lateinit var firestore: FirebaseFirestore
    private var scanDisposable: Disposable? = null
    private val senderMutex = Mutex()
    private var rssiLimit = -60

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        firestore = FirebaseFirestore.getInstance()
        checkRegister()
        checkRssiLimit()
        startForegroundService()
        startBleScan()
        startWifiScan()
    }

    private fun checkRssiLimit() {
        if (checkRssiJob?.isActive == true) return
        checkRssiJob = scope.launch {
            rssiLimit = deviceRepository.getRssiLimit().first { it != null }?.toInt() ?: -60
        }
    }

    private fun checkRegister() {
        if (checkRegisterJob?.isActive == true) return
        checkRegisterJob = scope.launch {
            val unRegistered = deviceRepository.getRegistered().first { !it }
            if (unRegistered) deviceRepository.registerDevice()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startWifiScan() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val scannedWifiMap = mutableMapOf<String, WifiDeviceData>()

        scope.launch {
            while (isActive) {
                wifiManager.startScan()
                val results = wifiManager.scanResults

                results.filter { it.level > rssiLimit }.forEach { scanResult ->
                    val wifiDevice = WifiDeviceData(
                        id = scanResult.SSID,
                        ssid = scanResult.SSID,
                        bssid = scanResult.BSSID,
                        frequency = scanResult.frequency,
                        rssi = scanResult.level
                    )
                    val existingDevice = scannedWifiMap[scanResult.BSSID.trim()]
                    if (existingDevice == null || scanResult.level > existingDevice.rssi) {
                        scannedWifiMap[scanResult.BSSID.trim()] = wifiDevice
                        publishData(wifiDevice)
                    }
                }

                sendDeviceListToMainActivity(scannedWifiMap.values.toList().ifEmpty { listOf(EmptyData("Wifi")) })
                scannedWifiMap.clear()
                delay(WIFI_SCAN_DELAY)
            }
        }
    }

    private fun startForegroundService() {
        val channelId = "BLE_SCAN_CHANNEL"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Catching Ads", NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification =
            NotificationCompat.Builder(this, channelId).setContentTitle("Catch Ad")
                .setContentText("Catching nearby ads").setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build()

        startForeground(notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        val devicesMap = mutableMapOf<String, BluetoothDeviceData>()
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        scanDisposable =
            rxBleClient.scanBleDevices(scanSettings, ScanFilter.empty()).subscribe({ scanResult ->
                if (scanResult.rssi > rssiLimit) {
                    scanResult.bleDevice.name?.let { name ->
                        val currentDevice = BluetoothDeviceData(
                            id = name,
                            name = name,
                            address = scanResult.bleDevice.macAddress,
                            manufacturerData = "Unknown",
                            rssi = scanResult.rssi
                        ).takeIf { name in ACKNOWLEDGED_DEVICES.map { it.first } }

                        currentDevice?.let {
                            val existingDevice = devicesMap[name]
                            if (existingDevice == null || scanResult.rssi > existingDevice.rssi) {
                                devicesMap[name] = currentDevice
                                publishData(currentDevice)
                            }
                        }
                    }
                }
            }, {
                stopSelf()
                it.printStackTrace()
            })

        scope.launch {
            while (isActive) {
                sendDeviceListToMainActivity(devicesMap.values.toList().ifEmpty { listOf(EmptyData("Bluetooth")) })
                devicesMap.clear()
                delay(BLUETOOTH_SCAN_DELAY)
            }
        }
    }

    private suspend fun sendDeviceListToMainActivity(devices: List<Parcelable>) =
        senderMutex.withLock {
            val intent = Intent("com.catchad.core.DEVICES_DETECTED")
            intent.putParcelableArrayListExtra("devices", ArrayList(devices))
            sendBroadcast(intent)
        }

    private fun publishData(data: Parcelable) = scope.launch {
        deviceRepository.publishDevice(data)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        scanDisposable?.dispose()
        super.onDestroy()
    }

    companion object {
        const val WIFI_SCAN_DELAY = 3000L
        const val BLUETOOTH_SCAN_DELAY = 3000L
    }
}