package com.catchad.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmptyData(
    val type: String
) : Parcelable