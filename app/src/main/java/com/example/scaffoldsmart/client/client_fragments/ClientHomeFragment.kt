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
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.ChatViewModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.client.ClientChatActivity
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_adapters.ClientScafoldRcvAdapter
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.client.client_models.ClientScafoldInfoModel
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.FragmentClientHomeBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class ClientHomeFragment : Fragment() {
    private val binding by lazy {
        FragmentClientHomeBinding.inflate(layoutInflater)
    }
    private var name: String = ""
    private var email: String = ""
    private var role: String = ""
    private var clientID: String = ""
    private var diffInDays: Long = 0
    private var infoList = ArrayList<ClientScafoldInfoModel>()
    private var dueRequests = ArrayList<RentalModel>()
    private lateinit var adapter: ClientScafoldRcvAdapter
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

        binding.totalPaymentDue.text = buildString {
            append(totalDueRent())
            append(" .Rs")
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClientScafoldRcvAdapter(requireActivity(), infoList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeClientLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeClientLiveData().observe(viewLifecycleOwner) { client ->
            binding.loading.visibility = View.GONE
            if (client != null) {
                clientID = client.id
                name = client.name
                email = client.email
                role = client.userType
                binding.welcomeTxt.text = buildString {
                    append("Welcome, ")
                    append(name)
                }


                onesignal.oneSignalLogin(email, role)

                chatPreferences.edit().putString("SenderUid", clientID).apply()
                chatPreferences.edit().putString("SenderName", name).apply()

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

                dueRequests = rentals.filter { it.clientID == clientID && it.status.isEmpty()} as ArrayList<RentalModel>

            }
        }
    }

    private fun populateInfoList(rentals: List<RentalModel>) {
        infoList.clear()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        for (rental in rentals) {
            try {
                // Convert start and end duration strings to Date type
                val startDate = dateFormat.parse(rental.startDuration)
                val endDate = dateFormat.parse(rental.endDuration)

                if (startDate != null && endDate != null) {
                    // Calculate difference in milliseconds
                    val diffInMillis = endDate.time - startDate.time
                    // Calculate difference in days
                    diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                    // Convert to months (approximately)
                    val durationInMonths = diffInDays / 30  // Assuming 30 days in a month

                    // Create a ScafoldInfoModel instance and add to infoList
                    infoList.add(ClientScafoldInfoModel(rental.rent, "$durationInMonths months", rental.rentStatus))
                    adapter.updateList(infoList)
                }
            } catch (e: ParseException) {
                Log.e("ClientHomeFragDebug", "Date parsing error for rental: ${rental.rentalId} - ${e.message}")
            }
        }
    }

    private fun totalDueRent(): Int {
        var total = 0
        for (request in dueRequests) {
            total += request.rent.toInt()
        }
        return total
    }
}