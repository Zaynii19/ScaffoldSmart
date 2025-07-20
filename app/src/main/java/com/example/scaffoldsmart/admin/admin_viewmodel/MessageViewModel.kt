package com.example.scaffoldsmart.admin.admin_viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scaffoldsmart.admin.admin_models.MessageModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class MessageViewModel(private val senderRoom: String) : ViewModel() {
    private var messageLiveData = MutableLiveData<List<MessageModel>?>()

    fun retrieveMessage() {
        Firebase.database.reference.child("Chat").child(senderRoom)
            .child("Messages")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val msgList = mutableListOf<MessageModel>()
                    for (child in snapshot.children) {
                        val msgItem = child.getValue(MessageModel::class.java)
                        if (msgItem != null) {
                            msgList.add(msgItem)
                        }
                    }
                    // Update the LiveData with the complete list at once
                    messageLiveData.value = msgList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MsgVMDebug", "Failed to retrieve chat", error.toException())
                }
            })
    }

    // Use from chat fragment to listen to LiveData
    fun observeMessageLiveData(): MutableLiveData<List<MessageModel>?> {
        return messageLiveData
    }
}
