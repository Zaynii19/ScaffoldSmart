package com.example.scaffoldsmart.client

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.MessageRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.DateHeader
import com.example.scaffoldsmart.admin.admin_models.MessageModel
import com.example.scaffoldsmart.admin.admin_viewmodel.ChatViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.MessageViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.MessageViewModelFactory
import com.example.scaffoldsmart.databinding.ActivityClientChatBinding
import com.example.scaffoldsmart.util.DateFormater
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import java.util.Calendar
import java.util.Date

class ClientChatActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientChatBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: MessageRcvAdapter
    private var messages: ArrayList<Any>? = ArrayList() // Hold both Message and DateHeader
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var dialog: ProgressDialog? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private var receiverName: String? = null
    private var senderName: String? = null
    private var isActivityVisible: Boolean? = null
    private lateinit var viewModelC: ChatViewModel
    private lateinit var viewModel: MessageViewModel
    private lateinit var chatPreferences: SharedPreferences
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Handle back button press here
            startActivity(Intent(this@ClientChatActivity, ClientMainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setStatusBarColor()

        dialog = ProgressDialog(this@ClientChatActivity)
        messages = ArrayList()
        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)

        dialog?.setMessage("Uploading image...")
        dialog?.setCancelable(false)

        // Animate CardView when keyboard appears or disappears
        setupKeyboardVisibilityListener()

        initializeChatDetails()
        setRcv()
        setUpPresenceListener()
        getChatsData()
        setupSendButton()
        setupAttachmentButton()
        setupTypingStatus()

        onBackPressedDispatcher.addCallback(this@ClientChatActivity, onBackPressedCallback)
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setRcv() {
        val layoutManager = LinearLayoutManager(this@ClientChatActivity, LinearLayoutManager.VERTICAL, false)
        binding.rcv.layoutManager = layoutManager
        adapter = MessageRcvAdapter(this, messages, senderRoom, receiverRoom, senderUid)
        binding.rcv.adapter = adapter
        layoutManager.stackFromEnd = true
        binding.rcv.setHasFixedSize(true)
    }

    private fun initializeChatDetails() {
        senderUid = chatPreferences.getString("SenderUid", null)
        senderName = chatPreferences.getString("SenderName", null)
        receiverUid = chatPreferences.getString("receiverUid", null)
        receiverName = chatPreferences.getString("receiverName", null)

        binding.userName.text = receiverName

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        // Create the ViewModel using the factory
        senderRoom?.let { viewModel = ViewModelProvider(this, MessageViewModelFactory(it))[MessageViewModel::class.java] }
        viewModel.retrieveMessage()

        observeMsgLiveData()
    }

    private fun observeMsgLiveData() {
        viewModel.observeMessageLiveData().observe(this@ClientChatActivity) { msgs ->
            messages?.let { messageList ->
                messageList.clear()
                var previousDate: String? = null

                msgs?.forEach { msg ->
                    // Safe handling of message and timestamp
                    msg.timestamp?.let { timestamp ->
                        val currentDate = DateFormater.formatDateHeader(timestamp)

                        // Add date header if date changed
                        if (currentDate != previousDate) {
                            messageList.add(DateHeader(currentDate))
                            previousDate = currentDate
                        }

                        // Add the message
                        messageList.add(msg)

                        // Mark as seen if conditions met
                        if (msg.senderId == receiverUid &&
                            msg.seen == false &&
                            isActivityVisible == true) {
                            msg.messageId?.let { markMessageAsSeen(it) }
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            } ?: Log.w("ClientChatDebug", "Messages list is null - cannot update UI")
        }
    }

    private fun setUpPresenceListener() {
        receiverUid?.let {
            Firebase.database.reference.child("ChatUser").child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val status = snapshot.child("status").getValue(String::class.java)
                            val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java)
                            if (!status.isNullOrEmpty() && lastSeen != null) {
                                if (status == "Offline") {
                                    binding.status.visibility = View.VISIBLE
                                    binding.status.text = DateFormater.formatTimestampForLastSeen(lastSeen)
                                } else {
                                    binding.status.text = status
                                    binding.status.visibility = View.VISIBLE
                                }
                            } else {
                                binding.status.visibility = View.GONE
                            }
                        } else {
                            binding.status.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatActivityDebug", "Database error: ${error.message}")
                    }
                })
        }
    }

    private fun setupSendButton() {
        binding.sendMsgBtn.setOnClickListener {
            val messageText = binding.typedMessage.text.toString()
            if (messageText.isEmpty()) {
                binding.typedMessage.error = "Type message to send"
            } else {
                sendMessage(messageText)
            }
        }
    }

    private fun sendMessage(messageText: String) {
        binding.typedMessage.setText("")
        val date = Date()
        val messageId = Firebase.database.reference.push().key
        val messageModelObject = MessageModel(senderUid, messageText, date.time, senderName, messageId, false)
        senderRoom?.let { sr ->
            messageId?.let { msgId ->
                Firebase.database.reference.child("Chat").child(sr).child("Messages").child(msgId)
                    .setValue(messageModelObject)
                    .addOnSuccessListener {
                        receiverRoom?.let { rr ->
                            Firebase.database.reference.child("Chat").child(rr).child("Messages").child(msgId)
                                .setValue(messageModelObject)
                                .addOnSuccessListener{}
                                .addOnFailureListener {}
                        }
                    }
                    .addOnFailureListener {}
            }
        }
    }

    private fun setupAttachmentButton() {
        binding.sendImageBtn.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .createIntent { intent ->
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    startActivityForResult(intent, 45)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 45 && resultCode == RESULT_OK && data?.data != null) {
            Log.d("ClientChatDebug", "onActivityResult: uri found and image selected ${data.data}")
            val selectedImage = data.data

            // Upload image to Firebase
            uploadImage(selectedImage)

        } else {
            Log.d("ClientChatDebug", "onActivityResult: uri not found or no image selected ${data?.data}")
        }
    }

    private fun uploadImage(selectedImage: Uri?) {
        val reference = Firebase.storage.reference.child("Chats").child(Calendar.getInstance().timeInMillis.toString())

        dialog?.show()

        // Handler to dismiss the dialog after 1 minute
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            dialog?.dismiss()
            Toast.makeText(this@ClientChatActivity, "Uploading image is taking too long, please try again.", Toast.LENGTH_SHORT).show()
        }

        // Start the handler
        handler.postDelayed(runnable, 60000)

        // Upload the image
        selectedImage?.let { reference.putFile(it).addOnCompleteListener {
            // Remove the handler as the upload completed
            handler.removeCallbacks(runnable)

            if (it.isSuccessful) {
                dialog?.dismiss()
                reference.downloadUrl.addOnSuccessListener { uri ->
                    sendMessageWithImage(uri)
                }
            } else {
                // Handle upload failure if needed
                Toast.makeText(this@ClientChatActivity, "Upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        }}
    }

    private fun sendMessageWithImage(uri: Uri) {
        binding.typedMessage.setText("")
        val filePath = uri.toString()
        val date = Date()
        val messageId = Firebase.database.reference.push().key
        val messageText = binding.typedMessage.text.toString()
        val messageModelObject = MessageModel(senderUid, messageText, date.time, senderName, messageId, false)
        messageModelObject.message = "photo"
        messageModelObject.imageUri = filePath
        senderRoom?.let { sr ->
            messageId?.let { msgId ->
                Firebase.database.reference.child("Chat").child(sr).child("Messages").child(msgId)
                    .setValue(messageModelObject)
                    .addOnSuccessListener {
                        receiverRoom?.let { rr ->
                            Firebase.database.reference.child("Chat").child(rr).child("Messages").child(msgId)
                                .setValue(messageModelObject)
                                .addOnSuccessListener {  }
                                .addOnFailureListener {  }
                        }
                    }
                    .addOnFailureListener {  }
            }
        }
    }

    private fun setupTypingStatus() {
        val handler = Handler(Looper.getMainLooper())
        binding.typedMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                senderUid?.let { Firebase.database.reference.child("ChatUser").child(it)
                    .child("status").setValue("typing...")
                }

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                senderUid?.let { Firebase.database.reference.child("ChatUser").child(it)
                    .child("status").setValue("Online")
                }
            }
        })
    }

    private fun markMessageAsSeen(messageId: String?) {
        if (messageId == null || isActivityVisible != true) return

        // Update the seen status in both sender and receiver rooms
        val updates = HashMap<String, Any>()
        updates["seen"] = true

        senderRoom?.let { Firebase.database.reference.child("Chat").child(it)
            .child("Messages").child(messageId).updateChildren(updates)
        }


        receiverRoom?.let { Firebase.database.reference.child("Chat").child(it)
            .child("Messages").child(messageId).updateChildren(updates)
        }
    }

    private fun getChatsData() {
        senderRoom?.let {
            Firebase.database.reference.child("Chat").child(it)
            .child("Messages")
            .orderByChild("timestamp")
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (msgSnapshot in snapshot.children) {
                            val lastMsg = msgSnapshot.child("message").getValue(String::class.java) ?: ""
                            val lastMsgTime = msgSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                            getUnseenMsgCount(lastMsg, lastMsgTime)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClientChatDebug", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun getUnseenMsgCount(lastMsg: String, lastMsgTime: Long) {
        senderRoom?.let { Firebase.database.reference.child("Chat").child(it)
            .child("Messages")
            .orderByChild("seen")
            .equalTo(false)  // Only retrieve messages where seen == false
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var unseenMsgCount = 0

                    for (msgSnapshot in snapshot.children) {
                        // Check if the message was sent by the receiver
                        val senderId = msgSnapshot.child("senderId").getValue(String::class.java)
                        if (senderId == receiverUid) {
                            unseenMsgCount++
                        }
                    }

                    // Update last message along with the unseen message count
                    updateLastMsg(lastMsg, lastMsgTime, unseenMsgCount)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClientChatDebug", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun updateLastMsg(lastMsg: String?, lastMsgTime: Long?, unseenCount: Int?) {
        if (lastMsg == null || lastMsgTime == null || unseenCount == null) {
            Log.d("ClientChatDebug", "Invalid data for updating last message")
            return
        }

        val updates = mapOf(
            "lastMsg" to lastMsg,
            "lastMsgTime" to lastMsgTime,
            "adminNewMsgCount" to unseenCount
        )

        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it)
            .updateChildren(updates)
            .addOnSuccessListener {
                Log.d("ClientChatDebug", "Last message updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ClientChatDebug", "Error updating last message: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Offline"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }

    private fun setupKeyboardVisibilityListener() {
        // Add a global layout listener to detect keyboard visibility changes
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)

            val screenHeight: Float = binding.root.rootView.height.toFloat()
            val toolbarHeight: Float = binding.toolbar.height.toFloat()
            val keyboardHeight = screenHeight - r.bottom

            // Calculate the maximum translation for the RecyclerView
            val availableSpaceBelowToolbar = screenHeight - toolbarHeight - binding.messageCardView.height

            if (keyboardHeight > screenHeight * 0.15) { // If keyboard is visible
                // Translate the message card view up by the keyboard height
                binding.messageCardView.animate().translationY(-keyboardHeight).setDuration(200).start()
                // Translate the RecyclerView up but not above the toolbar
                binding.rcv.animate().translationY(-minOf(keyboardHeight, availableSpaceBelowToolbar)).setDuration(200).start()
            } else { // If keyboard is hidden
                // Reset the translation of the message card view
                binding.messageCardView.animate().translationY(0.0f).setDuration(200).start()
                // Reset the translation of the RecyclerView
                binding.rcv.animate().translationY(0.0f).setDuration(200).start()
            }
        }
    }
}