package com.example.scaffoldsmart.admin

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
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.MessageViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.MessageViewModelFactory
import com.example.scaffoldsmart.databinding.ActivityChatBinding
import com.example.scaffoldsmart.util.DateFormater
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private var adapter: MessageRcvAdapter? = null
    private var messages: ArrayList<Any>? = ArrayList() // Hold both Message and DateHeader
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var dialog: ProgressDialog? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private var receiverName: String? = null
    private var senderName: String? = null
    private var isActivityVisible: Boolean? = null
    private lateinit var viewModelA: AdminViewModel
    private lateinit var viewModel: MessageViewModel
    private lateinit var chatPreferences: SharedPreferences
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Handle back button press here
            startActivity(Intent(this@ChatActivity, AdminMainActivity::class.java))
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

        viewModelA = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModelA.retrieveAdminData()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        messages = ArrayList()
        chatPreferences = getSharedPreferences("CHATADMIN", MODE_PRIVATE)
        senderUid = chatPreferences.getString("SenderUid", null)

        dialog?.setMessage("Uploading image...")
        dialog?.setCancelable(false)

        // Animate CardView when keyboard appears or disappears
        setupKeyboardVisibilityListener()

        observeAdminLiveData()
        initializeChatDetails()

        // Create the ViewModel using the factory
        viewModel = ViewModelProvider(this, MessageViewModelFactory(senderRoom!!))[MessageViewModel::class.java]
        viewModel.retrieveMessage()

        observeMsgLiveData()
        setUpPresenceListener()
        setupRecyclerView()
        setupSendButton()
        setupAttachmentButton()
        setupTypingStatus()

        onBackPressedDispatcher.addCallback(this@ChatActivity, onBackPressedCallback)
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        binding.rcv.layoutManager = layoutManager
        adapter = MessageRcvAdapter(this, messages!!, senderRoom!!, receiverRoom!!, senderUid!!)
        binding.rcv.adapter = adapter
        layoutManager.stackFromEnd = true
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeAdminLiveData() {
        viewModelA.observeAdminLiveData().observe(this@ChatActivity) { admin ->
            if (admin != null) {
                senderName = admin.name
            }
        }
    }

    private fun initializeChatDetails() {
        receiverUid = intent.getStringExtra("UID")
        receiverName = intent.getStringExtra("USERNAME")
        binding.userName.text = receiverName

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
    }

    private fun observeMsgLiveData() {
        viewModel.observeMessageLiveData().observe(this@ChatActivity) { msgs ->
            messages!!.clear()
            var previousDate: String? = null
            for (msg in msgs!!) {
                val currentDate = DateFormater.formatDateHeader(msg.timestamp)
                if (currentDate != previousDate) {
                    messages!!.add(DateHeader(currentDate))
                    previousDate = currentDate
                }
                messages!!.add(msg)

                // Mark messages as seen if they are sent by the other user and the activity is visible
                if (msg.senderId == receiverUid && !msg.seen!! && isActivityVisible!!) {
                    markMessageAsSeen(msg.messageId)
                }
            }
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun setUpPresenceListener() {
        Firebase.database.reference.child("ChatUser").child(receiverUid!!)
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
                                binding.status.text = status.toString()
                                binding.status.visibility = View.VISIBLE
                            }
                        } else {
                            binding.status.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivityDebug", "Database error: ${error.message}")
                }
            })
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
        val messageId = database!!.reference.push().key
        val messageModelObject = MessageModel(senderUid, messageText, date.time, senderName, messageId, false)
        database!!.reference.child("Chat").child(senderRoom!!).child("Messages").child(messageId!!)
            .setValue(messageModelObject).addOnSuccessListener {
                database!!.reference.child("Chat").child(receiverRoom!!).child("Messages").child(messageId)
                    .setValue(messageModelObject)
                    .addOnCompleteListener { }
            }
    }

    private fun setupTypingStatus() {
        val handler = Handler(Looper.getMainLooper())
        binding.typedMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Firebase.database.reference.child("ChatUser").child(senderUid!!)
                    .child("status").setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                Firebase.database.reference.child("ChatUser").child(senderUid!!)
                    .child("status").setValue("Online")
            }
        })
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data?.data != null)
            uploadImage(data.data!!)
    }*/

    private fun setupAttachmentButton() {
//        binding.sendImageBtn.setOnClickListener {
//            val intent = Intent()
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.type = "image/*"
//            startActivityForResult(intent, 1)
//        }

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
            Log.d("ChatActivityDebug", "onActivityResult: uri found and image selected ${data.data}")
            val selectedImage = data.data

            // Upload image to Firebase
            uploadImage(selectedImage!!)

        } else {
            Log.d("ChatActivityDebug", "onActivityResult: uri not found or no image selected ${data?.data}")
        }
    }

    private fun uploadImage(selectedImage: Uri) {
        val reference = storage!!.reference.child("Chats").child(Calendar.getInstance().timeInMillis.toString())

        dialog?.show()

        // Handler to dismiss the dialog after 1 minute
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            dialog?.dismiss()
            Toast.makeText(this@ChatActivity, "Uploading image is taking too long, please try again.", Toast.LENGTH_SHORT).show()
        }

        // Start the handler
        handler.postDelayed(runnable, 60000)

        // Upload the image
        reference.putFile(selectedImage).addOnCompleteListener {
            // Remove the handler as the upload completed
            handler.removeCallbacks(runnable)

            if (it.isSuccessful) {
                dialog?.dismiss()
                reference.downloadUrl.addOnSuccessListener { uri ->
                    sendMessageWithImage(uri)
                }
            } else {
                // Handle upload failure if needed
                Toast.makeText(this@ChatActivity, "Upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        }
    }

    private fun sendMessageWithImage(uri: Uri) {
        binding.typedMessage.setText("")
        val filePath = uri.toString()
        val date = Date()
        val messageId = database!!.reference.push().key
        val messageText = binding.typedMessage.text.toString()
        val messageModelObject = MessageModel(senderUid, messageText, date.time, senderName, messageId, false)
        messageModelObject.message = "photo"
        messageModelObject.imageUri = filePath
        database!!.reference.child("Chat").child(senderRoom!!).child("Messages").child(messageId!!)
            .setValue(messageModelObject).addOnSuccessListener {
                database!!.reference.child("Chat").child(receiverRoom!!).child("Messages").child(messageId)
                    .setValue(messageModelObject)
                    .addOnCompleteListener { }
            }
    }

    private fun markMessageAsSeen(messageId: String?) {
        if (messageId == null || !isActivityVisible!!) return

        // Update the seen status in both sender and receiver rooms
        val updates = HashMap<String, Any>()
        updates["seen"] = true

        database!!.reference.child("Chat").child(senderRoom!!)
            .child("Messages").child(messageId).updateChildren(updates)

        database!!.reference.child("Chat").child(receiverRoom!!)
            .child("Messages").child(messageId).updateChildren(updates)
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Offline"
        presenceMap["lastSeen"] = currentTime
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
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