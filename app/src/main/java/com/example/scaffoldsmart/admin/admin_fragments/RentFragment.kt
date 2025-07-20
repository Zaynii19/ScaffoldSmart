package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_adapters.RentalRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.databinding.FragmentRentBinding
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.databinding.RentalsCompletionDialogBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.example.scaffoldsmart.util.SmartContract
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database

class RentFragment : Fragment(), RentalRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        FragmentRentBinding.inflate(layoutInflater)
    }

    private var rentalList = ArrayList<RentalModel>()
    private lateinit var adapter: RentalRcvAdapter
    private lateinit var viewModel: RentalViewModel
    private lateinit var viewModelA: AdminViewModel
    private var smartContract: SmartContract? = null
    private var adminObj: AdminModel? = null
    private lateinit var onesignal: OnesignalService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smartContract = SmartContract()
        onesignal = OnesignalService(requireActivity())

        viewModel = ViewModelProvider(this)[RentalViewModel::class.java]
        viewModel.retrieveRentalReq()

        viewModelA = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModelA.retrieveAdminData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        setSearchView()
        observeRentalLiveData()
        observeAdminLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = RentalRcvAdapter(requireActivity(), rentalList, this@RentFragment)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeRentalLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            binding.loading.visibility = View.GONE
            val filteredRentals = rentals?.filter { it.status?.isNotEmpty() == true } // Get only approved rentals
            rentalList.clear()
            filteredRentals?.let {
                rentalList.addAll(it)
                Log.d("RentFragDebug", "observeRentalReqLiveData: ${rentalList.size} ")
            }
            adapter.updateList(rentalList)
        }
    }

    private fun observeAdminLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModelA.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            binding.loading.visibility = View.GONE
            if (admin != null) {
                adminObj = admin
            }
        }
    }

    private fun setSearchView() {
        // Change text color to white of search view
        binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        // Get app color from colors.xml
        val appColor = ContextCompat.getColor(requireContext(), R.color.item_color)

        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)?.setColorFilter(appColor)
        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.setColorFilter(appColor)

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter the itemList based on the search text (case-insensitive)
                val filteredList = rentalList.filter { item ->
                    item.clientName?.lowercase()?.contains(newText?.lowercase() ?: "") == true
                }

                // Update the adapter with the filtered list
                adapter.updateList(filteredList as ArrayList<RentalModel>)
                return true
            }
        })
    }

    override fun onDownloadButtonClick(rental: RentalModel) {
        val contractView = layoutInflater.inflate(R.layout.contract_item, null) as NestedScrollView
        smartContract?.createScaffoldingContractPdf(
            requireActivity(), false, adminObj?.name, adminObj?.company, adminObj?.email, adminObj?.phone, adminObj?.address,
            rental.clientName, rental.clientPhone, rental.clientEmail, rental.clientCnic, rental.clientAddress, rental.rentalAddress,
            rental.startDuration, rental.endDuration, rental.items, contractView
        )
    }

    override fun onDoneRentalButtonClick(currentRental: RentalModel) {
        showConfirmationDialog(currentRental)
    }

    private fun showConfirmationDialog(currentRental: RentalModel) {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setTitle("Complete Rental")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .setMessage("Do you want to complete this rental from ${currentRental.clientName}?")
            .setPositiveButton("Complete") { _, _ -> updateRentalStatus(currentRental) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            // Set message text color
            findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
            // Set button color
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    private fun updateRentalStatus(currentRental: RentalModel) {
        currentRental.rentalId?.let { rentalId ->
            val newRentStatus = "returned"
            val update = mapOf("rentStatus" to newRentStatus)
            Firebase.database.reference.child("Rentals")
                .child(rentalId)
                .updateChildren(update)
                .addOnSuccessListener {
                    showCompletionDialog()
                    notifyClient(currentRental)
                }
                .addOnFailureListener {}
        } ?: run {
            Log.e("RentFragDebug", "Cannot update - rentalId is null")
        }
    }

    private fun showCompletionDialog() {
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.rentals_completion_dialog, null)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        val binder = RentalsCompletionDialogBinding.bind(customDialog)

        Glide.with(this)
            .load(R.drawable.done_rental)
            .into(binder.completionGif)

        // Create and show the dialog
        val dialog = builder.setView(customDialog)
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .show()

        binder.dashboardBtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun notifyClient(currentReq: RentalModel) {
        val title = "Rental Return Confirmed!"
        val message = "We’ve received your rental. Thank you for choosing us! Hope to see you again soon."
        currentReq.clientEmail?.let { email ->
            val externalId = listOf(email)
            onesignal.sendNotiByOneSignalToExternalId(title, message, externalId)
        } ?: Toast.makeText(requireActivity(), "Skipped notifying - client email is null", Toast.LENGTH_SHORT).show()
    }
}