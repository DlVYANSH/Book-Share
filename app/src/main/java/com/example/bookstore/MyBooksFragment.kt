package com.example.bookstore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyBooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_my_books, container, false)

        recyclerView = view.findViewById(R.id.rvMyBook)

        setUpRecyclerView()

        return view
    }

    private fun setUpRecyclerView() {
        val uid = FirebaseAuth.getInstance().uid
        val reference  = FirebaseFirestore.getInstance().collection("books").whereEqualTo("userId", uid)
        val query: Query = reference.orderBy("createdAt")
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

        mAdapter = HomeAdapter(this, recyclerViewOptions)

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
}