package com.example.securemessenger.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.securemessenger.R
import com.example.securemessenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button.setOnClickListener {
            val email = email_login.text.toString()
            val password = password_login.text.toString()
            Log.d("Main","Attempt to login with email: $email")
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful)return@addOnCompleteListener
                    Log.d("Main","Successfully logged in user with uid: ${it.result?.user?.uid}" )
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("Main","Failed to log in user: ${it.message}")
                    Toast.makeText(this,"Failed to Log in user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        back_to_register_login_textview.setOnClickListener {
            finish()
        }
    }
}