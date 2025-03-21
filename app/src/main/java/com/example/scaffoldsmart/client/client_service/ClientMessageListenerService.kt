package com.example.scaffoldsmart.client.client_service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.MessageModel
import com.example.scaffoldsmart.util.ClientMsgNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientMessageListenerService : Service() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var myNotification: ClientMsgNotification
    private var senderRoom: String? = null
    private var receiverUid: String? = null
    private var receiverName: String? = null
    private var senderUid: String? = null
    private val notifiedMessageIds = mutableSetOf<String>() // Tracks notified message IDs

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        myNotification = ClientMsgNotification()
        loadNotifiedMessageIds() // Load notified message IDs from SharedPreferences

        Log.d("ClientMessageListenerService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retrieve the sender UID from the intent
        senderUid = intent?.getStringExtra("SenderUid")
        if (senderUid != null) {
            retrieveAdminData() // Fetch chat data and set up listeners
        } else {
            Log.e("ClientMessageListenerService", "SenderUid is null")
        }
        return START_STICKY // Service will be restarted if terminated
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Fetches the list of chat users from Firebase and sets up message listeners for each chat.
     */
    private fun retrieveAdminData() {
        database.reference.child("Admin")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val admin = child.getValue(AdminModel::class.java)
                        if (admin != null && admin.id != senderUid && !admin.id.isNullOrEmpty()) {
                            setupMessageListener(admin) // Set up listeners for admin chat
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClientMessageListenerService", "Failed to retrieve chat data", error.toException())
                }
            })
    }

    /**
     * Sets up a Firebase listener for messages in each chat room.
     */
    private fun setupMessageListener(admin: AdminModel) {
        receiverUid = admin.id
        receiverName = admin.name
        senderRoom = senderUid + receiverUid // Unique room ID for the chat

        database.reference.child("Chat").child(senderRoom!!).child("Messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val message = data.getValue(MessageModel::class.java)
                        if (message?.messageId != null && !notifiedMessageIds.contains(message.messageId)) {
                            // Check if the message is new and not from the sender
                            if (message.senderId != senderUid && !message.seen!!) {
                                Log.d("ClientMessageListenerService", "New message detected: ${message.messageId}")
                                // Notify the user
                                myNotification.handleNotification(
                                    applicationContext,
                                    message.message!!,
                                    receiverName,
                                    receiverUid
                                )
                                // Mark the message as notified
                                notifiedMessageIds.add(message.messageId!!)
                                saveNotifiedMessageIds() // Persist the updated set
                            } else {
                                Log.d("ClientMessageListenerService", "Message already seen or from sender: ${message.messageId}")
                            }
                        } else {
                            Log.d("ClientMessageListenerService", "Message already notified or invalid: ${message?.messageId}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClientMessageListenerService", "Database error: ${error.message}")
                }
            })
    }

    /**
     * Saves the notified message IDs to SharedPreferences for persistence.
     */
    private fun saveNotifiedMessageIds() {
        val sharedPreferences = getSharedPreferences("NotifiedMessages", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("notifiedMessageIds", notifiedMessageIds)
        editor.apply()
        Log.d("ClientMessageListenerService", "Saved notifiedMessageIds: $notifiedMessageIds")
    }

    /**
     * Loads the notified message IDs from SharedPreferences.
     */
    private fun loadNotifiedMessageIds() {
        val sharedPreferences = getSharedPreferences("NotifiedMessages", MODE_PRIVATE)
        val loadedIds = sharedPreferences.getStringSet("notifiedMessageIds", mutableSetOf()) ?: mutableSetOf()
        notifiedMessageIds.addAll(loadedIds)
        Log.d("ClientMessageListenerService", "Loaded notifiedMessageIds: $notifiedMessageIds")
    }

    override fun onDestroy() {
        super.onDestroy()
        saveNotifiedMessageIds() // Save notified message IDs before service is destroyed
        Log.d("ClientMessageListenerService", "Service destroyed")
    }
}