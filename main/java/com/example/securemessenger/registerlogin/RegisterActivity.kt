package com.example.securemessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.securemessenger.R
import com.example.securemessenger.messages.LatestMessagesActivity
import com.example.securemessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener{
           performRegister()
        }
        already_have_an_account_text_view.setOnClickListener {
            Log.d("RegisterActivity","Try to show login activity")
            //launch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        select_photo_button.setOnClickListener {
            Log.d("RegisterActivity","Try to show photo selecter")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }
    var selectedPhotoUri: Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity","Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            circular_imageView_register.setImageBitmap(bitmap)
            select_photo_button.alpha = 0f
        }

    }
    private fun performRegister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Please enter text in email/pw",Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("RegisterActivity","Email is: $email")
        Log.d("RegisterActivity","Password is: $password")
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegisterActivity","Successfully created user with uid: ${it.result?.user?.uid}" )
                uploadImageToFireBaseStroage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Failed to create user: ${it.message}")
                Toast.makeText(this,"Failed to create user: ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToFireBaseStroage(){
        if(selectedPhotoUri == null)return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity","File location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("RegisterActivity","Failed to upload image")
            }
    }
    private fun saveUserToFirebaseDatabase(profileImageUrl:String){
        Log.d("RegisterActivity","saveUser fun is called")
        val uid = FirebaseAuth.getInstance().uid?:""

        val database = Firebase.database("https://messenger-bd839-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("users/$uid")

        val user = User(uid,username_edittext_register.text.toString(),profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Successfully saved user to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Failed to save user to database")
            }
    }
}

