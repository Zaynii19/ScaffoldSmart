package com.example.scaffoldsmart.client.client_viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class ClientViewModel : ViewModel() {
    private var clientLiveData = MutableLiveData<ClientModel?>()

    fun retrieveClientData() {
        Firebase.database.reference.child("Client").child(Firebase.auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val client = snapshot.getValue<ClientModel>()
                    if (client != null) {
                        clientLiveData.value = client
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClientHomeDebug", "Failed to retrieve client data", error.toException())
                }
            })
    }

    //use from home fragment to listen live data
    fun observeClientLiveData(): MutableLiveData<ClientModel?> {
        return clientLiveData
    }
}