package com.example.scaffoldsmart.util

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.scaffoldsmart.admin.admin_models.ChatUserModel
import com.example.scaffoldsmart.admin.admin_models.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminMessageListenerService : Service() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var myNotification: AdminMsgNotification
    private var senderUid: String? = null
    private val notifiedMessageIds = mutableSetOf<String>() // Tracks notified message IDs
    private var chatClientList = ArrayList<ChatUserModel>()

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        myNotification = AdminMsgNotification()
        loadNotifiedMessageIds() // Load notified message IDs from SharedPreferences

        Log.d("MessageListenerService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retrieve the sender UID from the intent
        senderUid = intent?.getStringExtra("SenderUid")
        if (senderUid != null) {
            retrieveClientChatData() // Fetch chat data and set up listeners
        } else {
            Log.e("MessageListenerService", "SenderUid is null")
        }
        return START_STICKY // Service will be restarted if terminated
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Fetches the list of chat users from Firebase and sets up message listeners for each chat.
     */
    private fun retrieveClientChatData() {
        database.reference.child("ChatUser")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatClientList.clear() // Clear the list to avoid duplicates
                    for (child in snapshot.children) {
                        val chatItem = child.getValue(ChatUserModel::class.java)
                        if (chatItem != null && chatItem.uid != senderUid && !chatItem.uid.isNullOrEmpty()) {
                            chatClientList.add(chatItem) // Add only relevant chats
                        }
                    }
                    setupMessageListener(chatClientList) // Set up listeners for each chat
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessageListenerService", "Failed to retrieve chat data", error.toException())
                }
            })
    }

    /**
     * Sets up a Firebase listener for messages in each chat room.
     */
    private fun setupMessageListener(chatClientList: ArrayList<ChatUserModel>) {
        Log.d("MessageListenerService", "Setting up message listeners for ${chatClientList.size} chats")
        chatClientList.forEach { chat ->
            val receiverUid = chat.uid
            val receiverName = chat.userName
            val senderRoom = senderUid + receiverUid // Unique room ID for the chat

            // Create a unique listener for each chat room
            database.reference.child("Chat").child(senderRoom).child("Messages")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (data in snapshot.children) {
                            val message = data.getValue(MessageModel::class.java)
                            if (message?.messageId != null && !notifiedMessageIds.contains(message.messageId)) {
                                // Check if the message is new and not from the sender
                                if (message.senderId != senderUid && !message.seen!!) {
                                    Log.d("MessageListenerService", "New message detected: ${message.messageId}")
                                    // Notify the user
                                    myNotification.handleNotification(
                                        applicationContext,
                                        message.senderName!!,
                                        message.message!!,
                                        receiverName,
                                        receiverUid
                                    )
                                    // Mark the message as notified
                                    notifiedMessageIds.add(message.messageId!!)
                                    saveNotifiedMessageIds() // Persist the updated set
                                } else {
                                    Log.d("MessageListenerService", "Message already seen or from sender: ${message.messageId}")
                                }
                            } else {
                                Log.d("MessageListenerService", "Message already notified or invalid: ${message?.messageId}")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MessageListenerService", "Database error: ${error.message}")
                    }
                })
        }
    }

    /**
     * Saves the notified message IDs to SharedPreferences for persistence.
     */
    private fun saveNotifiedMessageIds() {
        val sharedPreferences = getSharedPreferences("NotifiedMessages", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("notifiedMessageIds", notifiedMessageIds)
        editor.apply()
        Log.d("MessageListenerService", "Saved notifiedMessageIds: $notifiedMessageIds")
    }

    /**
     * Loads the notified message IDs from SharedPreferences.
     */
    private fun loadNotifiedMessageIds() {
        val sharedPreferences = getSharedPreferences("NotifiedMessages", MODE_PRIVATE)
        val loadedIds = sharedPreferences.getStringSet("notifiedMessageIds", mutableSetOf()) ?: mutableSetOf()
        notifiedMessageIds.addAll(loadedIds)
        Log.d("MessageListenerService", "Loaded notifiedMessageIds: $notifiedMessageIds")
    }

    override fun onDestroy() {
        super.onDestroy()
        saveNotifiedMessageIds() // Save notified message IDs before service is destroyed
        Log.d("MessageListenerService", "Service destroyed")
    }
}