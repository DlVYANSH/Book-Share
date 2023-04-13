package com.example.bookstore

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private var profilePic : Any? = null

    private lateinit var ivProfilePic: ImageView
    private lateinit var ivEditPP: ImageView
    private lateinit var ivEditName: ImageView
    private lateinit var etDisplayName: EditText
    private lateinit var tvEmail: TextView
    private lateinit var ivEditEmail: ImageView
    private lateinit var btnChangePass: Button
    private lateinit var btnMyBooks: Button
    private lateinit var btnDelAccount: Button
    private lateinit var btnSave : Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        btnMyBooks= view.findViewById(R.id.btnMyBooks)
        ivProfilePic = view.findViewById(R.id.iv_profilePic)
        ivEditPP = view.findViewById(R.id.iv_edit_pp)
        ivEditName = view.findViewById(R.id.iv_edit_name)
        etDisplayName = view.findViewById(R.id.et_display_name)
        ivEditEmail = view.findViewById(R.id.iv_edit_email)
        tvEmail = view.findViewById(R.id.tv_email)
        btnChangePass = view.findViewById(R.id.btn_change_pass)
        btnDelAccount = view.findViewById(R.id.btn_deleteAcc)
        btnSave = view.findViewById(R.id.btn_save)
        progressBar = view.findViewById(R.id.progressBarProfile)

        currentUser = FirebaseAuth.getInstance().currentUser
        db = FirebaseFirestore.getInstance()

        updateUI()

        ivProfilePic.setOnClickListener {
            changeProfilePicture()
        }

        ivEditPP.setOnClickListener {
            changeProfilePicture()
        }

        ivEditName.setOnClickListener {
            updateName()
        }

        ivEditEmail.setOnClickListener {
            updateEmail()
        }

        btnChangePass.setOnClickListener {
            changePassword()
        }

        btnMyBooks.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainerView, MyBooksFragment())
                addToBackStack(null)
                commit()
            }
            HomeActivity.currentFragment = MyBooksFragment()
        }

        return view
    }

    private fun changePassword() {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, ReAuthenticationFragment("password"), ReAuthenticationFragment.REAUTHENTICATION_FRAGMENT_TAG)
            addToBackStack(null)
            setTransition(TRANSIT_FRAGMENT_FADE)
            commit()
        }
        HomeActivity.currentFragment = ReAuthenticationFragment("password")
    }

    private fun updateEmail() {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, ReAuthenticationFragment("email"), ReAuthenticationFragment.REAUTHENTICATION_FRAGMENT_TAG)
            addToBackStack(null)
            setTransition(TRANSIT_FRAGMENT_FADE)
            commit()
        }
        HomeActivity.currentFragment = ReAuthenticationFragment("email")
    }

    private fun updateName() {
//        etDisplayName.focusable = View.FOCUSABLE
//        etDisplayName.isCursorVisible = true
//        etDisplayName.requestFocus()

        btnSave.visibility = View.VISIBLE

        btnSave.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val newName = etDisplayName.text.toString()

            if(newName.isEmpty()){
                etDisplayName.error = "Empty fields!"
                etDisplayName.requestFocus()
                progressBar.visibility = View.GONE
            } else {

                val profileUpdates = userProfileChangeRequest {
                    displayName = newName
                }

                currentUser!!.updateProfile(profileUpdates).addOnCompleteListener {
                    if (it.isSuccessful) {
                        db.collection("users")
                            .document(currentUser!!.email.toString())
                            .set(
                                User(
                                    profilePic.toString(),
                                    currentUser!!.uid,
                                    newName,
                                    currentUser!!.email
                                )
                            )
                        Toast.makeText(
                            requireContext(),
                            "Name changed to $newName",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        btnSave.visibility = View.GONE
                        progressBar.visibility = View.GONE
                    } else {
                        Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun updateUI() {
        val users = FirebaseFirestore.getInstance().collection("users").document("${currentUser!!.email}")
        users.get().addOnSuccessListener {
            if(it != null){
                profilePic = it.data?.get("profilePic")
                if(profilePic != null) {
                    Glide.with(this@ProfileFragment).load(profilePic).into(ivProfilePic)
                }
            }
        }

        Log.d(TAG, "updateUI: ${currentUser!!.displayName}")
        etDisplayName.setText(currentUser!!.displayName)
        tvEmail.text = currentUser!!.email
    }

    private fun changeProfilePicture() {
        TODO("Not yet implemented")
    }

    override fun onAttachFragment(childFragment: Fragment) {
        parentFragmentManager.popBackStack(ReAuthenticationFragment.REAUTHENTICATION_FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}
