package com.example.scaffoldsmart.admin.admin_viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class InventoryViewModel: ViewModel() {
    private var inventoryLiveData = MutableLiveData<List<InventoryModel>?>()

    fun retrieveInventory() {
        Firebase.database.reference.child("Inventory")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val itemList = mutableListOf<InventoryModel>()
                    for (child in snapshot.children) {
                        val inventoryItem = child.getValue(InventoryModel::class.java)
                        if (inventoryItem != null) {
                            itemList.add(inventoryItem)
                        }
                    }
                    // Update the LiveData with the complete list at once
                    inventoryLiveData.value = itemList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InventoryVMDebug", "Failed to retrieve inventory", error.toException())
                }
            })
    }

    //use from home fragment to listen live data
    fun observeInventoryLiveData(): MutableLiveData<List<InventoryModel>?> {
        return inventoryLiveData
    }
}