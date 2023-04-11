package com.example.bookstore

import com.google.firebase.firestore.FirebaseFirestore

class BookDao {

    private val db = FirebaseFirestore.getInstance()
    val bookCollection = db.collection("books")

    fun addBook(book: Book?){
        if (book!= null) {
            bookCollection.document(book.bookName).set(book)
        }
    }
}