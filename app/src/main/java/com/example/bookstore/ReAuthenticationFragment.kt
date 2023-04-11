package com.example.bookstore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_log_in.view.*

class ReAuthenticationFragment : Fragment() {
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var tvForgotPass: TextView
    private lateinit var btnLogIn: Button
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_re_authentication, container, false)
        etEmail = view.findViewById(R.id.et_email)
        etPass = view.findViewById(R.id.et_password)
        tvForgotPass = view.findViewById(R.id.tv_forgotPass)
        btnLogIn = view.findViewById(R.id.btn_reAuthenticate)

        currentUser = FirebaseAuth.getInstance().currentUser!!

        btnLogIn.setOnClickListener {
            reAuthenticate()
        }

        tvForgotPass.setOnClickListener {

        }

        return view
    }

    private fun reAuthenticate() {
        val email = etEmail.text.toString()
        val password = etPass.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            val credentials = EmailAuthProvider.getCredential(email, password)

            currentUser.reauthenticate(credentials).addOnCompleteListener {
                if(it.isSuccessful){

                }
            }
        }
    }
}