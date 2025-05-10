package com.example.scaffoldsmart.client.client_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.ChatViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_adapters.ClientScaffoldRcvAdapter
import com.example.scaffoldsmart.client.client_models.ClientScaffoldInfoModel
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.FragmentClientHomeBinding
import com.example.scaffoldsmart.util.DateFormater
import com.example.scaffoldsmart.util.OnesignalService
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.database.database

class ClientHomeFragment : Fragment() {
    private val binding by lazy {
        FragmentClientHomeBinding.inflate(layoutInflater)
    }
    private var name: String = ""
    private var email: String = ""
    private var role: String = ""
    private var clientID: String = ""
    private var infoList = ArrayList<ClientScaffoldInfoModel>()
    private var dueRequests = ArrayList<RentalModel>()
    private lateinit var adapter: ClientScaffoldRcvAdapter
    private lateinit var viewModel: ClientViewModel
    private lateinit var onesignal: OnesignalService
    private lateinit var rentViewModel: RentalViewModel
    private lateinit var chatPreferences: SharedPreferences
    private lateinit var viewModelC: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatPreferences = requireActivity().getSharedPreferences("CHATCLIENT", MODE_PRIVATE)

        onesignal = OnesignalService(requireActivity())
        viewModel = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel.retrieveClientData()

        rentViewModel = ViewModelProvider(this)[RentalViewModel::class.java]
        rentViewModel.retrieveRentalReq()


        viewModelC = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModelC.retrieveChatData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeClientLiveData()
        observeRentalLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, ClientSettingActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.totalPaymentDue.text = buildString {
            append(totalDueRent(dueRequests))
            append(" .Rs")
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClientScaffoldRcvAdapter(requireActivity(), infoList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeClientLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeClientLiveData().observe(viewLifecycleOwner) { client ->
            binding.loading.visibility = View.GONE
            if (client != null) {
                client.id?.let { clientID = it }
                client.name?.let { name = it }
                client.email?.let { email = it }
                client.userType?.let { role = it }

                binding.welcomeTxt.text = buildString {
                    append("Welcome, ")
                    append(name)
                }

                onesignal.oneSignalLogin(email, role)

                chatPreferences.edit { putString("SenderUid", clientID) }
                chatPreferences.edit { putString("SenderName", name) }

                Log.d("ClientHomeFragDebug", "SenderUid: $clientID, SenderName: $name")
            }
        }
    }

    private fun observeRentalLiveData() {
        binding.loading.visibility = View.VISIBLE
        rentViewModel.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            binding.loading.visibility = View.GONE
            if (rentals != null) {
                // Filter rentals where the status is not empty
                val filteredRentals = rentals.filter { it.clientID == clientID && it.status == "approved"}
                populateInfoList(filteredRentals)

                dueRequests = rentals.filter { it.clientID == clientID && it.status?.isEmpty() == true } as ArrayList<RentalModel>
                binding.totalPaymentDue.text = buildString {
                    append(totalDueRent(dueRequests))
                    append(" .Rs")
                }
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
            val durationInMonths = DateFormater.calculateDurationInMonths(rental.startDuration, rental.endDuration)
            updateRentalStatus(rental, status)

            // Create a ScaffoldInfoModel instance and add to infoList
            rental.rent?.let { infoList.add(ClientScaffoldInfoModel(it, durationInMonths, rental.rentStatus)) }
            adapter.updateList(infoList)
        }
    }

    private fun updateRentalStatus(currentRental: RentalModel, newRentStatus: String) {
        if (currentRental.rentStatus == "returned") return // Exit if the status is already returned
        if (currentRental.rentStatus == newRentStatus) return // Exit if the status is already the same
        // Reference to the specific req in Firebase
        currentRental.rentalId?.let { rentalId ->
            val update = mapOf("rentStatus" to newRentStatus)
            Firebase.database.reference.child("Rentals")
                .child(rentalId)
                .updateChildren(update)
                .addOnSuccessListener {}
                .addOnFailureListener {}
        } ?: run {
            Log.e("ClientHomeFragDebug", "Cannot update - rentalId is null")
        }
    }

    private fun totalDueRent(dueRequests: ArrayList<RentalModel>): Int {
        var total = 0
        for (request in dueRequests) {
            request.rent?.let { total += it }
        }
        return total
    }
}