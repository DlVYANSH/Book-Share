package com.example.bookstore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyBooksFragment : Fragment(), BookAdapter.ItemClickListener, BookAdapter.LongPressListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: BookAdapter
    private lateinit var currentUser: FirebaseUser
    private lateinit var collectionReference: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_my_books, container, false)

        currentUser = FirebaseAuth.getInstance().currentUser!!

        collectionReference = FirebaseFirestore.getInstance().collection("books")


        recyclerView = view.findViewById(R.id.rvMyBook)

        setUpRecyclerView()

        listenForChangeInDatabase()

        return view
    }

    private fun listenForChangeInDatabase(){
        val reference = FirebaseFirestore.getInstance().collection("books")

        reference.addSnapshotListener { snapShot, error ->
            if(error != null){
                return@addSnapshotListener
            }

            if(snapShot != null){
                val reference = collectionReference.whereEqualTo("userId", currentUser.uid)

                val query: Query = reference.orderBy("createdAt", Query.Direction.DESCENDING)

                val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

                mAdapter.updateOptions(recyclerViewOptions)
            }
        }
    }

    private fun setUpRecyclerView() {
        val reference = collectionReference.whereEqualTo("userId", currentUser.uid)

        val query: Query = reference.orderBy("createdAt", Query.Direction.DESCENDING)

        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

        mAdapter = BookAdapter(this, this,this, recyclerViewOptions)

        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }

    }

    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()

        mAdapter.stopListening()
    }

    override fun onItemClicked(book: Book) {
        return
    }

    override fun onLongPressed(book: Book) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes"
            ) { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("No"
            ) { _, _ ->

            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteBook(book: Book){
        val reference = FirebaseFirestore.getInstance().collection("books").document(book.id)
        reference.delete().addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(requireContext(), "Deleted book successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}