package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_adapters.RecentChaRcvAdapter
import com.example.scaffoldsmart.admin.admin_adapters.ChatUserRcvAdapter
import com.example.scaffoldsmart.databinding.FragmentChatBinding
import com.example.scaffoldsmart.admin.admin_models.ChatUserModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.ChatViewModel
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ChatFragment : Fragment() {
    private val binding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }
    private lateinit var adapterC: RecentChaRcvAdapter
    private lateinit var adapterU: ChatUserRcvAdapter
    private var profilesList = ArrayList<ChatUserModel>()
    private var recentChatList = ArrayList<ChatUserModel>()
    private var chatClientList = ArrayList<ChatUserModel>()
    private lateinit var viewModelA: AdminViewModel
    private lateinit var viewModel: ChatViewModel
    private var auth: FirebaseAuth? = null
    private var senderUid: String? = null
    private var senderName: String? = null
    private lateinit var chatPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatPreferences = requireActivity().getSharedPreferences("CHATADMIN", MODE_PRIVATE)
        senderUid = chatPreferences.getString("SenderUid", null)
        senderName = chatPreferences.getString("SenderName", null)

        // Initialize Firebase and UI components
        auth = FirebaseAuth.getInstance()
        profilesList = ArrayList()
        recentChatList = ArrayList()
        chatClientList = ArrayList()

        viewModelA = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModelA.retrieveAdminData()

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModel.retrieveChatData()

        fetchingOtherUsersProfiles()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setChatUserRcv()
        setRecentChatRcv()
        observeChatLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }

        setSearchView()
    }

    private fun setChatUserRcv() {
        val layoutManagerU = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.usersRcv.layoutManager = layoutManagerU
        adapterU = ChatUserRcvAdapter(requireActivity(), profilesList)
        binding.usersRcv.adapter = adapterU
        binding.usersRcv.setHasFixedSize(true)
    }

    private fun setRecentChatRcv() {
        val layoutManagerC = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.chatRCV.layoutManager = layoutManagerC
        adapterC = RecentChaRcvAdapter(requireActivity(), recentChatList)
        binding.chatRCV.adapter = adapterC
        binding.chatRCV.setHasFixedSize(true)
    }

    private fun setSearchView() {
        // Change text color to white of search view
        binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        // Get app color from colors.xml
        val appColor = ContextCompat.getColor(requireActivity(), R.color.item_color)

        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)?.setColorFilter(appColor)
        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.setColorFilter(appColor)

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val searchText = query?.lowercase() ?: ""
                performSearch(searchText)
                return  true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText?.lowercase() ?: ""
                performSearch(searchText)
                return true
            }
        })
    }

    private fun performSearch(searchText: String) {
        // Filter the list based on the search text (case-insensitive)
        val filteredListU = profilesList.filter { item ->
            item.userName!!.lowercase().contains(searchText.lowercase())
        }
        // Update the adapter with the filtered list
        adapterU.updateList(filteredListU as ArrayList<ChatUserModel>)

        // Filter the list based on the search text (case-insensitive)
        val filteredListC = recentChatList.filter { item ->
            item.userName!!.lowercase().contains(searchText.lowercase())
        }
        // Update the adapter with the filtered list
        adapterC.updateList(filteredListC as ArrayList<ChatUserModel>)

        // Determine visibility based on filtered results using a single conditional statement
        val isVisible = filteredListU.isNotEmpty() || filteredListC.isNotEmpty()

        binding.userRcvCard.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.chatRcvCard.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun fetchingOtherUsersProfiles() {
        Firebase.database.reference.child("Client").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                profilesList.clear()
                for (child in snapshot.children) {
                    val user = child.getValue(ClientModel::class.java)
                    if (user != null) {
                        profilesList.add(ChatUserModel(user.id, user.name))
                        storeChatUser(profilesList)
                    }
                }
                getChatsData(profilesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragDebug", "Failed to retrieve others user data", error.toException())
            }
        })
    }

    /*private fun storeChatUser(users: ArrayList<ChatUserModel>) {
        // store client data
        for (user in users) {
            Firebase.database.reference.child("ChatUser").child(user.uid!!).setValue(user)
                .addOnSuccessListener {
                    Log.d("ChatFragDebug", "User data added to Firebase successfully!")
                }
                .addOnFailureListener { error ->
                    Log.e("ChatFragDebug", "Failed to add user data to Firebase: ${error.message}")
                }
        }

        // store Admin data
        val user = ChatUserModel(senderUid, senderName)
        Firebase.database.reference.child("ChatUser").child(senderUid!!).setValue(user)
            .addOnSuccessListener {
                Log.d("ChatFragDebug", "User data added to Firebase successfully!")
            }
            .addOnFailureListener { error ->
                Log.e("ChatFragDebug", "Failed to add user data to Firebase: ${error.message}")
            }
    }*/

    private fun storeChatUser(users: ArrayList<ChatUserModel>) {
        // store client data
        for (user in users) {
            val updates = mapOf(
                "uid" to user.uid,
                "userName" to user.userName,
            )

            Firebase.database.reference.child("ChatUser").child(user.uid!!).updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("ChatFragDebug", "User data added to Firebase successfully!")
                }
                .addOnFailureListener { error ->
                    Log.e("ChatFragDebug", "Failed to add user data to Firebase: ${error.message}")
                }
        }

        // store Admin data
        val updates = mapOf(
            "uid" to senderUid,
            "userName" to senderName,
        )
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(updates)
            .addOnSuccessListener {
                Log.d("ChatFragDebug", "User data added to Firebase successfully!")
            }
            .addOnFailureListener { error ->
                Log.e("ChatFragDebug", "Failed to add user data to Firebase: ${error.message}")
            }
    }

    private fun getChatsData(profilesList: ArrayList<ChatUserModel>) {
        for (profile in profilesList) {
            val receiverUid = profile.uid
            val senderRoom = senderUid + receiverUid
            Firebase.database.reference.child("Chat").child(senderRoom)
                .child("Messages")
                .orderByChild("timestamp")
                .limitToLast(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var lastMsg: String? = null
                        var lastMsgTime: Long? = null

                        if (snapshot.exists()) {
                            for (msgSnapshot in snapshot.children) {
                                lastMsg = msgSnapshot.child("message").getValue(String::class.java) ?: ""
                                lastMsgTime = msgSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            }

                            // Call getUnseenMsgCount with current profile instance
                            getUnseenMsgCount(profile, lastMsg!!, lastMsgTime!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatFragDebug", "Database error: ${error.message}")
                    }
                })
        }
    }

    private fun getUnseenMsgCount(profile: ChatUserModel, lastMsg: String, lastMsgTime: Long) {
        val receiverUid = profile.uid
        val senderRoom = senderUid + receiverUid

        Firebase.database.reference.child("Chat").child(senderRoom)
            .child("Messages")
            .orderByChild("seen")
            .equalTo(false) // Only retrieve messages where seen == false
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
                    updateLastMsg(receiverUid, lastMsg, lastMsgTime, unseenMsgCount)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatFragDebug", "Database error: ${error.message}")
                }
            })
    }

    // Update function remains unchanged but includes logging
    private fun updateLastMsg(receiverUid: String?, lastMsg: String?, lastMsgTime: Long?, unseenCount: Int?) {
        if (receiverUid == null || lastMsg == null || lastMsgTime == null || unseenCount == null) {
            Log.d("ChatFragDebug", "Invalid data for updating last message")
            return
        }

        val updates = mapOf(
            "lastMsg" to lastMsg,
            "lastMsgTime" to lastMsgTime,
            "clientNewMsgCount" to unseenCount
        )

        // Log receiverUid to ensure updates only affect the intended user
        Firebase.database.reference.child("ChatUser").child(receiverUid)
            .updateChildren(updates)
            .addOnSuccessListener {
                Log.d("ChatFragDebug", "Last message updated successfully for $receiverUid")
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragDebug", "Error updating last message for $receiverUid: ${e.message}")
            }
    }

    private fun observeChatLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeChatLiveData().observe(viewLifecycleOwner) { chats ->
            binding.loading.visibility = View.GONE
            chats?.let { chatList ->
                // Clear the lists to avoid duplicates
                chatClientList.clear()
                recentChatList.clear()

                // Filter out the sender's data and populate the lists
                chatList.forEach { chat ->
                    // Skip if the chat belongs to the sender
                    if (chat.uid != senderUid && !chat.uid.isNullOrEmpty()) {
                        chatClientList.add(chat)
                    }

                    // Add to recentChatList if the last message is not empty
                    if (!chat.lastMsg.isNullOrEmpty()) {
                        recentChatList.add(chat)
                    }
                }

                // Update adapters
                adapterU.updateList(chatClientList)
                recentChatList.sortByDescending { it.lastMsgTime }
                adapterC.updateList(recentChatList)

                // Update UI visibility based on the list content
                updateUIVisibility()
            }
        }
    }

    private fun updateUIVisibility() {
        if (chatClientList.isEmpty()) {
            binding.userRcvCard.visibility = View.GONE
            binding.chatRcvCard.visibility = View.GONE
        } else {
            binding.userRcvCard.visibility = View.VISIBLE
            binding.chatRcvCard.visibility = if (recentChatList.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}