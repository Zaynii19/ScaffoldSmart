package com.example.scaffoldsmart.admin.admin_bottomsheets

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.RequestRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.client.client_adapters.RentalDetailRcvAdapter
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.example.scaffoldsmart.databinding.ShowRentalReqBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.example.scaffoldsmart.util.SmartContract
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database

class ShowRentalReq : BottomSheetDialogFragment(), RequestRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        ShowRentalReqBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: RequestRcvAdapter
    private lateinit var reqList: ArrayList<RentalModel>
    private lateinit var viewModel: RentalViewModel
    private lateinit var viewModelA: AdminViewModel
    private lateinit var onesignal: OnesignalService
    private var smartContract: SmartContract? = null
    private var adminObj: AdminModel? = null
    private lateinit var dialogRcvAdapter: RentalDetailRcvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onesignal = OnesignalService(requireActivity())
        smartContract = SmartContract()

        viewModel = ViewModelProvider(requireActivity())[RentalViewModel::class.java]
        viewModel.retrieveRentalReq()

        viewModelA = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModelA.retrieveAdminData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeAdminLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setRcv() {
        // Retrieve the list of rental requests from arguments safely
        arguments?.let {
            reqList = it.getSerializable(ARG_LIST) as ArrayList<RentalModel>
        }
        // Set up the RecyclerView only if reqList is not null
        binding.rcv.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapter = RequestRcvAdapter(requireActivity(), reqList, this)  // Pass the whole list to the adapter
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
        binding.rcv.isNestedScrollingEnabled = false

    }

    private fun observeAdminLiveData() {
        viewModelA.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            if (admin != null) {
                adminObj = admin
            }
        }
    }

    override fun onReqUpdateListener(currentReq: RentalModel) {
        showReqDetailsDialog(currentReq)
    }

    @SuppressLint("SetTextI18n")
    private fun showReqDetailsDialog(currentReq: RentalModel) {
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.rentals_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(requireActivity())
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

        setDialogRcv(binder, currentReq.items)

        builder.setView(customDialog)
            .setTitle("Rental Request Details")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .setPositiveButton("Approve") { dialog, _ ->
                approveRentalReq(currentReq)
                notifyClient(currentReq, true)
                val contractView = layoutInflater.inflate(R.layout.contract_item, null) as NestedScrollView
                smartContract?.createScaffoldingContractPdf(
                    requireActivity(), true, adminObj?.name, adminObj?.company, adminObj?.email, adminObj?.phone, adminObj?.address,
                    currentReq.clientName, currentReq.clientPhone, currentReq.clientEmail, currentReq.clientCnic, currentReq.clientAddress,
                    currentReq.rentalAddress, currentReq.startDuration, currentReq.endDuration, currentReq.items, contractView
                )
                dialog.dismiss()
            }.setNegativeButton("Reject"){ dialog, _ ->
                delRentalReq(currentReq)
                notifyClient(currentReq, false)
                dialog.dismiss()
            }.setOnDismissListener {
                // Dismiss the dialog when it's closed
                dismiss()
            }.create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
                // Set button color
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
    }

    private fun setDialogRcv(binder: RentalsDetailsDialogBinding, items: ArrayList<RentalItem>?) {
        binder.rcv.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        // Provide empty list if items is null
        dialogRcvAdapter = RentalDetailRcvAdapter(requireActivity(), items ?: ArrayList())
        binder.rcv.adapter = dialogRcvAdapter
        binder.rcv.setHasFixedSize(true)
    }

    private fun approveRentalReq(currentReq: RentalModel) {
        if (!isAdded) return // Return true if the fragment is currently added to its activity.

        currentReq.rentalId?.let {
            Firebase.database.reference.child("Rentals")
                .child(it)
                .updateChildren(mapOf("status" to "approved"))
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Request Approved", Toast.LENGTH_SHORT).show()
                        reqList.remove(currentReq)
                        viewModel.retrieveRentalReq()
                        adapter.updateList(reqList)
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to approve request", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun delRentalReq(currentReq: RentalModel) {
        if (!isAdded) return

        currentReq.rentalId?.let {
            Firebase.database.reference.child("Rentals")
                .child(it)
                .removeValue()
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Request rejected from ${currentReq.clientName}", Toast.LENGTH_SHORT).show()
                        reqList.remove(currentReq)
                        adapter.updateList(reqList)
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to reject request", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun notifyClient(currentReq: RentalModel, isApproved: Boolean) {
        if (isApproved) {
            val title = "Rental Request Alert"
            val message = "Your Rental Request Has Been Approved. Click to view rental details."
            val email = currentReq.clientEmail ?: run {
                Toast.makeText(context, "Client email is null", Toast.LENGTH_SHORT).show()
                return  // exit if email is null
            }
            val externalId = listOf(email)  // Now definitely List<String>
            onesignal.sendNotiByOneSignalToExternalId(title, message, externalId)
        } else {
            val title = "Rental Request Alert"
            val message = "Your Rental Request Has Been Rejected."
            val email = currentReq.clientEmail ?: run {
                Toast.makeText(context, "Client email is null", Toast.LENGTH_SHORT).show()
                return  // exit if email is null
            }
            val externalId = listOf(email)  // Now definitely List<String>
            onesignal.sendNotiByOneSignalToExternalId(title, message, externalId)
        }
    }

    companion object {
        private const val ARG_LIST = "arg_list"

        // Modify the function to accept a list of RentalReqModel
        fun newInstance(reqList: List<RentalModel>): ShowRentalReq {
            val fragment = ShowRentalReq()

            // Pass the list as Serializable
            val args = Bundle()
            args.putSerializable(ARG_LIST, ArrayList(reqList))  // Make sure to convert it to ArrayList
            fragment.arguments = args

            return fragment
        }
    }
}