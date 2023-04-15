package com.example.bookstore

import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.*


class Util {
    fun convertBitmapToUri(photo: Bitmap): Uri? {

        val tempFile = File.createTempFile("Img" + System.currentTimeMillis().toString(), ".png")

        val bytes = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()

        return Uri.fromFile(tempFile)
    }

}