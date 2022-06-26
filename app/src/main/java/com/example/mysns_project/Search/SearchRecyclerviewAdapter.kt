package com.example.mysns_project.Search

import android.app.ActivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mysns_project.DTO.PostDTO
import com.example.mysns_project.Myinfo.MyinfoFragment
import com.example.mysns_project.R
import com.google.firebase.firestore.FirebaseFirestore

class SearchRecyclerviewAdapter(var frag : Fragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var firestore : FirebaseFirestore? = null
    var postDTOs : ArrayList<PostDTO> = ArrayList()
    init {
        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("posts")?.orderBy("timestamp")?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // 스냅샷이 살아있는데 다른데서 액티나 플먼 종료하려하면 자주 튕김
            postDTOs.clear()
            for (snapshot in value!!.documents) {
                var postDTO = snapshot.toObject(PostDTO::class.java)
                postDTOs.add(postDTO!!)
            }
            notifyDataSetChanged()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_search,parent,false)
        val width = view.resources.displayMetrics.widthPixels / 3
        val imageView = ImageView(parent.context)
        imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)

        return CustomViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val imageView = (holder as CustomViewHolder).imageView
        Glide.with(holder.imageView.context).load(postDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        imageView.setOnClickListener {
            val fragment = MyinfoFragment()
            val bundle = Bundle()
            bundle.putString("destinationUid",postDTOs[position].uid)
            bundle.putString("userId",postDTOs[position].userId)
            fragment.arguments = bundle

            frag.activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_container,fragment)?.commit()
        }
    }
    inner class CustomViewHolder(var imageView : ImageView) : RecyclerView.ViewHolder(imageView)
    override fun getItemCount() = postDTOs.size
}