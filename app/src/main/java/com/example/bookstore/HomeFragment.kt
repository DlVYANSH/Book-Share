package com.example.bookstore

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment(), BookAdapter.ItemClickListener, BookAdapter.LongPressListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: BookAdapter
    private lateinit var mContext: Context

    companion object{
        const val TAG_BUNDLE = "TAG_BUNDLE"
    }

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
        val bookCollection = FirebaseFirestore.getInstance().collection("books")
        val query = bookCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

        mAdapter = BookAdapter(this, this,  this, recyclerViewOptions)

        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
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
        val fragment = BookDetail()
        val bundle = Bundle()
        fragment.arguments = bundle
        bundle.putParcelable(TAG_BUNDLE, book)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment)
            setTransition(TRANSIT_FRAGMENT_FADE)
            addToBackStack(null)
            commit()
        }
        HomeActivity.currentFragment = fragment
    }

    override fun onLongPressed(book: Book) {
        return
    }

}