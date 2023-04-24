package com.example.bookstore

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(
    private val currentUser: User,
    private val otherUser: User,
    private val messageList: MutableList<Message>
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    companion object{
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVE = 2
    }

    abstract class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: Message)
    }

    inner class SentMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message_from)
        private val ivProfilePic: ImageView = itemView.findViewById(R.id.iv_profile_pic_clFrom)

        override fun bind(message: Message) {
//            Log.d("ChatAdapter", "bindSent: ${message.textMessage}")
            tvMessage.text = message.textMessage
            Glide.with(itemView.context).load(currentUser.profilePic).into(ivProfilePic)
        }
    }

    inner class ReceiveMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message_to)
        private val ivProfilePic: ImageView = itemView.findViewById(R.id.iv_profile_pic_clTo)

        override fun bind(message: Message) {
//            Log.d("ChatAdapter", "bindReceive: ${message.textMessage}")
            tvMessage.text = message.textMessage
            Glide.with(itemView.context).load(otherUser.profilePic).into(ivProfilePic)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if(viewType == VIEW_TYPE_SENT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_from, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_to, parent, false)
            ReceiveMessageViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        Log.d("ChatAdapter", "getItemViewType: ${message.textMessage}, ${message.fromId}, ${currentUser.userId}")
        return if(currentUser.userId == message.fromId){
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemCount() = messageList.size
}