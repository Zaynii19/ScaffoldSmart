package com.example.scaffoldsmart.admin.admin_viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scaffoldsmart.admin.admin_models.ChatUserModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ChatViewModel: ViewModel() {
    private var chatLiveData = MutableLiveData<List<ChatUserModel>?>()

    fun retrieveChatData() {
        Firebase.database.reference.child("ChatUser")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatList = mutableListOf<ChatUserModel>()
                    for (child in snapshot.children) {
                        val chatItem = child.getValue(ChatUserModel::class.java)
                        if (chatItem != null) {
                            chatList.add(chatItem)
                        }
                    }
                    // Update the LiveData with the complete list at once
                    chatLiveData.value = chatList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatVMDebug", "Failed to retrieve chat", error.toException())
                }
            })
    }

    //use from chat fragment to listen live data
    fun observeChatLiveData(): MutableLiveData<List<ChatUserModel>?> {
        return chatLiveData
    }
}