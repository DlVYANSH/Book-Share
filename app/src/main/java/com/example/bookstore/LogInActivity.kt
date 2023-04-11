package com.example.bookstore

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

open class LogInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient

    companion object {
        private const val RC_SIGN_IN = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)

        btn_logIn.setOnClickListener {
            signInWithEmailPass()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        tv_forgotPass.setOnClickListener {
            forgotPassword()
        }

        linear_layout.setOnClickListener {
            signInWithGoogle()
        }

        tv_register.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun forgotPassword() {
        val intent = Intent(this, ResetPassword::class.java)
        startActivity(intent)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            progressBar_LI.visibility = View.VISIBLE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try{
            if(task.isSuccessful) {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "FirebaseAuthWithGoogle " + account.id)
                firebaseAuthWithGoogle(account.idToken)
            } else {
                progressBar_LI.visibility = View.GONE
            }
        } catch (e: ApiException){
            Log.w(TAG, "SignInResult: Failed Code = " + e.statusCode )
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        GlobalScope.launch(Dispatchers.IO){
            val auth = auth.signInWithCredential(credential).await()
            //update UI in main thread because we can not update it in io thread
            withContext(Dispatchers.Main){
                updateUI()
            }
        }
    }

    private fun signInWithEmailPass() {
        progressBar_LI.visibility = View.VISIBLE

        val email = et_email_LA.editText?.text.toString()
        val pass = et_password_LA.editText?.text.toString()

        if(email.isNotEmpty() && pass.isNotEmpty()) {
            if(pass.length < 8){
                et_password_LA.error = "Password must contains at least 8 characters!"
                et_password_LA.requestFocus()
                progressBar_LI.visibility = View.GONE
            } else {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        updateUI()
                    } else {
                        Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_LONG)
                            .show()
                        progressBar_LI.visibility = View.GONE
                    }
                }
            }
        } else {
            Toast.makeText(this, "Empty fields!!", Toast.LENGTH_SHORT).show()
            progressBar_LI.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateUI() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}