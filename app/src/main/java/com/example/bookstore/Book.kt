package com.example.bookstore

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Book(
    val userId: String? = null,
    val createdAt: String = "",
    val bookImage: String = "",
    val bookName: String = "",
    val authorName: String = "",
    val price: String = ""
) : Parcelable{
}