package com.craftwithme.productively.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Todo(
        var id: Int? = 0,
        var title: String? = null,
        var date: String? = null,
        var time: String? = null
) : Parcelable