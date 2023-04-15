package com.example.bookstore

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class BookDetail : Fragment() {

    private lateinit var ivBook: ImageView
    private lateinit var tvBookName: TextView
    private lateinit var tvAuthorName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var btnContactSeller: Button
    private lateinit var book: Book
    private var user: User? = null

    companion object {
        const val USER_TAG = "USER_TAG"
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

        updateUI()

        getUserData()

        btnContactSeller.setOnClickListener {

            if(user == null){
                return@setOnClickListener
            }

            val fragment = ChatLog()
            val bundle = Bundle()

                bundle.putParcelable(USER_TAG, user)
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragmentContainerView, fragment)
                    setTransition(TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }

                HomeActivity.currentFragment = fragment
//            }
        }

        return view
    }

    private fun getUserData() {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("userId", book.userId)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    user = document.toObject(User::class.java)
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