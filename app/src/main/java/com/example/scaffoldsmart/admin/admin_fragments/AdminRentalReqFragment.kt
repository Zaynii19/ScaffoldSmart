package com.example.scaffoldsmart.admin.admin_fragments

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.RequestRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.databinding.FragmentAdminRentalReqBinding
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.example.scaffoldsmart.util.SmartContract
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database

class AdminRentalReqFragment : BottomSheetDialogFragment(), RequestRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        FragmentAdminRentalReqBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: RequestRcvAdapter
    private lateinit var reqList: ArrayList<RentalModel>
    private lateinit var viewModel: RentalViewModel
    private lateinit var viewModelA: AdminViewModel
    private lateinit var onesignal: OnesignalService
    private var smartContract: SmartContract? = null
    private var adminName: String = ""
    private var adminEmail: String = ""
    private var adminCompany: String = ""
    private var adminAddress: String = ""
    private var adminPhone: String = ""
    private var isApproved: Boolean = false

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
                adminName = admin.name
                adminEmail = admin.email
                adminCompany = admin.company
                adminPhone = admin.phone
                adminAddress = admin.address
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
        binder.address.text = currentReq.rentalAddress
        binder.phoneNum.text = currentReq.clientPhone
        binder.email.text = currentReq.clientEmail
        binder.cnic.text = currentReq.clientCnic
        binder.rentalDurationFrom.text = currentReq.startDuration
        binder.rentalDurationTo.text = currentReq.endDuration
        binder.rent.text = currentReq.rent.toString()
        binder.rentalAddress.text = currentReq.rentalAddress
        setViewVisibilityAndText(binder.pipes, currentReq.pipes, binder.entry8)
        setViewVisibilityAndText(binder.pipesLength, currentReq.pipesLength, binder.entry9)
        setViewVisibilityAndText(binder.joints, currentReq.joints, binder.entry10)
        setViewVisibilityAndText(binder.wench, currentReq.wench, binder.entry11)
        setViewVisibilityAndText(binder.slugPumps, currentReq.pumps, binder.entry12)
        setViewVisibilityAndText(binder.motors, currentReq.motors, binder.entry13)
        setViewVisibilityAndText(binder.generators, currentReq.generators, binder.entry14)
        setViewVisibilityAndText(binder.wheel, currentReq.wheel, binder.entry15)

        builder.setView(customDialog)
            .setTitle("Rental Request Details")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .setPositiveButton("Approve") { dialog, _ ->
                isApproved = true
                approveRentalReq(currentReq)
                notifyClient(currentReq, isApproved)
                smartContract!!.createScaffoldingContractPdf(
                    requireActivity(), isApproved, adminName, adminCompany, adminEmail, adminPhone, adminAddress, currentReq.clientName, currentReq.clientPhone,
                    currentReq.clientEmail, currentReq.clientCnic, currentReq.clientAddress, currentReq.rentalAddress, currentReq.startDuration, currentReq.endDuration,
                    currentReq.pipes.toString(), currentReq.pipesLength.toString(), currentReq.joints.toString(), currentReq.wench.toString(),
                    currentReq.motors.toString(), currentReq.pumps.toString(), currentReq.generators.toString(), currentReq.wheel.toString()
                )
                dialog.dismiss()
            }.setNegativeButton("Reject"){ dialog, _ ->
                isApproved = false
                delRentalReq(currentReq)
                notifyClient(currentReq, isApproved)
                dialog.dismiss()
            }.setOnDismissListener {
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

    private fun setViewVisibilityAndText(view: TextView, text: Int, entry: ConstraintLayout) {
        if (text.toString().isNotEmpty()) {
            view.text = "$text"
        } else {
            entry.visibility = View.GONE
        }
    }

    private fun approveRentalReq(currentReq: RentalModel) {
        if (!isAdded) return // Return true if the fragment is currently added to its activity.

        Firebase.database.reference.child("Rentals")
            .child(currentReq.rentalId)
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

    private fun delRentalReq(currentReq: RentalModel) {
        if (!isAdded) return

        Firebase.database.reference.child("Rentals")
            .child(currentReq.rentalId)
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

    companion object {
        private const val ARG_LIST = "arg_list"

        // Modify the function to accept a list of RentalReqModel
        fun newInstance(reqList: List<RentalModel>): AdminRentalReqFragment {
            val fragment = AdminRentalReqFragment()

            // Pass the list as Serializable
            val args = Bundle()
            args.putSerializable(ARG_LIST, ArrayList(reqList))  // Make sure to convert it to ArrayList
            fragment.arguments = args

            return fragment
        }
    }
}