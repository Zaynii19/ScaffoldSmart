package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_adapters.InvoiceRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.databinding.FragmentInvoiceBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class InvoiceFragment : BottomSheetDialogFragment() {
    private val binding by lazy {
        FragmentInvoiceBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: InvoiceRcvAdapter
    private var rentalList = ArrayList<RentalModel>()
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var viewModelR: RentalViewModel
    private lateinit var viewModelA: AdminViewModel
    private lateinit var viewModelI: InventoryViewModel
    private lateinit var adminObj: AdminModel
    private lateinit var invoicePreferences: SharedPreferences
    private lateinit var chatPreferences: SharedPreferences
    private var userType : String? = null
    private var clientUid : String? = null

    // Flags to track if data is loaded
    private var isRentalLoaded = false
    private var isAdminLoaded = false
    private var isInventoryLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        invoicePreferences = requireActivity().getSharedPreferences("INVOICE", MODE_PRIVATE)
        userType = invoicePreferences.getString("USER", null)
        chatPreferences = requireActivity().getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        clientUid = chatPreferences.getString("SenderUid", null)

        adminObj = AdminModel()

        viewModelR = ViewModelProvider(requireActivity())[RentalViewModel::class.java]
        viewModelR.retrieveRentalReq()

        viewModelI = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModelI.retrieveInventory()

        if (userType == "admin") {
            viewModelA = ViewModelProvider(requireActivity())[AdminViewModel::class.java]
            viewModelA.retrieveAdminData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize RecyclerView with empty data
        setRcv()

        if (userType == "admin") {
            observeRentalLiveData()
            observeAdminLiveData()
        } else {
            observeRentalLiveDataForClient()
            fetchingAdminData()
        }

        observeInventoryLiveData()

        return binding.root
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = InvoiceRcvAdapter(requireActivity(), ArrayList(), ArrayList(), AdminModel())
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeRentalLiveData() {
        viewModelR.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            if (rentals != null) {
                rentalList.clear()
                // Filter rentals where the status is not empty
                val filteredRentals = rentals.filter { it.status.isNotEmpty() } as ArrayList<RentalModel>
                rentalList.addAll(filteredRentals)

                isRentalLoaded = true
                checkAllDataLoaded()

                if (rentalList.isEmpty()) {
                    Toast.makeText(requireActivity(), "No Rental Found", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    private fun observeRentalLiveDataForClient() {
        viewModelR.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            if (rentals != null) {
                rentalList.clear()
                // Filter rentals where the status is not empty
                val filteredRentals = rentals.filter { it.status.isNotEmpty() && it.clientID == clientUid} as ArrayList<RentalModel>
                rentalList.addAll(filteredRentals)

                isRentalLoaded = true
                checkAllDataLoaded()

                if (rentalList.isEmpty()) {
                    Toast.makeText(requireActivity(), "No Rental Found", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    private fun observeInventoryLiveData() {
        viewModelI.observeInventoryLiveData().observe(viewLifecycleOwner) { items ->
            itemList.clear()
            items?.let {
                itemList.addAll(it)
                isInventoryLoaded = true
                checkAllDataLoaded()
            }
        }
    }

    private fun observeAdminLiveData() {
        viewModelA.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            if (admin != null) {
                adminObj = admin
                isAdminLoaded = true
                checkAllDataLoaded()
            }
        }
    }

    private fun fetchingAdminData() {
        Firebase.database.reference.child("Admin").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val admin = child.getValue(AdminModel::class.java)
                    if (admin != null) {
                        adminObj = admin
                        isAdminLoaded = true
                        checkAllDataLoaded()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkAllDataLoaded() {
        if (isRentalLoaded && isAdminLoaded && isInventoryLoaded) {
            // All data is loaded, update the adapter
            adapter.updateData(rentalList, itemList, adminObj)
        }
    }
}