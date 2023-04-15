package com.example.bookstore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class HomeAdapter(
    private val mContext: Fragment,
    options: FirestoreRecyclerOptions<Book>,
    private val clickListener: ItemClickListener
) : FirestoreRecyclerAdapter<Book, HomeAdapter.PostViewHolder>(
    options
) {
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgBook: ImageView = itemView.findViewById(R.id.iv_bookImg)
        val bookName: TextView = itemView.findViewById(R.id.tv_bookName)
        val authorName: TextView = itemView.findViewById(R.id.tv_authorName)
        val price: TextView = itemView.findViewById(R.id.tv_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_list, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Book) {
        holder.apply {
            Glide.with(mContext).load(model.bookImage.toUri()).into(imgBook)
            bookName.text = model.bookName
            authorName.text = model.authorName
            price.text = model.price
        }

        holder.itemView.setOnClickListener {
            clickListener.onItemClicked(model)
        }
    }

    interface ItemClickListener{
        fun onItemClicked(book: Book)
    }
}