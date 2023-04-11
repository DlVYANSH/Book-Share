package com.example.bookstore

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {

    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private var profilePic : Any? = null

    private lateinit var ivProfilePic: ImageView
    private lateinit var ivEditPP: ImageView
//    private lateinit var tvDisplayName: EditText
    private lateinit var ivEditName: ImageView
    private lateinit var etDisplayName: EditText
//    private lateinit var tvEmail: TextView
    private lateinit var ivEditEmail: ImageView
    private lateinit var etEmail: EditText
    private lateinit var tvChangePass: TextView
    private lateinit var btnMyBooks: Button
    private lateinit var btnDelAccount: Button
    private lateinit var btnSave : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        btnMyBooks= view.findViewById(R.id.btnMyBooks)
        ivProfilePic = view.findViewById(R.id.iv_profilePic)
        ivEditPP = view.findViewById(R.id.iv_edit_pp)
//        tvDisplayName = view.findViewById(R.id.tv_display_name)
        ivEditName = view.findViewById(R.id.iv_edit_name)
        etDisplayName = view.findViewById(R.id.et_display_name)
//        tvEmail = view.findViewById(R.id.tv_email)
        ivEditEmail = view.findViewById(R.id.iv_edit_email)
        etEmail = view.findViewById(R.id.et_email)
        tvChangePass = view.findViewById(R.id.tv_change_pass)
        btnDelAccount = view.findViewById(R.id.btn_deleteAcc)
        btnSave = view.findViewById(R.id.btn_save)

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

    private fun updateEmail() {
        btnSave.visibility = View.VISIBLE
        
        btnSave.setOnClickListener {
            currentUser!!.sendEmailVerification().addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d(TAG, "updateEmail: verification successful!")
                    Toast.makeText(context, "Verification mail sent to the registered mail id", Toast.LENGTH_SHORT).show()
                    currentUser!!.updateEmail(etEmail.text.toString())
                }
            }
        }
    }

    private fun updateName() {
//        etDisplayName.focusable = View.FOCUSABLE
//        etDisplayName.isCursorVisible = true
//        etDisplayName.requestFocus()

        etDisplayName.isFocusable = true
        etDisplayName.isCursorVisible = true
        etDisplayName.requestFocus()

        btnSave.visibility = View.VISIBLE

        btnSave.setOnClickListener {
            val newName = etDisplayName.text.toString()

            val profileUpdates = userProfileChangeRequest {
                displayName = newName
            }

            currentUser!!.updateProfile(profileUpdates).addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("users")
                        .document(currentUser!!.email.toString())
                        .set(User(profilePic.toString(), currentUser!!.uid, newName, currentUser!!.email))
                    Toast.makeText(requireContext(), "Name changed to $newName", Toast.LENGTH_SHORT)
                        .show()
                    btnSave.visibility = View.GONE
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
        etEmail.setText(currentUser!!.email)
    }

    private fun changeProfilePicture() {
        TODO("Not yet implemented")
    }

}
