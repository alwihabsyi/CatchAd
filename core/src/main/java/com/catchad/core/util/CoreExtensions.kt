package com.catchad.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toDateString(): String = SimpleDateFormat("dd MMMM yyyy | HH:mm:ss", Locale.getDefault()).format(this)