package com.example.scaffoldsmart.client.client_fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_adapters.ClientInventoryRcvAdapter
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.FragmentClientInventoryBinding
import com.example.scaffoldsmart.util.Encryption
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ClientInventoryFragment : Fragment() {
    private val binding by lazy {
        FragmentClientInventoryBinding.inflate(layoutInflater)
    }
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var adapter: ClientInventoryRcvAdapter
    private lateinit var viewModel: InventoryViewModel
    private lateinit var viewModel2: ClientViewModel
    private lateinit var clientObj: ClientModel
    private var clientID: String = ""
    private var clientName: String = ""
    private var clientEmail: String = ""
    private var clientPhone: String = ""
    private var clientCnic: String = ""
    private var currentDecryptedPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()

        viewModel2 = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel2.retrieveClientData()

        //clientObj = ClientModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeInventoryLiveData()
        observeClientLiveData()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, ClientSettingActivity::class.java))
        }

        binding.rentalRequestBtn.setOnClickListener {
            if (clientCnic.isNullOrEmpty() && clientPhone.isNullOrEmpty()) {
                showVerificationDialog()
            } else {
                showReqBottomSheet()
            }
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.item_color)
        binding.swipeRefresh.setOnRefreshListener {
            // Refresh data here
            viewModel.retrieveInventory() // Call your method to fetch data again
            observeInventoryLiveData()
            binding.swipeRefresh.isRefreshing = false // Hide the refresh animation after data is fetched
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClientInventoryRcvAdapter(requireActivity(), itemList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeInventoryLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeInventoryLiveData().observe(viewLifecycleOwner) { items ->
            binding.loading.visibility = View.GONE
            itemList.clear()
            items?.let {
                itemList.addAll(it)
                Log.d("ClientInventoryDebug", "observeInventoryLiveData: ${it.size}")
            }
            adapter.updateList(itemList) // Notify the adapter about the changes
        }
    }

    private fun observeClientLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel2.observeClientLiveData().observe(viewLifecycleOwner) { client ->
            binding.loading.visibility = View.GONE
            if (client != null) {
                clientID = client.id
                clientName = client.name
                clientEmail = client.email
                clientPhone = client.phone
                clientCnic = client.cnic

                currentDecryptedPassword = Encryption.decrypt(client.pass)

                clientObj = client //Passing whole client to the obj
            }
        }
    }

    private fun showReqBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = RentalReqFragment.newInstance(object : RentalReqFragment.OnSendReqListener {
            override fun onReqSendUpdated(
                rentalAddress: String,
                startDuration: String,
                endDuration: String,
                pipes: String,
                pipesLength: String,
                joints: String,
                wench: String,
                pumps: String,
                motors: String,
                generators: String,
                wheel: String,
                rent: Int
            ) {
                val onesignal = OnesignalService(requireActivity())
                onesignal.sendReqNotiByOneSignalToSegment(clientID, clientName, rentalAddress, clientEmail, clientPhone, clientCnic, startDuration, endDuration, pipes, pipesLength, joints, wench, pumps, motors, generators, wheel, rent)
            }
        }, clientObj)
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "RentalReq")
    }

    private fun showVerificationDialog() {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setTitle("Account Verification")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .setMessage("Your account is not verified. Please verify it to send rental request.")
            .setPositiveButton("Verify") { _, _ -> showVerifyBottomSheet() }
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
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }
    }

    private fun showVerifyBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = ClientUpdateFragment.newInstance(object : ClientUpdateFragment.OnClientUpdatedListener {
            override fun onClientUpdated(name: String, email: String, pass: String, cnic: String, phone: String, address: String) {}

            override fun onClientVerified(cnic: String, phone: String, address: String) {
                ClientSettingActivity.verifyClient(cnic, address, phone, currentDecryptedPassword, requireActivity())
            }
        }, true)
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "Client")
    }

    /*private fun sendReqNotificationByFunction(token: String, title: String, body: String) {
        val url = "https://your-function-endpoint" // Replace with your function's endpoint

        val jsonObject = JSONObject()
        jsonObject.put("token", token)
        jsonObject.put("title", title)
        jsonObject.put("body", body)

        val requestBody = jsonObject.toString()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle errors, e.g., log the error or display a user message
                Log.e("FCM Notification", "Error sending notification: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("FCM Notification", "Notification sent successfully")
                } else {
                    Log.e("FCM Notification", "Error sending notification: ${response.code}")
                }
            }
        })
    }*/

    companion object {
        var notificationId = ""
    }
}