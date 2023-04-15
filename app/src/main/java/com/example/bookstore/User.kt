package com.example.bookstore

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(
    val profilePic: String? = "",
    val userId: String? = "",
    val name: String? = "",
    val email: String? = ""
) : Parcelable