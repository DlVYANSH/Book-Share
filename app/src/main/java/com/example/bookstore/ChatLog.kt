package com.example.bookstore

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat_log.view.*
import kotlin.math.log

class ChatLog : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_chat_log, container, false)

//        val tv = view.findViewById<TextView>(R.id.textView)

//        val json = arguments?.get(BookDetail.USER_TAG).toString()
//
//        val gson = Gson()
//        val user = gson.fromJson(json, User::class.java)

        val user = arguments?.get(BookDetail.USER_TAG) as User

        Log.d("ChatLog", "${user.name}")


//        tv.text = user.userId


        return view
    }
}