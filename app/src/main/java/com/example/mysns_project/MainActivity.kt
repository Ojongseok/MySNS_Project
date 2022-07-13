package com.example.mysns_project

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.mysns_project.Chat.ChatFragment
import com.example.mysns_project.Chat.SearchFragment
import com.example.mysns_project.DTO.FriendDTO
import com.example.mysns_project.Home.HomeFragment
import com.example.mysns_project.Myinfo.MyinfoFragment
import com.example.mysns_project.PostUpload.AddPostActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {
    var user : FirebaseAuth? = null
    val PICK_PROFILE_FROM_ALBUM = 10
    var database : DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        user = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.selectedItemId = R.id.item1


        val email = user?.currentUser?.email
        val uid = user?.currentUser?.uid.toString()
        var friend = FriendDTO(uid,email)
        database?.child("users")?.child(uid)?.setValue(friend)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)

        if (requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            var imageUri = data?.data

            storageRef.putFile(imageUri!!).continueWithTask {
                return@continueWithTask storageRef.downloadUrl
            }?.addOnSuccessListener {
                var url = it.toString()
                var map =HashMap<String,Any>()
                map["image"] = url
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }?.addOnFailureListener {
                Toast.makeText(this,"프로필 변경에 실패했습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun setToolbarDefault() {
        main_toolbar_back_button.visibility = View.GONE
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()
        when(item.itemId) {
            R.id.item1 -> {
                main_toolbar_main_text.text = "SNS 프로젝트"
                supportFragmentManager.beginTransaction().replace(R.id.main_container,HomeFragment()).commit()
                return true
            }
            R.id.item2 -> {
                main_toolbar_main_text.text = "게시물 검색"
                supportFragmentManager.beginTransaction().replace(R.id.main_container,SearchFragment()).commit()
                return true
            }
            R.id.item3 -> {
                startActivity(Intent(this,AddPostActivity::class.java))
                return true
            }
            R.id.item4 -> {
                main_toolbar_main_text.text = "채팅 목록"
                supportFragmentManager.beginTransaction().replace(R.id.main_container,ChatFragment()).commit()
                return true
            }
            R.id.item5 -> {
                main_toolbar_main_text.text = "마이 페이지"
                var myinfoFragment = MyinfoFragment()
                var uid = user?.currentUser?.uid
                var bundle = Bundle()
                bundle.putString("destinationUid",uid)
                myinfoFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_container,myinfoFragment).commit()
                return true
            }
        }
        return false
    }
}