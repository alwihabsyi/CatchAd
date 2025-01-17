package com.catchad.core.data.bluetooth

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.catchad.core.R
import com.catchad.core.data.local.AppDatabase
import com.catchad.core.data.local.entity.ContentEntity
import com.catchad.core.data.mapper.ContentMapper
import com.catchad.core.domain.constant.Collections
import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.domain.model.Content
import com.google.firebase.firestore.FirebaseFirestore
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BleScanService : Service() {
    private lateinit var rxBleClient: RxBleClient
    private var scanDisposable: Disposable? = null
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        firestore = FirebaseFirestore.getInstance()
        startForegroundService()
        startBleScan()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "BLE_SCAN_CHANNEL"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Catching Ads",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Catch Ad")
            .setContentText("Catching nearby ads")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        val devicesMap = mutableMapOf<String, BluetoothDeviceData>()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanDisposable = rxBleClient.scanBleDevices(scanSettings, ScanFilter.empty())
            .subscribe({ scanResult ->
                if (scanResult.rssi > -70) {
                    scanResult.bleDevice.name?.let { name ->
                        val currentDevice = BluetoothDeviceData(
                            id = name,
                            name = name,
                            address = scanResult.bleDevice.macAddress,
                            manufacturerData = " ",
                            rssi = scanResult.rssi
                        )
                        val existingDevice = devicesMap[name]
                        if (existingDevice == null || scanResult.rssi > existingDevice.rssi) {
                            devicesMap[name] = currentDevice
                        }
                    }
                }
            }, {
                stopSelf()
                it.printStackTrace()
            })

        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            while (isActive) {
                delay(1000)
                val strongestDevice = devicesMap.values.maxByOrNull { it.rssi }
                strongestDevice?.let { checkFirestoreForDevice(it) }
                devicesMap.clear()
            }
        }
    }

    private fun checkFirestoreForDevice(device: BluetoothDeviceData) {
        device.id?.let {
            firestore.collection(Collections.CONTENTS)
                .document(device.id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.toObject(Content::class.java)?.let {
                            saveToDatabase(ContentMapper().mapDomainToEntity(it))
                        }
                    }
                }
        }
    }

    private fun sendNotification(content: ContentEntity) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent().apply {
            setClassName(
                this@BleScanService,
                "com.blecatch.app.presentation.webview.WebViewActivity"
            )
            putExtra("contentUrl", content.contentUrl)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "New Content Channel",
                "BLE Scanning",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "BLE_SCAN_CHANNEL")
            .setContentTitle("New Content Added")
            .setContentText(content.description)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(content.id.hashCode(), notification)
    }

    private fun saveToDatabase(items: ContentEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val dao = appDatabase.contentDao()
            val currentContentEntities = dao.getAllContents().first()

            val existingItem = currentContentEntities.find { it.id == items.id }

            when {
                existingItem != null && existingItem.contentUrl != items.contentUrl -> {
                    dao.updateContent(oldId = items.id)
                    dao.insert(items)
                    sendNotification(items)
                }

                existingItem == null -> {
                    dao.insert(items)
                    sendNotification(items)
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        scanDisposable?.dispose()
        super.onDestroy()
    }
}