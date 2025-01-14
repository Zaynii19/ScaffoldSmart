package com.example.scaffoldsmart.admin.admin_viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class RentalViewModel: ViewModel() {
    private var rentalReqLiveData = MutableLiveData<List<RentalModel>?>()

    fun retrieveRentalReq() {
        Firebase.database.reference.child("Rentals")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reqList = mutableListOf<RentalModel>()
                    for (child in snapshot.children) {
                        val req = child.getValue(RentalModel::class.java)
                        if (req != null) {
                            reqList.add(req)
                        }
                    }
                    // Update the LiveData with the complete list at once
                    rentalReqLiveData.value = reqList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InventoryDebug", "Failed to retrieve inventory", error.toException())
                }
            })
    }

    //use from home fragment to listen live data
    fun observeRentalReqLiveData(): MutableLiveData<List<RentalModel>?> {
        return rentalReqLiveData
    }
}