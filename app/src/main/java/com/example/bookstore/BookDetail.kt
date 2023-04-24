package com.example.bookstore

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class BookDetail : Fragment() {

    private lateinit var ivBook: ImageView
    private lateinit var tvBookName: TextView
    private lateinit var tvAuthorName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var btnContactSeller: Button
    private lateinit var book: Book
    private var otherUser: User? = null
    private var currentUser: User? = null

    companion object {
        const val OTHER_USER_TAG = "Book's owner"
        const val CURRENT_USER_TAG = "Current user"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_book_detail, container, false)

        ivBook = view.findViewById(R.id.iv_book_bookDetail)
        tvBookName = view.findViewById(R.id.tv_book_name_bookDetail)
        tvAuthorName = view.findViewById(R.id.tv_author_name_bookDetail)
        tvPrice = view.findViewById(R.id.tv_price_bookDetail)
        btnContactSeller = view.findViewById(R.id.btn_contact_seller)

        book = arguments?.get(HomeFragment.TAG_BUNDLE) as Book

        GlobalScope.launch {
            getUserData()
        }

        updateUI()


        btnContactSeller.setOnClickListener {
            //If data of book's owner is not fetched yet
            if(otherUser == null || currentUser == null){
                return@setOnClickListener
            }

            //If book's owner is opening chat of it's own book
            if(book.userId == FirebaseAuth.getInstance().currentUser!!.uid){
                Toast.makeText(requireContext(), "It's your book. You can not message yourself!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fragment = ChatLog()
            val bundle = Bundle()

           /*** Pass the users to bookDetail because we will need the user's data who had
            uploaded the book to open it's chat
            and the current user's data is required for chatAdapter. we are fetching it here only
            because it takes time to get the data from firebase***/
            bundle.putParcelable(OTHER_USER_TAG, otherUser)
            bundle.putParcelable(CURRENT_USER_TAG, currentUser)
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainerView, fragment)
                setTransition(TRANSIT_FRAGMENT_FADE)
                addToBackStack(null)
                commit()
            }

            HomeActivity.currentFragment = fragment
        }

        return view
    }

    private suspend fun getUserData() {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("userId", book.userId)

        withContext(Dispatchers.IO) {
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        otherUser = document.toObject(User::class.java)
                    }
                } else {
                    Log.d("BookDetail", "getUserData: ${task.exception?.message}")
                }
            }
        }

        //getting current user's data to pass to the next fragment

        val firebaseAuth = FirebaseAuth.getInstance()
        val reference = FirebaseFirestore.getInstance().collection("users")
            .document(firebaseAuth.currentUser!!.email.toString())

        withContext(Dispatchers.IO) {
            reference.get().addOnSuccessListener {
                if (it != null) {
                    currentUser = User(
                        it.data?.get("profilePic")?.toString(),
                        it.data?.get("userId")?.toString(),
                        it.data?.get("name")?.toString(),
                        it.data?.get("email")?.toString()
                    )
                    Log.d(ChatLog.TAG, "setUpRecyclerView: ${currentUser!!.name}")
                }
            }
        }
    }

    private fun updateUI() {
        Log.d("BookDetail", book.bookImage)
        Glide.with(requireContext()).load(book.bookImage).into(ivBook)
        tvBookName.text = book.bookName
        tvAuthorName.text = book.authorName
        tvPrice.text = "Rs " + book.price
    }
}