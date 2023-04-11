package com.example.bookstore

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        btn_next.setOnClickListener {
            sentRecoveryMail()
        }

    }

    private fun sentRecoveryMail() {
        val recoveryMail = et_recovery_mail.text.toString()

        if(recoveryMail.isNotEmpty()){
            if(recoveryMail.contains("@")){
                Firebase.auth.sendPasswordResetEmail(recoveryMail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Mail sent to the given mail id", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Email sent.")
                        } else {
                            Toast.makeText(this, task.exception?.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Invalid mail id!!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Empty fields not allowed!!", Toast.LENGTH_SHORT).show()
        }
    }
}