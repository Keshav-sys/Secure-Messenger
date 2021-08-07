package com.example.securemessenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.securemessenger.NewMessageActivity
import com.example.securemessenger.R
import com.example.securemessenger.models.ChatMessage
import com.example.securemessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_log_from_row.view.*
import kotlinx.android.synthetic.main.chat_log_from_row.view.textView_row
import kotlinx.android.synthetic.main.chat_log_to_row.view.*
import kotlinx.android.synthetic.main.chat_log_to_row.view.imageView_row as imageView_row1

class ChatLogActivity : AppCompatActivity() {

    companion object{
        const val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser : User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
        recycler_view_chat_log.adapter = adapter
        listenForMessages()
        send_button_chatlog.setOnClickListener{
            Log.d(TAG,"Try to send message")

            performSendMessage()


        }
    }

    private fun listenForMessages(){
        val database = Firebase.database("https://messenger-bd839-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = database.getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if(chatMessage != null){
                    Log.d(TAG,chatMessage.text)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                         val currentUser = LatestMessagesActivity.currentUser
                         adapter.add(ChatItemFrom(chatMessage.text,currentUser!!))
                    }else {
                        adapter.add(ChatItemTo(chatMessage.text, toUser!!))
                    }
                    recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }
    private fun performSendMessage(){
        val database = Firebase.database("https://messenger-bd839-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val user = toUser
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = user?.uid
        val ref = database.getReference("/user-messages/$fromId/$toId").push()
        val id = ref.key
        val text = editText_chat_log.text.toString()
        val chatMessage = ChatMessage(id!!,text,fromId,toId!!,System.currentTimeMillis()/1000)
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"Saved Message in Firebase with id : ${ref.key}")
                editText_chat_log.text.clear()
                recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
            .addOnFailureListener{
                Log.d(TAG,"Failed to save message in Firebase")
            }

        val toref = database.getReference("/user-messages/$toId/$fromId").push()
        toref.setValue(chatMessage)
            .addOnSuccessListener {
                recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

        val latestMessageref = database.getReference("latest-messages/$fromId/$toId")
        latestMessageref.setValue(chatMessage)

        val latestMessagetoref = database.getReference("latest-messages/$toId/$fromId")
        latestMessagetoref.setValue(chatMessage)
    }
}

class ChatItemFrom(val text:String,val user :User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
      viewHolder.itemView.textView_row.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_row)
    }

    override fun getLayout(): Int {
       return R.layout.chat_log_from_row
    }

}

class ChatItemTo(val text:String,val user :User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_row.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_row)
    }

    override fun getLayout(): Int {
        return R.layout.chat_log_to_row
    }

}