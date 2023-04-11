package com.example.bookstore

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_sell.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SellFragment : Fragment() {


    private lateinit var currentUser: FirebaseUser
    private var imageUri: Uri? = null
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (container != null) {
            mContext = container.context
        }

        val view = inflater.inflate(R.layout.fragment_sell, container, false)
        val ivBook: ImageView = view.findViewById(R.id.iv_book)
        val ivEdit: ImageView = view.findViewById(R.id.iv_edit)
        val btnPublishBook: Button = view.findViewById(R.id.btn_publishBook)

        currentUser = FirebaseAuth.getInstance().currentUser!!

        ivBook.setOnClickListener {
            choosePhoto()
        }

        ivEdit.setOnClickListener {
            choosePhoto()
        }

        btnPublishBook.setOnClickListener {
            updateUI()
        }

        return view
    }

    private fun updateUI() {

        pb_upload.visibility = View.VISIBLE

        val bookName = et_bookName.text.toString()
        val authorName = et_authorName.text.toString()
        val price = et_price.text.toString()

        if(imageUri != null) {

            if (bookName.isNotEmpty() && authorName.isNotEmpty() && price.isNotEmpty()) {

                val imageRef = FirebaseStorage.getInstance().reference.child("bookImages").child(imageUri!!.lastPathSegment!!)
                val uploadTask = imageRef.putFile(imageUri!!)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        val book = Book(currentUser.uid, System.currentTimeMillis().toString(), downloadUri, bookName, authorName, price)
                        val bookDao = BookDao()
                        bookDao.addBook(book)
                        Toast.makeText(mContext, "Published book successfully!", Toast.LENGTH_SHORT).show()

                        pb_upload.visibility = View.GONE
                        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, HomeFragment(), "home").commit()
                        HomeActivity.currentFragment = HomeFragment()
                        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                        bottomNavigationView!!.menu.getItem(0).isChecked = true

                    } else {
                        Toast.makeText(mContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                        pb_upload.visibility = View.GONE

                    }
                }
            } else {
                Toast.makeText(mContext, "Empty fields not allowed!", Toast.LENGTH_SHORT).show()
                pb_upload.visibility = View.GONE

            }

        } else {
            Toast.makeText(mContext, "Please select a photo of the book!", Toast.LENGTH_SHORT).show()
            pb_upload.visibility = View.GONE
        }
    }

    private fun choosePhoto() {
        val builder = AlertDialog.Builder(mContext)
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
            HomeActivity.PICK_PHOTO_CODE
        )
    }

    private fun choosePhotoFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, HomeActivity.CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                HomeActivity.PICK_PHOTO_CODE -> {
                    imageUri = data?.data
                }

                HomeActivity.CAMERA_REQUEST_CODE -> {
                    var photo = data?.extras?.get("data") as Bitmap
                    //Rotates the photo 90 degree
                    val matrix = Matrix()
                    matrix.postRotate(90F)
                    photo = Bitmap.createBitmap(photo, 0, 0, photo.width, photo.height, matrix, true)

                    imageUri = convertBitmapToUri(photo)
                }
            }

            updatePhoto()
        }
    }

    private fun updatePhoto() {
        iv_book.setImageURI(imageUri)
    }

    private fun convertBitmapToUri(photo: Bitmap): Uri? {
//        val cw = ContextWrapper(context);
//        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//        val file = File(directory, "image" + System.currentTimeMillis().toString() + ".jpg");
//
//
//        val bytes = ByteArrayOutputStream()
//        photo.compress(Bitmap.CompressFormat.PNG, 100, bytes)
//        val bitmapData = bytes.toByteArray()
//
//        if(!file.exists()) {
//            try {
//                val fileOutPut = FileOutputStream(file)
//                fileOutPut.write(bitmapData)
//                fileOutPut.flush()
//                fileOutPut.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        val imgUri = Uri.fromFile(file)
//        file.delete()
//        return imgUri

        val tempFile = File.createTempFile("temprentpk", ".png")
        val bytes = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)
    }
}