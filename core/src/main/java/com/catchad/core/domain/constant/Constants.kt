package com.catchad.core.domain.constant

import android.graphics.Color

object Constants {
    const val APP_DATABASE = "bleexplore"
    val ACKNOWLEDGED_DEVICES = listOf(
        "Kitchen" to Color.parseColor("#4CAF50"),  // Bright Green
        "Notebook" to Color.parseColor("#89CFF0"), // Bright Blue
        "Audio" to Color.parseColor("#F44336"),     // Bright Red
        "Phone" to Color.parseColor("#89CFF0")     // Blue
    )
}