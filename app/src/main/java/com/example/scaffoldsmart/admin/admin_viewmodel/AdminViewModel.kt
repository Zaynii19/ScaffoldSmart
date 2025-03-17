package com.example.scaffoldsmart.admin.admin_viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class AdminViewModel: ViewModel() {
    private var adminLiveData = MutableLiveData<AdminModel?>()

    fun retrieveAdminData() {
        Firebase.database.reference.child("Admin").child(Firebase.auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val admin = snapshot.getValue<AdminModel>()
                    adminLiveData.value = admin

                    Log.d("AdminVMDebug", "Admin data retrieved successfully")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AdminVMDebug", "Failed to retrieve admin data", error.toException())
                }
            })
    }

    //use from home fragment to listen live data
    fun observeAdminLiveData(): MutableLiveData<AdminModel?> {
        return adminLiveData
    }
}