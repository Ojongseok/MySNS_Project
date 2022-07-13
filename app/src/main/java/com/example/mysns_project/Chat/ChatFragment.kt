package com.example.mysns_project.Chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mysns_project.DTO.FriendDTO
import com.example.mysns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.item_chat_user.view.*

class ChatFragment : Fragment() {
    var database : DatabaseReference? = null
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        database = Firebase.database.reference

        view.chat_recyclerview.adapter = ChatRecyclerViewAdapter()
        view.chat_recyclerview.layoutManager = LinearLayoutManager(requireContext())

        return view
    }
    inner class ChatRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var friend : ArrayList<FriendDTO> = ArrayList()
        init {
            database?.child("users")?.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    friend.clear()
                    for (data in snapshot.children) {
                        val item  = data.getValue<FriendDTO>()
                        if (item?.userId.equals(uid)) {continue}
                        friend.add(item!!)
                    }
                    notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user,parent,false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView

//            viewHolder.item_chat_user_email.text = friend[position].email
            viewHolder.setOnClickListener {


            }
        }
        inner class CustomViewHolder(view : View?) : RecyclerView.ViewHolder(view!!)
        override fun getItemCount() = 3
    }
}