package com.example.mysns_project.Home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysns_project.DTO.FollowDTO
import com.example.mysns_project.DTO.PostDTO
import com.example.mysns_project.Myinfo.MyinfoFragment
import com.example.mysns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_post_recycler.view.*
import java.text.SimpleDateFormat

class PostRecyclerviewAdapter(var frag : Fragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var user : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    var postDTOs : ArrayList<PostDTO> = ArrayList()
    var postUidList : ArrayList<String> = ArrayList()
    init {
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance()
        var uid= user?.currentUser?.uid

        firestore?.collection("users")?.document(uid!!)?.get()?.addOnCompleteListener {
            if (it.isSuccessful) {
                var userDTO = it.result.toObject(FollowDTO::class.java)
                if (userDTO !=null){
                    getPost(userDTO.followings)
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_recycler,parent,false)

        return CustomViewHolder(view)
    }
    inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = (holder as CustomViewHolder).itemView

        firestore?.collection("profileImages")?.document(postDTOs[position].uid.toString())?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // ???????????? ??????????????? ???????????? ????????? ?????? ?????????????????? ?????? ??????
            if (value?.data != null) {
                var url = value.data!!["image"]
                Glide.with(holder.itemView.context).load(url).into(viewHolder.post_detail_profile_image)
            }
        }
        viewHolder.post_detail_username.text = postDTOs[position].userId
        Glide.with(holder.itemView.context).load(postDTOs[position].imageUrl).into(viewHolder.post_detail_content_image)
        viewHolder.post_detail_text.text = postDTOs[position].explain
        viewHolder.post_favorite_counter.text = "?????????"+ postDTOs[position].favoriteCount+"???"
        viewHolder.post_comment_counter.text = "??????" + postDTOs[position].commentCount +"???"
        viewHolder.post_detail_timestamp.text = SimpleDateFormat("yyyy-MM-dd hh:mm").format(postDTOs[position].timestamp)

        viewHolder.post_favorite_iamge.setOnClickListener {
            favoriteEvent(position)
        }
        if(postDTOs[position].favorites.containsKey(user?.currentUser!!.uid)) {
            viewHolder.post_favorite_iamge.setImageResource(R.drawable.ic_favorite)
        } else {
            viewHolder.post_favorite_iamge.setImageResource(R.drawable.ic_favorite_border)
        }
        viewHolder.post_detail_profile_image.setOnClickListener {
            var fragment = MyinfoFragment()
            var bundle = Bundle()
            bundle.putString("destinationUid",postDTOs[position].uid)
            fragment.arguments = bundle

            frag.activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_container,fragment)?.commit()
        }
        viewHolder.post_comment_button.setOnClickListener {
            val intent = Intent(frag.context,PostDetailActivity::class.java)
            intent.putExtra("postId",postUidList[position])
            frag.startActivity(intent)
        }
    }
    override fun getItemCount() = postDTOs.size

    private fun getPost(followers:MutableMap<String,Boolean>) {
        firestore?.collection("posts")?.orderBy("timestamp")?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // ???????????? ??????????????? ???????????? ?????????????????? ?????? ???????????? ?????? ?????????
            postDTOs.clear()
            postUidList.clear()

            for (snapshot in value.documents) {
                var item = snapshot.toObject(PostDTO::class.java)
                if (followers.keys.contains(item?.uid)) {
                    postDTOs.add(item!!)
                    postUidList.add((snapshot.id))
                }
            }
            notifyDataSetChanged()
        }
    }
    private fun favoriteEvent(position: Int) {
        val tsDoc = firestore?.collection("posts")?.document(postUidList[position])
        firestore?.runTransaction {
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            val postDTO = it.get(tsDoc!!).toObject(PostDTO::class.java)
            // ????????? ??????????????? ??????
            if (postDTO!!.favorites.containsKey(uid)){
                postDTO.favoriteCount--
                postDTO.favorites.remove(uid)
            } else {
                postDTO.favorites[uid]=true //?????? ?????????
                postDTO.favoriteCount++
            }
            it.set(tsDoc,postDTO)
            return@runTransaction
        }
    }
}