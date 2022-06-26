package com.example.mysns_project.Myinfo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mysns_project.DTO.FollowDTO
import com.example.mysns_project.LoginActivity
import com.example.mysns_project.MainActivity
import com.example.mysns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_myinfo.view.*

class MyinfoFragment : Fragment() {
    val PICK_PROFILE_FROM_ALBUM = 10
    var firestore : FirebaseFirestore? = null
    var auth : FirebaseAuth? = null
    var currentUid : String? = null   // 내 id
    var userId : String? = null    // 다른 사람 or 내가 선택한 uid
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_myinfo, container, false)
        val mainActivity = (activity as MainActivity)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUid = auth?.currentUser?.uid
        userId = auth?.currentUser?.uid

        if (arguments != null) {
            userId = requireArguments().getString("destinationUid")
            if (userId == currentUid) {  // 나의 정보 페이지
                view.myinfo_follow_button.text = "로그아웃"
                view.myinfo_follow_button.setOnClickListener {
                    Toast.makeText(context,"로그아웃 되었습니다.",Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(mainActivity,LoginActivity::class.java))
                }
                view.myinfo_profile_image.setOnClickListener {
                    var photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
                }
            } else {
                view.myinfo_follow_button.text = "팔로우"
                mainActivity.main_toolbar_back_button.visibility = View.VISIBLE
                mainActivity.main_toolbar_back_button.setOnClickListener {
                    mainActivity.bottomNavigationView.selectedItemId = R.id.item1
                }
                view.myinfo_follow_button.setOnClickListener {
                    requestFollow()
                }
            }
        }

        view?.myinfo_recyclerview?.adapter = MyinfoRecyclerviewAdapter(userId!!)
        view?.myinfo_recyclerview?.layoutManager = GridLayoutManager(activity,3)
        getProfileImages()
        getFollower()
        getFollowing()

        return view
    }
    fun getFollower() {
        firestore?.collection("users")?.document(userId!!)?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // 스냅샷이 살아있는데 다른데서 액티나 플먼 종료하려하면 자주 튕김
            var followDTO = value.toObject(FollowDTO::class.java)
            view?.myinfo_follower_count?.text = followDTO?.followerCount.toString()
            if (followDTO?.followers?.containsKey(currentUid)!!) {
                view?.myinfo_follow_button?.text = "팔로우 취소"
            } else {
                if (userId != currentUid) {
                    view?.myinfo_follow_button?.text = "팔로우"
                }
            }
        }
    }
    fun getFollowing() {
        firestore?.collection("users")?.document(userId!!)?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // 스냅샷이 살아있는데 다른데서 액티나 플먼 종료하려하면 자주 튕김
            var followDTO = value.toObject(FollowDTO::class.java)
            view?.myinfo_following_count?.text=followDTO?.followingCount.toString()
        }
    }
    private fun getProfileImages() {
        firestore?.collection("profileImages")?.document(userId!!)?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener  // 스냅샷이 살아있는데 다른데서 액티나 플먼 종료하려하면 자주 튕김
            if (value?.data != null) {
                var url = value.data!!["image"]
                activity?.let {
                    Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(requireView().myinfo_profile_image)
                }
            }
        }
    }
    private fun requestFollow() {
        var tsDocFoolwing = firestore!!.collection("users").document(currentUid!!)
        firestore?.runTransaction {
            var followDTO = it.get(tsDocFoolwing).toObject(FollowDTO::class.java)
            // 아무도 팔로잉 안했을 때
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[userId!!] = true

                it.set(tsDocFoolwing,followDTO)
                return@runTransaction
            }
            // 내가 제3자 아이디를 이미 팔로잉 하고 있을 경우
            if (followDTO.followings.containsKey(userId)) {
                followDTO?.followingCount--
                followDTO?.followings.remove(userId)
            } else {
                // 내가 제 3자를 팔로잉 하지 않았을 경우
                followDTO?.followingCount++
                followDTO?.followings[userId!!] = true
            }
            it.set(tsDocFoolwing,followDTO)
            return@runTransaction
        }
        var tsDocFollower = firestore!!.collection("users").document(userId!!)
        firestore?.runTransaction {
            var followDTO = it.get(tsDocFollower).toObject(FollowDTO::class.java)
            // 아무도 팔로워 하지 않았을 경우
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUid!!] = true

                it.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            // 제 3자의 유저를 내가 팔로잉 하고 있는 경우
            if (followDTO!!.followers.containsKey(currentUid!!)) {
                followDTO!!.followerCount--
                followDTO!!.followers.remove(currentUid)
            } else {
                // 제3자를 내가 팔로워 하지 않은 경우 -> 팔로워 하겠다
                followDTO!!.followerCount++
                followDTO!!.followers[currentUid!!] = true
            }
            it.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }
}