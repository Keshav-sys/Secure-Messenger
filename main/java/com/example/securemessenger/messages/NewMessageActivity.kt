package com.example.securemessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.securemessenger.messages.ChatLogActivity
import com.example.securemessenger.messages.LatestMessagesActivity
import com.example.securemessenger.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.row_resource_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"
        fetchUsers()
    }
    companion object{
        val USER_KEY = "USER_KEY"
    }
    private fun fetchUsers(){
        val database = Firebase.database("https://messenger-bd839-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {

                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null && user.uid != LatestMessagesActivity.currentUser?.uid) {
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener{ item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                    finish()
                }
                recycler_view_new_message.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
      viewHolder.itemView.username_textview_new_message.text = user.username
      Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.username_imageView_new_message)
    }

    override fun getLayout(): Int {
       return R.layout.row_resource_new_message
    }

}