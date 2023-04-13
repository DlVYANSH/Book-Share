package com.example.bookstore

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ReAuthenticationFragment(private val operation: String) : Fragment() {
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var tvForgotPass: TextView
    private lateinit var btnLogIn: Button
    private lateinit var etNewPass: EditText
    private lateinit var etReNewPass: EditText
    private lateinit var etNewEmail: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var currentUser: FirebaseUser

    companion object{
        const val REAUTHENTICATION_FRAGMENT_TAG = "ReAuthentication Tag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_re_authentication, container, false)
        etEmail = view.findViewById(R.id.et_email_RA)
        etPass = view.findViewById(R.id.et_password_RA)
        tvForgotPass = view.findViewById(R.id.tv_forgotPass_RA)
        btnLogIn = view.findViewById(R.id.btn_reAuthenticate)
        etNewEmail = view.findViewById(R.id.et_newEmail_RA)
        etNewPass = view.findViewById(R.id.et_newPass_RA)
        etReNewPass = view.findViewById(R.id.et_re_new_pass_RA)
        progressBar = view.findViewById(R.id.progressBarRA)


        currentUser = FirebaseAuth.getInstance().currentUser!!

        btnLogIn.setOnClickListener {
            reAuthenticate()
        }

        tvForgotPass.setOnClickListener {
            val intent = Intent(context, ResetPassword::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun reAuthenticate() {
        progressBar.visibility = View.VISIBLE
        val email = etEmail.text.toString()
        val password = etPass.text.toString()

        if(email.isEmpty()){
            etEmail.error = "Empty field!"
            etEmail.requestFocus()
            progressBar.visibility = View.GONE
        } else if(password.isEmpty()){
            etPass.error = "Empty field!"
            etPass.requestFocus()
            progressBar.visibility = View.GONE
        } else {

            val credentials = EmailAuthProvider.getCredential(email, password)

            currentUser.reauthenticate(credentials).addOnCompleteListener {
                if (it.isSuccessful) {
                    progressBar.visibility = View.GONE


                    if (operation == "email") {
                        etNewEmail.visibility = View.VISIBLE
                        btnLogIn.text = "update email"
                        btnLogIn.setOnClickListener {
                            updateEmail()
                        }
                    } else if (operation == "password") {
                        etNewPass.visibility = View.VISIBLE
                        etReNewPass.visibility = View.VISIBLE
                        btnLogIn.text = "update Password"
                        btnLogIn.setOnClickListener {
                            updatePassword()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updatePassword() {
        progressBar.visibility = View.VISIBLE
        val newPassword = etNewPass.text.toString()
        val reNewPassword = etReNewPass.text.toString()

        if (newPassword.isEmpty()) {
            etNewPass.error = "Empty field!"
            etNewPass.requestFocus()
            progressBar.visibility = View.GONE
        } else if (reNewPassword.isEmpty()) {
            etReNewPass.error = "Empty field!"
            etReNewPass.requestFocus()
            progressBar.visibility = View.GONE
        } else {
            if (newPassword == reNewPassword) {
                currentUser.updatePassword(newPassword).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT)
                            .show()
                        parentFragmentManager.beginTransaction().apply {
                            replace(
                                R.id.fragmentContainerView,
                                ProfileFragment(),
                                HomeActivity.PROFILE_FRAGMENT_TAG
                            )
                            addToBackStack(REAUTHENTICATION_FRAGMENT_TAG)
                            setTransition(TRANSIT_FRAGMENT_FADE)
                            commit()
                        }
                    } else {
                        Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateEmail() {
        progressBar.visibility = View.VISIBLE
        val newEmail = etNewEmail.text.toString()

        if (newEmail.isEmpty()) {
            etNewEmail.error = "Empty field!"
            etNewEmail.requestFocus()
            progressBar.visibility = View.GONE
        } else {

            currentUser.updateEmail(newEmail).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Email updated to $newEmail",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.beginTransaction().apply {
                        replace(
                            R.id.fragmentContainerView,
                            ProfileFragment(),
                            HomeActivity.PROFILE_FRAGMENT_TAG
                        )
                        addToBackStack(REAUTHENTICATION_FRAGMENT_TAG)
                        setTransition(TRANSIT_FRAGMENT_FADE)
                        commit()
                    }
//                    parentFragmentManager.beginTransaction().remove(this).commit()

//                    parentFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentContainerView, ProfileFragment(), HomeActivity.PROFILE_FRAGMENT_TAG)
//                        .setTransition(TRANSIT_FRAGMENT_FADE)
//                        .addToBackStack(null)
//                        .commit()
//                    val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
//                    bottomNavigationView?.visibility = View.VISIBLE

                    HomeActivity.currentFragment = ProfileFragment()
                } else {
                    Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_LONG)
                        .show()
                    progressBar.visibility = View.GONE
                }
            }
        }
    }
}