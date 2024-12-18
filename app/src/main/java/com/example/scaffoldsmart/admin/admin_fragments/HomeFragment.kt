package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.util.EncryptionUtil
import com.example.scaffoldsmart.admin.ClientActivity
import com.example.scaffoldsmart.admin.InventoryActivity
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_adapters.ScafoldRcvAdapter
import com.example.scaffoldsmart.admin.admin_fragments.AddInventoryFragment.OnInventoryUpdatedListener
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_models.RentalReqModel
import com.example.scaffoldsmart.databinding.FragmentHomeBinding
import com.example.scaffoldsmart.admin.admin_models.ScafoldInfoModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalReqViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private var id: String = ""
    private var name: String = ""
    private var email: String = ""
    private var company: String = ""
    private var address: String = ""
    private var phone: String = ""
    private var userType: String = ""
    private var pass: String = ""
    private var infoList = ArrayList<ScafoldInfoModel>()
    private lateinit var adapter: ScafoldRcvAdapter
    private var reqList = ArrayList<RentalReqModel>()
    private lateinit var viewModel: AdminViewModel
    private lateinit var reqViewModel: RentalReqViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeInfoList()
        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModel.retrieveAdminData()
        storeAdminData()

        reqViewModel = ViewModelProvider(this)[RentalReqViewModel::class.java]
        reqViewModel.retrieveRentalReq()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeAdminLiveData()
        observeRentalReqLiveData()
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

        binding.notiAlert.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ScafoldRcvAdapter(requireActivity(), infoList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun storeAdminData() {

        val encryptedPassword = EncryptionUtil.encrypt(pass)
        userType = "Admin"

        val userId = Firebase.auth.currentUser?.uid

        if (userId != null) {
            // Reference to the Admin node for this user
            val adminRef = Firebase.database.reference.child("Admin").child(userId)

            // Check if data already exists for this admin
            adminRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Admin data already exists
                    Log.d("HomeFragDebug", "Admin data already exists. No need to store again.")
                } else {
                    // Data doesn't exist, proceed to store it
                    id = adminRef.push().key.toString()

                    val admin = AdminModel(userType, id, name, email, encryptedPassword, company, address, phone)

                    // Store admin data in Firebase Database
                    adminRef.setValue(admin)
                        .addOnSuccessListener {
                            Log.d("HomeFragDebug", "Admin data stored successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.d("HomeFragDebug", "Failed to add Admin Data: ${exception.localizedMessage}")
                        }
                }
            }.addOnFailureListener { exception ->
                Log.d("HomeFragDebug", "Failed to check existing Admin Data: ${exception.localizedMessage}")
            }
        } else {
            Log.d("HomeFragDebug", "Failed to retrieve current user ID")
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

    private fun observeAdminLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            binding.loading.visibility = View.GONE
            if (admin != null) {
                userType = admin.userType
                id = admin.id
                name = admin.name
                email = admin.email
                pass  = admin.pass
                company = admin.company
                phone = admin.phone
                address = admin.address

                binding.welcomeTxt.text = buildString {
                    append("Welcome, ")
                    append(name)
                }
            }
        }
    }

    private fun observeRentalReqLiveData() {
        binding.loading.visibility = View.VISIBLE
        reqViewModel.observeRentalReqLiveData().observe(viewLifecycleOwner) { requests ->
            binding.loading.visibility = View.GONE
            if (requests != null) {
                reqList.clear()
                // Filter requests where the status is empty
                val filteredRequests = requests.filter { it.status.isEmpty() }
                reqList.addAll(filteredRequests)
                if (filteredRequests.isNotEmpty()) {
                    binding.notiAlert.visibility = View.VISIBLE
                    Log.d("HomeFragDebug", "observeRentalReqLiveData: ${filteredRequests.size} empty status requests")
                } else {
                    binding.notiAlert.visibility = View.GONE // Hide if no empty status requests
                    Log.d("HomeFragDebug", "observeRentalReqLiveData: No empty status requests")
                }
            }
        }
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = AdminRentalReqFragment.newInstance(reqList)
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "Request")
    }
}