package com.example.bookstore

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ProfileFragment : Fragment() {

    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private var profilePic : Any? = null
    private var imageUri: Uri? = null

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

    companion object{
        private const val GALLERY_CODE = 5
        private const val CAMERA_CODE = 6
        const val TAG_OPERATION = "tag operation"
        const val TAG_MESSAGE = "message to be shown on textView"
    }

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

        btnDelAccount.setOnClickListener {
            deleteAccount()
        }

        return view
    }

    private fun deleteAccount() {

        val fragment = ReAuthenticationFragment()
        val bundle = Bundle()
        bundle.putString(TAG_OPERATION, "delete")
        bundle.putString(TAG_MESSAGE, "You need to re-authenticate in order to delete the account")
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment, ReAuthenticationFragment.REAUTHENTICATION_FRAGMENT_TAG)
            addToBackStack(null)
            setTransition(TRANSIT_FRAGMENT_FADE)
            commit()
        }

        HomeActivity.currentFragment = ReAuthenticationFragment()


    }

    private fun changePassword() {

        val fragment = ReAuthenticationFragment()
        val bundle = Bundle()
        bundle.putString(TAG_OPERATION, "password")
        bundle.putString(TAG_MESSAGE, "You need to re-authenticate in order to change the password")
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment, ReAuthenticationFragment.REAUTHENTICATION_FRAGMENT_TAG)
            addToBackStack(null)
            setTransition(TRANSIT_FRAGMENT_FADE)
            commit()
        }
        HomeActivity.currentFragment = ReAuthenticationFragment()
    }

    private fun updateEmail() {

        val fragment = ReAuthenticationFragment()
        val bundle = Bundle()
        bundle.putString(TAG_OPERATION, "email")
        bundle.putString(TAG_MESSAGE, "You need to re-authenticate in order to change the email id")
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment, ReAuthenticationFragment.REAUTHENTICATION_FRAGMENT_TAG)
            addToBackStack(null)
            setTransition(TRANSIT_FRAGMENT_FADE)
            commit()
        }
        HomeActivity.currentFragment = ReAuthenticationFragment()
    }

    private fun updateName() {
        etDisplayName.isFocusableInTouchMode = true
        etDisplayName.isCursorVisible = true
        etDisplayName.isFocusable = true

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
                        etDisplayName.isCursorVisible = false
                        etDisplayName.isFocusable = false
                    } else {
                        Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun updateUI() {
        val users =
            FirebaseFirestore.getInstance().collection("users").document("${currentUser!!.email}")
        users.get().addOnSuccessListener {
            if (it != null) {
                profilePic = it.data?.get("profilePic")
                if (profilePic != null) {
                    Glide.with(this@ProfileFragment).load(profilePic).into(ivProfilePic)
                }
            }
        }

        Log.d(TAG, "updateUI: ${currentUser!!.displayName}")
        etDisplayName.setText(currentUser!!.displayName)
        tvEmail.text = currentUser!!.email
    }

    private fun changeProfilePicture(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Pick Photo from..")
            .setPositiveButton("Camera",
                DialogInterface.OnClickListener { _, _ ->
                    choosePhotoFromCamera()

                })
            .setNegativeButton("Gallery",
                DialogInterface.OnClickListener { _, _ ->
                    choosePhotoFromGallery()
                })

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun choosePhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"     // means only image will be selected
        startActivityForResult(
            Intent.createChooser(intent, "Choose Images"),
            GALLERY_CODE
        )
    }

    private fun choosePhotoFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_CODE -> {
                    imageUri = data?.data
                }

                CAMERA_CODE -> {
                    var photo = data?.extras?.get("data") as Bitmap

                    //Rotates the photo 90 degree
//                    val matrix = Matrix()
//                    matrix.postRotate(90F)
//                    photo = Bitmap.createBitmap(photo, 0, 0, photo.width, photo.height, matrix, true)

                    val util = Util()
                    imageUri = util.convertBitmapToUri(photo)
                }
            }

            updatePhoto()
        }
    }

    private fun updatePhoto() {
        ivProfilePic.setImageURI(imageUri)
        btnSave.visibility = View.VISIBLE

        btnSave.setOnClickListener {
            uploadAndUpdateProfilePic()
        }
    }

    private fun uploadAndUpdateProfilePic() {
        progressBar.visibility = View.VISIBLE
        val imageRef: StorageReference? = FirebaseStorage.getInstance().reference.child("profilePicture").child(imageUri!!.lastPathSegment!!)
        val uploadTask = imageRef?.putFile(imageUri!!)

        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                progressBar.visibility = View.GONE
                btnSave.visibility = View.GONE
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }?.addOnCompleteListener {
            if(it.isSuccessful){
                val downloadUrl = it.result.toString()
                val user = User(downloadUrl, currentUser!!.uid, currentUser!!.displayName, currentUser!!.email.toString())
                val userDao = UserDao()
                userDao.addUser(user)

                val profileUpdate = userProfileChangeRequest {
                    photoUri = imageUri
                }

                currentUser!!.updateProfile(profileUpdate).addOnCompleteListener { task->
                    if(task.isSuccessful){
                        Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        btnSave.visibility = View.GONE

                    } else {
                        Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        btnSave.visibility = View.GONE
                    }
                }

            } else {
                progressBar.visibility = View.GONE
                btnSave.visibility = View.GONE
                Toast.makeText(requireContext(), it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
