@file:Suppress("DEPRECATION")

package com.catchad.app.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide

fun Activity.toast(msg: String?) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun ImageView.glide(url: String) {
    Glide.with(this).load(url).into(this)
}

fun View.show() { visibility = View.VISIBLE }

fun View.hide() { visibility = View.GONE }

val permissions = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
    else -> {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }
}

fun isImageUrl(url: String): Boolean {
    val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    return imageExtensions.any { url.endsWith(it, ignoreCase = true) }
}

fun isVideoUrl(url: String): Boolean {
    val videoExtensions = listOf("mp4", "webm", "ogg", "3gp", "mkv")
    return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
}

fun Activity.alertDialog(onYesClicked: () -> Unit) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Hapus Notifikasi")
    builder.setMessage("Yakin ingin menghapus semua notifikasi?")

    builder.setPositiveButton("Ya") { _, _ ->
        onYesClicked.invoke()
    }

    builder.setNegativeButton("Tidak") { dialog, _ ->
        dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun Activity.allowScanning(): Boolean {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return bluetoothAdapter?.isEnabled == true && wifiManager.isWifiEnabled
}