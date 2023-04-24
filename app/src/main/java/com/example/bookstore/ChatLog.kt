package com.example.bookstore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatLog : Fragment() {

    companion object{
        const val TAG = "ChatLog"
    }

    private lateinit var etMessage: EditText

    private lateinit var mAdapter: ChatAdapter
    private lateinit var currentFirebaseUser: FirebaseUser
    private lateinit var otherUser: User
    private lateinit var currentUser: User
    private lateinit var recyclerView: RecyclerView
    private var messageList = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_chat_log, container, false)

        val btnSend: ImageView = view.findViewById(R.id.btn_send_message)
        etMessage = view.findViewById(R.id.et_new_message)

        otherUser = arguments?.get(BookDetail.OTHER_USER_TAG) as User
        currentUser = arguments?.get(BookDetail.CURRENT_USER_TAG) as User

        Log.d(TAG, "onCreateView: ${otherUser.userId}, ${currentUser.userId}")

        currentFirebaseUser = FirebaseAuth.getInstance().currentUser!!

        recyclerView = view.findViewById(R.id.rv_chat_log)

        (activity as HomeActivity).setActionBarTitle(otherUser.name.toString())

        listenForMessageChange()

        setUpRecyclerView()

        btnSend.setOnClickListener {
            uploadMessageToFireBase()
        }

        return view
    }

    private fun listenForMessageChange() {
        var toIdAndFromId = otherUser.userId + currentUser.userId
        toIdAndFromId = toIdAndFromId.toCharArray().apply { sort() }.joinToString("")
        
        val reference = FirebaseFirestore.getInstance().collection("messages")
            .whereEqualTo("toIdAndFromId", toIdAndFromId)
            .orderBy("createdAt", Query.Direction.ASCENDING)

//            .whereEqualTo("fromId", currentUser.userId)
//            .whereEqualTo("toId", otherUser.userId)
//            .orderBy("createdAt", Query.Direction.ASCENDING)


        reference.addSnapshotListener { value, error ->
            if(error != null){
                return@addSnapshotListener
            }

            if (value != null) {
                for(dc in value.documentChanges){
                    if(dc.type == DocumentChange.Type.ADDED){
                        val message = Message(
                            dc.document["id"].toString(),
                            dc.document["textMessage"].toString(),
                            dc.document["fromId"].toString(),
                            dc.document["toId"].toString(),
                            dc.document["toIdAndFromId"].toString(),
                            dc.document["createdAt"].toString()
                        )

                        Log.d(TAG, "listenForMessageChange: ${dc.document["textMessage"]}")

                        messageList.add(message)
                        mAdapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(messageList.size-1)
                        etMessage.text.clear()
                    }
                }
            }
        }

    }

    private fun uploadMessageToFireBase() {
        val textMessage = etMessage.text.toString()

        if(textMessage.isEmpty()){
            return
        }

        val reference = FirebaseFirestore.getInstance().collection("messages")
        val documentId = reference.document().id

        var toIdAndFromId = otherUser.userId + currentUser.userId
        toIdAndFromId = toIdAndFromId.toCharArray().apply { sort() }.joinToString("")

        val message = Message(
            documentId,
            textMessage,
            currentFirebaseUser.uid,
            otherUser.userId.toString(),
            toIdAndFromId,
            System.currentTimeMillis().toString()
        )

        reference.document(documentId).set(message).addOnCompleteListener {
            if(it.isSuccessful){
                Log.d(TAG, "uploadMessageToFireBase: Message uploaded successfully with document id $documentId")

            } else {
                Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
        
        val latestMessageReference = FirebaseFirestore.getInstance().collection("latest-Messages")
        
        val latestMessage = LatestMessage(
            textMessage,
            otherUser,
            currentUser,
            System.currentTimeMillis().toString()
        )

        latestMessageReference.document(toIdAndFromId).set(latestMessage).addOnCompleteListener {
            if(it.isSuccessful){
                Log.d(TAG, "uploadMessageToFireBase: latest message uploaded successfully")
            } else {
                Log.d(TAG, "uploadMessageToFireBase: ${it.exception?.message}")
            }
        }
    }

    private fun setUpRecyclerView() {
        mAdapter  = ChatAdapter(currentUser, otherUser, messageList)

        recyclerView.adapter = mAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
    }
}