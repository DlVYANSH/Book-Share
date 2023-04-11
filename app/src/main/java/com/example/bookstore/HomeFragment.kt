package com.example.bookstore

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: HomeAdapter
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (container != null) {
            mContext = container.context
        }

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)

        setUpRecyclerView()

        return view
    }

    private fun setUpRecyclerView() {
        val bookDao = BookDao()
        val bookCollection = bookDao.bookCollection
        val query = bookCollection.orderBy("createdAt", Query.Direction.DESCENDING)
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