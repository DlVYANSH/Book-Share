package com.example.bookstore

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatFragment : Fragment(), ChatUserAdapter.ItemClickListener {

    companion object{
        const val TAG = "ChatFragment"
    }

    private lateinit var currentFirebaseUser: FirebaseUser
    private val latestMessages: MutableList<LatestMessage> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: ChatUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.rv_fragment_chat)

        currentFirebaseUser = FirebaseAuth.getInstance().currentUser!!

        fetchUsers()

        setUpRecyclerAdapter()

        return view
    }

    private fun setUpRecyclerAdapter() {
        mAdapter = ChatUserAdapter(latestMessages, this)
        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
            //It will create a line at the end of the items
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            setHasFixedSize(true)
        }
    }

    private fun fetchUsers(){
        latestMessages.clear()
        val reference = FirebaseFirestore.getInstance().collection("latest-Messages")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        reference.addSnapshotListener { value, error ->
            if(error != null){
                return@addSnapshotListener
            }

            if(value != null){
                for(dc in value.documents){
                    val latestMessage = dc.toObject(LatestMessage::class.java)

                    if(latestMessage?.sender?.userId == currentFirebaseUser.uid
                        || latestMessage?.receiver?.userId == currentFirebaseUser.uid) {

                        latestMessages.add(latestMessage)
                        mAdapter.notifyDataSetChanged()

                        Log.d(TAG, "onCreateView: $latestMessage")
                    }
                }
            }
        }
    }

    override fun onItemClicked(latestMessage: LatestMessage) {
        val fragment = ChatLog()
        val bundle = Bundle()
        bundle.putParcelable(BookDetail.OTHER_USER_TAG, latestMessage.receiver)
        bundle.putParcelable(BookDetail.CURRENT_USER_TAG, latestMessage.sender)
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment, "chatFragment")
            addToBackStack(null)
            setTransition(TRANSIT_FRAGMENT_FADE)
            commit()
        }
        HomeActivity.currentFragment = fragment
    }

}
