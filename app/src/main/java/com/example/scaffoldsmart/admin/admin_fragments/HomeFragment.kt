package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.ClientActivity
import com.example.scaffoldsmart.admin.InventoryActivity
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_adapters.ScafoldRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.databinding.FragmentHomeBinding
import com.example.scaffoldsmart.admin.admin_models.ScafoldInfoModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    private var name: String = ""
    private var email: String = ""
    private var company: String = ""
    private var address: String = ""
    private var phone: String = ""
    private var infoList = ArrayList<ScafoldInfoModel>()
    private lateinit var adapter: ScafoldRcvAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeInfoList()
        storeAdminData()
        retrieveAdminData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        setAdminData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inventoryBtn.setOnClickListener {
            startActivity(Intent(context, InventoryActivity::class.java))
        }

        binding.clientBtn.setOnClickListener {
            startActivity(Intent(context, ClientActivity::class.java))
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ScafoldRcvAdapter(requireActivity(), infoList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun storeAdminData() {
        name = "Sallar Ahmed"
        email = "sallarmirza77@gmail.com"
        company = "Sallar Rental Ltd"
        address = "Islamabad"
        phone = "03125351971"

        val userId = Firebase.auth.currentUser?.uid

        if (userId != null) {
            // Generate a new Firebase key
            val adminRef = Firebase.database.reference.child("Admin").child(userId)
            val adminId = adminRef.push().key

            if (adminId != null) {
                val admin = AdminModel(adminId, name, email, company, address, phone)

                // Store admin data in Firebase Database
                adminRef.setValue(admin)
                    .addOnSuccessListener {
                        Log.d("HomeFragDebug", "Admin data stored successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("HomeFragDebug", "Failed to add Admin Data: ${exception.localizedMessage}")
                    }
            } else {
                Log.d("HomeFragDebug", "Failed to generate Admin ID")
            }
        } else {
            Log.d("HomeFragDebug", "Failed to retrieve current user ID")
        }

    }

    private fun retrieveAdminData() {
        // retrieve admin data from Firebase Database
        Firebase.database.reference.child("Admin").child(Firebase.auth.currentUser!!.uid)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val admin = snapshot.getValue<AdminModel>()
                        name = admin!!.name
                        email = admin.email
                        company = admin.company
                        address = admin.address
                        phone = admin.phone
                        Log.d("HomeFragDebug", "Admin ID: ${admin.id}")
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                }
            )
    }

    private fun setAdminData() {
        binding.welcomeTxt.text = buildString {
            append("Welcome, ")
            append(name)
        }
    }

    private fun initializeInfoList() {
        infoList.add(ScafoldInfoModel("Hasan", "300 Pipes and 200 Joints", "returned"))
        infoList.add(ScafoldInfoModel("Fatima", "300 Pipes and 200 Joints", "overdue"))
        infoList.add(ScafoldInfoModel("Ali", "300 Pipes and 200 Joints", "ongoing"))
        infoList.add(ScafoldInfoModel("Laiba", "300 Pipes and 200 Joints", "returned"))
        infoList.add(ScafoldInfoModel("Danish", "300 Pipes and 200 Joints", "overdue"))
        infoList.add(ScafoldInfoModel("Rabia", "300 Pipes and 200 Joints", "ongoing"))
        infoList.add(ScafoldInfoModel("Haider", "300 Pipes and 200 Joints", "returned"))
    }

}