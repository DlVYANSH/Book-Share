package com.example.bookstore

import android.graphics.Bitmap
import android.net.Uri
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit


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

    //Not working properly
    fun convertTimeToHourAndMinutes(timeInMilliSec: Long): String{
        return java.lang.String.format(
            Locale.ENGLISH,
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(timeInMilliSec) % 24,
            TimeUnit.MILLISECONDS.toMinutes(timeInMilliSec) % 60,
            TimeUnit.MILLISECONDS.toSeconds(timeInMilliSec) % 60
        )
    }

}