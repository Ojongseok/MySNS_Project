package com.example.mysns_project.PostUpload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mysns_project.DTO.PostDTO
import com.example.mysns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_post.*
import java.text.SimpleDateFormat
import java.util.*

class AddPostActivity : AppCompatActivity() {
    val PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    var photoUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        addpost_image.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)
        }
        addpost_upload_button.setOnClickListener {
            if (addpost_edit_explain.text.toString().length > 0 && photoUri != null) {
                postUpload()
            } else {
                Toast.makeText(this,"업로드에 실패하였습니다.",Toast.LENGTH_SHORT).show()
            }
        }
        addpost_toolbar_back_button.setOnClickListener {
            onBackPressed()
        }
    }

    private fun postUpload() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_hhmmss").format(Date())
        val imageFilename = "JPEG_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFilename)

        storageRef?.putFile(photoUri!!)?.continueWithTask {
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener {
            Toast.makeText(this, "업로드에 성공했습니다.", Toast.LENGTH_SHORT).show()
            var postDTO = PostDTO()
            postDTO.imageUrl = it.toString()
            postDTO.uid = auth?.currentUser?.uid
            postDTO.expain = addpost_edit_explain.text.toString()
            postDTO.userId = auth?.currentUser?.email
            postDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("posts")?.document()?.set(postDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }?.addOnFailureListener {
            Toast.makeText(this, "업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            addpost_image.setImageURI(data?.data)
            photoUri = data?.data
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}