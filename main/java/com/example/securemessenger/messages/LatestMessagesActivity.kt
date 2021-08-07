package com.example.securemessenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.securemessenger.NewMessageActivity
import com.example.securemessenger.R
import com.example.securemessenger.models.ChatMessage
import com.example.securemessenger.registerlogin.RegisterActivity
import com.example.securemessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser : User? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recycler_view_latest_messages.adapter = adapter
        recycler_view_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessage
            val intent = Intent(view.context, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }
        fetchCurrentUser()
        verifyIsUserLoggedIn()
        listenForLatestMessages()
    }
    val latestMessageMap = HashMap<String, ChatMessage>()
    val adapter = GroupAdapter<GroupieViewHolder>()
    private fun updateRecyclerView(){
        adapter.clear()
       latestMessageMap.values.forEach{
           adapter.add(LatestMessage(it))
       }


    }


    private fun listenForLatestMessages(){
        val database = Firebase.database("https://messenger-bd839-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val fromId = FirebaseAuth.getInstance().uid
        val ref = database.getReference("latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                latestMessageMap[snapshot.key!!] = chatMessage!!
                updateRecyclerView()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                latestMessageMap[snapshot.key!!] = chatMessage!!
                updateRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }


    class LatestMessage(val chatMessage: ChatMessage) : Item<GroupieViewHolder>(){
        var chatPartnerUser : User? = null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            viewHolder.itemView.textView_message_latest_message.text = chatMessage.text

            var partnerId : String
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                partnerId = chatMessage.toId
            }else{
                partnerId = chatMessage.fromId
            }
            val database = Firebase.database("https://messenger-bd839-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val ref = database.getReference("users/$partnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                     chatPartnerUser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.textView_username_latest_message.text = chatPartnerUser?.username
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.imageView)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }

        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

    }
    private fun fetchCurrentUser(){
        val database = Firebase.database("https://messenger-bd839-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val uid = FirebaseAuth.getInstance().uid
        val ref = database.getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               currentUser = snapshot.getValue(User::class.java)
                Log.d("LatestMessageActivity","Current user is ${currentUser?.username}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun verifyIsUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_new_message -> {
             val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
             FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}