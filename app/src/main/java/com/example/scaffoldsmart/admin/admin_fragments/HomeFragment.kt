package com.example.scaffoldsmart.admin.admin_fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.util.Security
import com.example.scaffoldsmart.admin.ClientActivity
import com.example.scaffoldsmart.admin.InventoryActivity
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_adapters.ScaffoldRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.FragmentHomeBinding
import com.example.scaffoldsmart.admin.admin_models.ScafoldInfoModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.example.scaffoldsmart.util.DateFormater
import com.example.scaffoldsmart.util.OnesignalService
import com.example.scaffoldsmart.util.SmartContract
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import androidx.core.content.edit

class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    private lateinit var onesignal: OnesignalService
    private var id: String = ""
    private var name: String = ""
    private var email: String = ""
    private var company: String = ""
    private var address: String = ""
    private var phone: String = ""
    private var userType: String = ""
    private var pass: String = ""
    private var infoList = ArrayList<ScafoldInfoModel>()
    private lateinit var adapter: ScaffoldRcvAdapter
    private var reqList = ArrayList<RentalModel>()
    private var filteredRentals = ArrayList<RentalModel>()
    private lateinit var viewModel: AdminViewModel
    private lateinit var reqViewModel: RentalViewModel
    private var durationInMonths: String = ""
    private var bottomSheetDialog: BottomSheetDialogFragment? = null
    private lateinit var chatPreferences: SharedPreferences
    private var smartContract: SmartContract? = null
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var viewModelI: InventoryViewModel
    private lateinit var dialog: AlertDialog
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onesignal = OnesignalService(requireActivity())
        smartContract = SmartContract()

        chatPreferences = requireActivity().getSharedPreferences("CHATADMIN", MODE_PRIVATE)

        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModel.retrieveAdminData()
        storeAdminData()

        reqViewModel = ViewModelProvider(this)[RentalViewModel::class.java]
        reqViewModel.retrieveRentalReq()

        viewModelI = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModelI.retrieveInventory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeAdminLiveData()
        observeRentalLiveData()
        observeInventoryLiveData()

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

    override fun onResume() {
        super.onResume()
        binding.totalPaymentReceived.text = buildString {
            append(totalPaymentReceived(filteredRentals))
            append(" .Rs")
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ScaffoldRcvAdapter(requireActivity(), infoList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun storeAdminData() {
        val encryptedPassword = Security.encrypt(pass)
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

                onesignal.oneSignalLogin(email, userType)

                chatPreferences.edit { putString("SenderUid", id) }
                chatPreferences.edit { putString("SenderName", name) }
            }
        }
    }

    private fun observeRentalLiveData() {
        binding.loading.visibility = View.VISIBLE
        reqViewModel.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            binding.loading.visibility = View.GONE
            if (rentals != null) {
                // Filter rentals where the status is not empty
                filteredRentals = rentals.filter { it.status.isNotEmpty() } as ArrayList<RentalModel>

                binding.totalPaymentReceived.text = buildString {
                    append(totalPaymentReceived(filteredRentals))
                    append(" .Rs")
                }

                populateInfoList(filteredRentals)

                // Filter rentals where the status is empty
                val filteredRequests = rentals.filter { it.status.isEmpty() }
                populateReqList(filteredRequests)
            }
        }
    }

    private fun observeInventoryLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModelI.observeInventoryLiveData().observe(requireActivity()) { items ->
            binding.loading.visibility = View.GONE
            itemList.clear()
            items?.let {
                itemList.addAll(it)
            }
        }
    }

    private fun populateInfoList(rentals: List<RentalModel>) {
        infoList.clear()
        for (rental in rentals) {

            // Determine the status
            val isOverdue = DateFormater.compareDateWithCurrentDate(rental.endDuration)
            val status = if (isOverdue) {
                "overdue"
            } else {
                "ongoing"
            }

            // Calculate the duration in months
            durationInMonths = DateFormater.calculateDurationInMonths(rental.startDuration, rental.endDuration)

            // Create a ScaffoldInfoModel instance and add to infoList
            infoList.add(ScafoldInfoModel(rental.clientName, durationInMonths, status))
            adapter.updateList(infoList)

            updateRentalStatus(rental, status)
        }
    }

    private fun updateRentalStatus(currentRental: RentalModel, newRentStatus: String) {
        val databaseRef = Firebase.database.reference.child("Rentals")
            .child(currentRental.rentalId) // Reference to the specific rental using rentalId

        val updates = hashMapOf<String, Any>(
            "rentStatus" to newRentStatus
        )

        // Update the item with the new values
        databaseRef.updateChildren(updates)
            .addOnSuccessListener {}
            .addOnFailureListener {}
    }

    private fun populateReqList(filteredRequests: List<RentalModel>) {
        reqList.clear()
        if (filteredRequests.isNotEmpty()) {
            filteredRequests.let {
                reqList.addAll(it)
                Log.d("HomeFragDebug", "observeRentalReqLiveData: ${filteredRequests.size} empty status requests")
                showRequestsDialogSequentially(reqList)
            }
            binding.notiAlert.visibility = View.VISIBLE
        } else {
            binding.notiAlert.visibility = View.GONE // Hide if no empty status requests
            if (bottomSheetDialog != null && bottomSheetDialog!!.isVisible) {
                bottomSheetDialog?.dismiss() // Dismiss if open
            }
            Log.d("HomeFragDebug", "observeRentalReqLiveData: No empty status requests")
        }
    }

    private fun showRequestsDialogSequentially(reqList: List<RentalModel>) {
        if (reqList.isNotEmpty()) {
            showReqDetailsDialog(reqList, 0) // Start with the first request
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showReqDetailsDialog(reqList: List<RentalModel>, index: Int) {
        if (reqList.isEmpty()) return // Exit if the list is empty

        val currentReq = reqList[index]

        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.rentals_details_dialog, null)
        val binder = RentalsDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = currentReq.clientName
        binder.address.text = currentReq.clientAddress
        binder.phoneNum.text = currentReq.clientPhone
        binder.email.text = currentReq.clientEmail
        binder.cnic.text = currentReq.clientCnic
        binder.rent.text = buildString {
            append(currentReq.rent)
            append(" .Rs")
        }
        binder.rentalAddress.text = currentReq.rentalAddress
        binder.rentalDurationFrom.text = currentReq.startDuration
        binder.rentalDurationTo.text = currentReq.endDuration

        // Regular quantities (just numbers)
        setViewVisibilityAndText(binder.pipes, currentReq.pipes, binder.entry8)
        setViewVisibilityAndText(binder.joints, currentReq.joints, binder.entry10)
        setViewVisibilityAndText(binder.wench, currentReq.wench, binder.entry11)
        setViewVisibilityAndText(binder.slugPumps, currentReq.pumps, binder.entry12)
        setViewVisibilityAndText(binder.motors, currentReq.motors, binder.entry13)
        setViewVisibilityAndText(binder.generators, currentReq.generators, binder.entry14)
        setViewVisibilityAndText(binder.wheel, currentReq.wheel, binder.entry15)

        // Special case for pipe length (with "feet" unit)
        if (currentReq.pipesLength != 0) {
            binder.pipesLength.text = buildString {
                append(currentReq.pipesLength)
                append(" feet")
            }
            binder.entry9.visibility = View.VISIBLE
        } else {
            binder.entry9.visibility = View.GONE
        }

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(customDialog)
            .setTitle("Rental Request Details ${index + 1}")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .setPositiveButton("Approve") { dialog, _ ->
                approveRentalReq(currentReq)
                notifyClient(currentReq, true)
                smartContract!!.createScaffoldingContractPdf(
                    requireActivity(), true, name, company, email, phone, address, currentReq.clientName, currentReq.clientPhone,
                    currentReq.clientEmail, currentReq.clientCnic, currentReq.clientAddress, currentReq.rentalAddress, currentReq.startDuration,
                    currentReq.endDuration, currentReq.pipes.toString(), currentReq.pipesLength.toString(), currentReq.joints.toString(),
                    currentReq.wench.toString(), currentReq.motors.toString(), currentReq.pumps.toString(), currentReq.generators.toString(), currentReq.wheel.toString()
                )
                updateInventory(currentReq.pipes, currentReq.joints, currentReq.wench, currentReq.pumps, currentReq.motors, currentReq.generators, currentReq.wheel)
                dialog.dismiss()
            }
            .setNegativeButton("Reject") { dialog, _ ->
                delRentalReq(currentReq)
                notifyClient(currentReq, false)
                dialog.dismiss()
            }
            .setNeutralButton(if (index == reqList.size - 1) "Previous" else "Next") { dialog, _ ->
                dialog.dismiss()
                if (index == reqList.size - 1) {
                    // Traverse backward to the first dialog
                    showReqDetailsDialog(reqList, index - 1)
                } else {
                    // Traverse forward
                    showReqDetailsDialog(reqList, index + 1)
                }
            }
            .create().apply {
                show()
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)

                val neutralButton = getButton(AlertDialog.BUTTON_NEUTRAL)
                if (reqList.size <= 1) { // Hide the "Previous" button if only one request is left
                    neutralButton?.visibility = View.GONE
                } else {
                    neutralButton?.setTextColor(Color.BLUE)
                }
            }
    }

    private fun setViewVisibilityAndText(view: TextView, quantity: Int, entry: ConstraintLayout) {
        if (quantity != 0) {
            view.text = "$quantity"
            entry.visibility = View.VISIBLE
        } else {
            entry.visibility = View.GONE
        }
    }

    private fun approveRentalReq(currentReq: RentalModel) {
        // Reference to the specific req in Firebase
        val databaseRef = Firebase.database.reference.child("Rentals")
            .child(currentReq.rentalId) // Reference to the specific req using reqId

        // Update the status
        val newStatus = "approved"

        // Determine the rent status
        val isOverdue = DateFormater.compareDateWithCurrentDate(currentReq.endDuration)
        val newRentStatus = if (isOverdue) {
            "overdue"
        } else {
            "ongoing"
        }

        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "rentStatus" to newRentStatus
        )

        // Update the item with the new values
        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(requireActivity(), "Request Approved", Toast.LENGTH_SHORT).show()
                // Remove the item from the list
                reqList.remove(currentReq)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireActivity(), "Failed to approve request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun delRentalReq(currentReq: RentalModel) {
        // Reference to the specific req in Firebase
        val databaseRef = Firebase.database.reference.child("Rentals")
            .child(currentReq.rentalId) // Reference to the specific req using reqId
        databaseRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireActivity(), "Request rejected from ${currentReq.clientName}", Toast.LENGTH_SHORT).show()
                // Remove the item from the list
                reqList.remove(currentReq)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireActivity(), "Failed to reject request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun notifyClient(currentReq: RentalModel, isApproved: Boolean) {
        if (isApproved) {
            val title = "Rental Request Alert"
            val message = "Your Rental Request Has Been Approved. Click to view rental details."
            val externalId = listOf(currentReq.clientEmail)
            onesignal.sendNotiByOneSignalToExternalId(title, message, externalId)
        } else {
            val title = "Rental Request Alert"
            val message = "Your Rental Request Has Been Rejected."
            val externalId = listOf(currentReq.clientEmail)
            onesignal.sendNotiByOneSignalToExternalId(title, message, externalId)
        }
    }

    private fun updateInventory(
        pipeQuantity: Int,
        jointsQuantity: Int,
        wenchQuantity: Int,
        pumpsQuantity: Int,
        motorsQuantity: Int,
        generatorsQuantity: Int,
        wheelQuantity: Int
    ) {
        itemList.forEach { item ->
            val lowerName = item.itemName.lowercase()
            val databaseRef = Firebase.database.reference.child("Inventory").child(item.itemId)

            when {
                lowerName.contains("pipe") && pipeQuantity.toString().isNotEmpty() -> {
                    if (item.quantity >= pipeQuantity) {
                        val remaining = (item.quantity - pipeQuantity)
                        databaseRef.updateChildren(mapOf("quantity" to remaining))
                    }
                }
                lowerName.contains("joint") && jointsQuantity.toString().isNotEmpty() -> {
                    if (item.quantity >= jointsQuantity) {
                        val remaining = (item.quantity - jointsQuantity)
                        databaseRef.updateChildren(mapOf("quantity" to remaining))
                    }
                }
                lowerName.contains("wench") && wenchQuantity.toString().isNotEmpty() -> {
                    if (item.quantity >= wenchQuantity) {
                        val remaining = (item.quantity - wenchQuantity)
                        databaseRef.updateChildren(mapOf("quantity" to remaining))
                    }
                }
                lowerName.contains("pump") && pumpsQuantity.toString().isNotEmpty() -> {
                    if (item.quantity >= pumpsQuantity) {
                        val remaining = (item.quantity - pumpsQuantity)
                        databaseRef.updateChildren(mapOf("quantity" to remaining))
                    }
                }
                lowerName.contains("motor") && motorsQuantity.toString().isNotEmpty() -> {
                    if (item.quantity >= motorsQuantity) {
                        val remaining = (item.quantity - motorsQuantity)
                        databaseRef.updateChildren(mapOf("quantity" to remaining))
                    }
                }
                lowerName.contains("generator") && generatorsQuantity.toString().isNotEmpty() -> {
                    if (item.quantity >= generatorsQuantity) {
                        val remaining = (item.quantity - generatorsQuantity)
                        databaseRef.updateChildren(mapOf("quantity" to remaining))
                    }
                }
                lowerName.contains("wheel") && wheelQuantity.toString().isNotEmpty() -> {
                    if (item.quantity >= wheelQuantity) {
                        val remaining = (item.quantity - wheelQuantity)
                        databaseRef.updateChildren(mapOf("quantity" to remaining))
                    }
                }
            }
        }
    }

    private fun showBottomSheet() {
        bottomSheetDialog = AdminRentalReqFragment.newInstance(reqList)
        bottomSheetDialog?.show(requireActivity().supportFragmentManager, "Request")
    }

    private fun totalPaymentReceived(filteredRentals: ArrayList<RentalModel>): Int {
        var total = 0
        for (rental in filteredRentals) {
            total += rental.rent.toInt()
        }
        return total
    }
}