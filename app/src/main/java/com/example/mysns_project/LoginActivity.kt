package com.example.mysns_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        login_button.setOnClickListener {
            createAndLogin()
        }
    }
    private fun createAndLogin() {
        if (edit_text_email.text.toString().length > 0&&edit_text_password.text.toString().length>0) {
            auth!!.createUserWithEmailAndPassword(edit_text_email.text.toString(), edit_text_password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    } else if(task.exception?.message.isNullOrEmpty()){
                        Toast.makeText(this,"회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        signinEmail()
                    }
                }
        } else {
            Toast.makeText(this,"이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun signinEmail() {
        auth!!.signInWithEmailAndPassword(edit_text_email.text.toString(), edit_text_password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    moveMainPage(auth!!.currentUser)
                    Toast.makeText(this,"로그인에 성공했습니다.",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"로그인에 실패했습니다.",Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
    //자동 로그인 설정
    override fun onStart() {
        super.onStart()

        moveMainPage(auth?.currentUser)
    }
}