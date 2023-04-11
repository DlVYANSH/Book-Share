package com.example.bookstore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyBookDao {
    private val firebaseAuth = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    var bookCollection = db.collection(firebaseAuth!!.uid)

    fun addBook(myBook: Book?){
        if (myBook != null) {
            bookCollection.document(myBook.bookName).set(myBook)
        }
    }
}