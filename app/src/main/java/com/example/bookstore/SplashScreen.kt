package com.example.bookstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val videoPath = "android.resource://$packageName/raw/book"
        videoView.setVideoPath(videoPath)

        videoView.setOnCompletionListener {
            var r = object: Runnable{
                override fun run() {
                    startActivity(Intent(this@SplashScreen, LogInActivity::class.java))
                    finish()
                }
            }
            Handler().postDelayed(r, 500)
        }

        videoView.start()
    }
}