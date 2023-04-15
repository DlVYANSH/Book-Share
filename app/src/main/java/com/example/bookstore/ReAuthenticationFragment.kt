package com.example.bookstore

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ReAuthenticationFragment() : Fragment() {
    private lateinit var tvMessage: TextView
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

        tvMessage = view.findViewById(R.id.tv_message)
        etEmail = view.findViewById(R.id.et_email_RA)
        etPass = view.findViewById(R.id.et_password_RA)
        tvForgotPass = view.findViewById(R.id.tv_forgotPass_RA)
        btnLogIn = view.findViewById(R.id.btn_reAuthenticate)
        etNewEmail = view.findViewById(R.id.et_newEmail_RA)
        etNewPass = view.findViewById(R.id.et_newPass_RA)
        etReNewPass = view.findViewById(R.id.et_re_new_pass_RA)
        progressBar = view.findViewById(R.id.progressBarRA)

        tvMessage.text = arguments?.get(ProfileFragment.TAG_MESSAGE).toString()

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

        val operation = arguments?.get(ProfileFragment.TAG_OPERATION)

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
                    etEmail.isFocusable = false
                    etPass.isFocusable = false
                    tvForgotPass.isClickable = false

                    if (operation == "email") {
                        val params  = btnLogIn.layoutParams as ViewGroup.MarginLayoutParams
                        //converts dp to pixel as setMargin function take input in pixels
                        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 76f, resources.displayMetrics)
                        //setting margin dynamically after re-authenticating to show the edit text for new email
                        params.setMargins(16, margin.toInt(), 16, 16)
                        btnLogIn.layoutParams = params

                        etNewEmail.visibility = View.VISIBLE
                        btnLogIn.text = "update email"

                        btnLogIn.setOnClickListener {
                            updateEmail()
                        }
                    } else if (operation == "password") {

                        val params  = btnLogIn.layoutParams as ViewGroup.MarginLayoutParams
                        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 136f, resources.displayMetrics).toInt()
                        params.setMargins(16, margin, 16, 16)
                        btnLogIn.layoutParams = params

                        etNewPass.visibility = View.VISIBLE
                        etReNewPass.visibility = View.VISIBLE
                        btnLogIn.text = "update Password"
                        btnLogIn.setOnClickListener {
                            updatePassword()
                        }
                    } else if(operation == "delete"){

                        btnLogIn.text = "delete account"
                        btnLogIn.setOnClickListener {
                            deleteAccount()
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

    private fun deleteAccount() {
//        progressBar.visibility = View.VISIBLE

        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want delete your account?")
            .setPositiveButton("Yes"
            ) { _, _ ->
                progressBar.visibility = View.VISIBLE

                currentUser!!.delete().addOnCompleteListener {
                    if(it.isSuccessful){
                        val db = FirebaseFirestore.getInstance()
                        val queryBook = db.collection("books").whereEqualTo("userId", currentUser!!.uid)
                        val queryUser = db.collection("users").document(currentUser!!.email.toString())

                        queryUser.delete()

                        queryBook.get().addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                for(document in task.result){
                                    document.reference.delete()
                                    progressBar.visibility = View.GONE
                                }
                                //goto login activity
                                startActivity(Intent(requireContext(), LogInActivity::class.java))
                                activity?.finish()
                            } else {
                                Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_LONG).show()
                                progressBar.visibility = View.GONE
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                    }
                }
            }
            .setNegativeButton("No"
            ) { _, _ ->
                progressBar.visibility = View.GONE
            }.show()
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