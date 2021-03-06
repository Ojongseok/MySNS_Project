package com.example.mysns_project.Home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysns_project.DTO.PostDTO
import com.example.mysns_project.MainActivity
import com.example.mysns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.item_comment_recycler.*
import kotlinx.android.synthetic.main.item_comment_recycler.view.*
import kotlinx.android.synthetic.main.item_post_recycler.post_detail_content_image
import kotlinx.android.synthetic.main.item_post_recycler.post_detail_text
import kotlinx.android.synthetic.main.item_post_recycler.post_detail_timestamp
import kotlinx.android.synthetic.main.item_post_recycler.post_detail_username
import kotlinx.android.synthetic.main.item_post_recycler.view.*
import java.text.SimpleDateFormat

class PostDetailActivity() : AppCompatActivity() {
    var firestore : FirebaseFirestore? = null
    var user : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance()
        val postId = intent.getStringExtra("postId").toString()

        getInfo()
        post_detail_recyclerview.adapter = CommentRecyclerViewAdapter()
        post_detail_recyclerview.layoutManager = LinearLayoutManager(this)

        comment_button.setOnClickListener {
            var comment = PostDTO.Comment()
            comment.userId = user?.currentUser?.email
            comment.uid = user?.currentUser?.uid
            comment.comment = post_detail_comment_text.text.toString()
            comment.timestamp = System.currentTimeMillis()
            post_detail_comment_text.text = null

            FirebaseFirestore.getInstance().collection("posts").document(postId)
                .collection("comments").document().set(comment)
        }
    }
    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val comments : ArrayList<PostDTO.Comment> = ArrayList()
        init {
            FirebaseFirestore.getInstance().collection("posts")
                .document(intent.getStringExtra("postId").toString())
                .collection("comments").orderBy("timestamp").addSnapshotListener { value, error ->
                    comments.clear()
                    if (value == null) return@addSnapshotListener  // ???????????? ??????????????? ???????????? ????????? ?????? ?????????????????? ?????? ??????
                    for (snapshot in value.documents) {
                        comments.add(snapshot.toObject(PostDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_recycler,parent,false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView
            viewHolder.comment_explain_text.text = comments[position].comment
            viewHolder.comment_profile_userid.text = comments[position].userId
            viewHolder.comment_timestamp.text = SimpleDateFormat("yyyy-MM-dd hh:mm").format(comments[position].timestamp)

            firestore?.collection("profileImages")?.document(comments[position].uid.toString())?.get()?.addOnSuccessListener {
                Glide.with(applicationContext).load(it["image"].toString()).into(viewHolder.comment_profile_userimage)
            }?.addOnFailureListener { Toast.makeText(applicationContext,"????????? ???????????? ??????",Toast.LENGTH_SHORT).show() }
        }
        inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)
        override fun getItemCount() = comments.size
    }
    private fun getInfo() {
        val postId = intent.getStringExtra("postId")
        firestore?.collection("posts")?.document(postId!!)?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // ???????????? ??????????????? ???????????? ????????? ?????? ?????????????????? ?????? ??????
            if (value?.data != null) {
                post_detail_username.text = value.data!!["userId"].toString()
                post_detail_timestamp.text = SimpleDateFormat("yyyy-MM-dd hh:mm").format(value.data!!["timestamp"])
                post_detail_text.text = value.data!!["explain"].toString()

                if (!this.isFinishing){
                    Glide.with(applicationContext).load(value.data!!["imageUrl"]).into(post_detail_content_image)
                }

                firestore?.collection("profileImages")?.document(value.data!!["uid"].toString())?.get()?.addOnSuccessListener {
                    if (this.isFinishing){
                        return@addOnSuccessListener  // ?????? ?????????????????? ????????????????????? ?????????
                    }
                    Glide.with(this).load(it["image"].toString()).into(post_detail_profile_image)
                }
            }
        }
    }
}