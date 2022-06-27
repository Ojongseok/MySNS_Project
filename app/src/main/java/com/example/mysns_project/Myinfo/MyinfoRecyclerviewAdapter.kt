package com.example.mysns_project.Myinfo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mysns_project.DTO.PostDTO
import com.example.mysns_project.Home.PostDetailActivity
import com.example.mysns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_myinfo.*
import kotlinx.android.synthetic.main.fragment_myinfo.view.*

class MyinfoRecyclerviewAdapter(var uid : String, var frag : Fragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var postDTOs : ArrayList<PostDTO> = ArrayList()
    var postUidList : ArrayList<String> = ArrayList()
    var firestore : FirebaseFirestore? = null
    init {
        postDTOs.clear()
        postUidList.clear()
        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("posts")?.whereEqualTo("uid",uid)?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // 스냅샷이 살아있는데 다른데서 액티나 플먼 종료하려하면 자주 튕김
            for (snapshot in value?.documents!!) {
                postDTOs.add(snapshot.toObject(PostDTO::class.java)!!)
                postUidList.add((snapshot.id))
            }
            frag.myinfo_post_count.text = postDTOs.size.toString()
            notifyDataSetChanged()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_myinfo,parent,false)
        val width = view.resources.displayMetrics.widthPixels / 3
        val imageView = ImageView(parent.context)
        imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)

        return CustomViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val imageView = (holder as CustomViewHolder).imageView
        Glide.with(holder.itemView.context).load(postDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        imageView.setOnClickListener {
            var intent = Intent(imageView.context,PostDetailActivity::class.java)
            intent.putExtra("postId",postUidList[position])
            frag.startActivity(intent)
        }
    }
    inner class CustomViewHolder(var imageView : ImageView) : RecyclerView.ViewHolder(imageView)
    override fun getItemCount() = postDTOs.size
}