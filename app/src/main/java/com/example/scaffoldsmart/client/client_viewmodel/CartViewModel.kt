package com.example.scaffoldsmart.client.client_viewmodel

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scaffoldsmart.client.client_models.CartModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class CartViewModel(private val clientId: String): ViewModel() {
    private var cartLiveData = MutableLiveData<List<CartModel>?>()

    fun retrieveCartItems() {
        Firebase.database.reference.child("Cart").child(clientId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val itemList = mutableListOf< CartModel>()
                    for (child in snapshot.children) {
                        val cartItem = child.getValue(CartModel::class.java)
                        if (cartItem != null) {
                            itemList.add(cartItem)
                        }
                    }
                    // Update the LiveData with the complete list at once
                    cartLiveData.value = itemList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartVMDebug", "Failed to retrieve cart items", error.toException())
                }
            })
    }

    //use from home fragment to listen live data
    fun observeCartLiveData(): MutableLiveData<List<CartModel>?> {
        return cartLiveData
    }
}