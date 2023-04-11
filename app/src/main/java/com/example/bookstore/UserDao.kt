package com.example.bookstore

import com.google.firebase.firestore.FirebaseFirestore

class UserDao {
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    fun addUser(user: User?){
        if (user != null) {
            user.email?.let { userCollection.document(it).set(user) }
        }
    }
}