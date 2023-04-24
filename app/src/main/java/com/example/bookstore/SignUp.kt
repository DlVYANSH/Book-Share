package com.example.bookstore

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_log_in.tv_logIn
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class SignUp : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var firebaseAuth: FirebaseAuth

    companion object{
        private const val CAMERA_CODE = 1
        private const val GALLERY_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firebaseAuth = FirebaseAuth.getInstance()

        cv_profileImg_signUp.setOnClickListener {
            getImage()
        }

        btn_signUp.setOnClickListener {
            signUp()
        }

        tv_logIn.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUp() {

        progressBar_SU.visibility = View.VISIBLE

        cv_profileImg_signUp.setOnClickListener {
            getImage()
        }

        if(imageUri == null){
            Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_SHORT).show()
            progressBar_SU.visibility = View.GONE
            return
        }
        val email = et_email.editText?.text.toString()
        val name = et_name.editText?.text.toString()
        val pass = et_password.editText?.text.toString()
        val confirmPass = et_confirm_pass.editText?.text.toString()

        if(email.isNotEmpty() && name.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()){
            if(pass.length < 8){
                et_password.error = "Password must contains at least 8 characters!"
                et_password.requestFocus()
                progressBar_SU.visibility = View.GONE
            }
            else if(pass == confirmPass){
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                    if(it.isSuccessful){
                        val firebaseUser = firebaseAuth.currentUser
                        if (firebaseUser != null) {
                            updateUI(firebaseUser, name)
                        }
                    } else {
                        if(it.exception is FirebaseAuthUserCollisionException)
                            Toast.makeText(this, "User already registered with this email!!", Toast.LENGTH_SHORT).show()
                        progressBar_SU.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                progressBar_SU.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "Empty fields are not allowed!!", Toast.LENGTH_SHORT).show()
            progressBar_SU.visibility = View.GONE
        }
    }

    private fun getImage(){
        val builder = AlertDialog.Builder(this)
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
                    val matrix = Matrix()
                    matrix.postRotate(90F)
                    photo = Bitmap.createBitmap(photo, 0, 0, photo.width, photo.height, matrix, true)

                    val util = Util()
                    imageUri = util.convertBitmapToUri(photo)
                }
            }

            updatePhoto()
        }
    }

    private fun updatePhoto() {
        iv_profilePic.setImageURI(imageUri)
    }

    private fun updateUI(firebaseUser: FirebaseUser, name: String) {

        val imageRef: StorageReference? = FirebaseStorage.getInstance().reference.child("profilePicture").child(imageUri!!.lastPathSegment!!)
        val uploadTask = imageRef?.putFile(imageUri!!)

        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }?.addOnCompleteListener {
            if(it.isSuccessful){
                val downloadUrl = it.result.toString()
                val user = User(downloadUrl, firebaseUser.uid, name, firebaseUser.email.toString())
                val userDao = UserDao()
                userDao.addUser(user)

                val profileUpdate = userProfileChangeRequest {
                    displayName = name
                    photoUri = imageUri
                }

                firebaseUser.updateProfile(profileUpdate)

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}