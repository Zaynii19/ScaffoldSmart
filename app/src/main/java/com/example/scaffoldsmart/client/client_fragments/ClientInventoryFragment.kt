package com.example.scaffoldsmart.client.client_fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.client.client_adapters.ClientInventoryRcvAdapter
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.FragmentClientInventoryBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ClientInventoryFragment : Fragment() {
    private val binding by lazy {
        FragmentClientInventoryBinding.inflate(layoutInflater)
    }
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var adapter: ClientInventoryRcvAdapter
    private lateinit var viewModel: InventoryViewModel
    private lateinit var viewModel2: ClientViewModel
    private var clientID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()

        viewModel2 = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel2.retrieveClientData()
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

        binding.rentalRequestBtn.setOnClickListener {
            showBottomSheet()
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
            }
        }
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = RentalReqFragment.newInstance(object : RentalReqFragment.OnSendReqListener {
            override fun onReqSendUpdated(
                clientName: String,
                rentalAddress: String,
                clientEmail: String,
                clientPhone: String,
                clientCnic: String,
                startDuration: String,
                endDuration: String,
                pipes: String,
                pipesLength: String,
                joints: String,
                wench: String,
                pumps: String,
                motors: String,
                generators: String,
                wheel: String
            ) {
                val onesignal = OnesignalService(requireActivity())
                onesignal.sendReqNotiByOneSignalToSegment(clientID, clientName, rentalAddress, clientEmail, clientPhone, clientCnic, startDuration, endDuration, pipes, pipesLength, joints, wench, pumps, motors, generators, wheel)
            }
        })
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "RentalReq")
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