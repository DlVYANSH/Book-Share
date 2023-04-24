package com.example.bookstore

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.chat_user_list.view.*

class ChatUserAdapter(
    private val latestMessages: MutableList<LatestMessage>,
    private val itemClickListener: ItemClickListener
    )
    : RecyclerView.Adapter<ChatUserAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfilePic: CircleImageView = itemView.iv_profile_pic_chat_user_list
        val tvUserName: TextView = itemView.tv_user_name_chat_user_list
        val tvLatestMessage: TextView = itemView.tv_latest_message_chat_user_list
        val tvTime: TextView = itemView.tv_time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_user_list, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentLatestInfo = latestMessages[position]

        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser

        val receiver: User = if(currentLatestInfo.receiver.userId == currentFirebaseUser!!.uid){
            currentLatestInfo.sender
        } else {
            currentLatestInfo.receiver
        }

        val util = Util()

        holder.apply {
            tvUserName.text = receiver.name
            tvLatestMessage.text = currentLatestInfo.textMessage
            tvTime.text = ""
            Glide.with(holder.itemView.context).load(receiver.profilePic).into(ivProfilePic)
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClicked(currentLatestInfo)
        }
    }

    override fun getItemCount() = latestMessages.size

    interface ItemClickListener{
        fun onItemClicked(latestMessage: LatestMessage)
    }
}