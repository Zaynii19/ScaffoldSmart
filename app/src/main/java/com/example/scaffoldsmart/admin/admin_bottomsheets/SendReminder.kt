package com.example.scaffoldsmart.admin.admin_bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_adapters.ReminderRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.databinding.SendReminderBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.getValue

class SendReminder : BottomSheetDialogFragment() {
    private val binding by lazy {
        SendReminderBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: ReminderRcvAdapter
    private var rentalList = ArrayList<RentalModel>()
    private lateinit var viewModel: RentalViewModel
    private lateinit var oneSignal: OnesignalService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        oneSignal = OnesignalService(requireActivity())

        viewModel = ViewModelProvider(requireActivity())[RentalViewModel::class.java]
        viewModel.retrieveRentalReq()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeRentalLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ReminderRcvAdapter(requireActivity(), rentalList, oneSignal)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeRentalLiveData() {
        viewModel.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            if (rentals != null) {
                rentalList.clear()
                // Filter rentals where the status is not empty
                val filteredRentals = rentals.filter { it.status?.isNotEmpty() == true && it.rentStatus == "ongoing" } as ArrayList<RentalModel>
                rentalList.addAll(filteredRentals)
                if (rentalList.isEmpty()) {
                    Toast.makeText(requireActivity(), "No Ongoing Rental Found", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }
}