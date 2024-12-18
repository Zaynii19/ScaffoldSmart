package com.example.scaffoldsmart.client.client_fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryItemIModel
import com.example.scaffoldsmart.admin.admin_models.RentalClientModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.client.client_adapters.ClientInventoryRcvAdapter
import com.example.scaffoldsmart.client.client_models.RentalReqModel
import com.example.scaffoldsmart.databinding.FragmentClientInventoryBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.database.database
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ClientInventoryFragment : Fragment() {
    private val binding by lazy {
        FragmentClientInventoryBinding.inflate(layoutInflater)
    }
    private var reqList = ArrayList<RentalClientModel>()
    private var itemList = ArrayList<InventoryItemIModel>()
    private lateinit var adapter: ClientInventoryRcvAdapter
    private lateinit var viewModel: InventoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeInventoryLiveData()

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
                onesignal.sendReqNotiByOneSignalToSegment(clientName, rentalAddress, clientEmail, clientPhone, clientCnic, startDuration, endDuration, pipes, pipesLength, joints, wench, pumps, motors, generators, wheel)
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

    private fun storeRentalReq(
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
        // Reference to the inventory in Firebase
        val databaseRef = Firebase.database.reference.child("Rentals")
        val newItemRef = databaseRef.push()
        val rentalId = newItemRef.key // Get the generated key

        if (rentalId != null) {
            // Create new rental request model
            val newReq = RentalReqModel(rentalId, clientName, clientEmail, rentalAddress, clientCnic, clientPhone,
                startDuration, endDuration, pipes, pipesLength, joints, wench, motors, pumps, generators, wheel)

            // Store the new request in Firebase
            newItemRef.setValue(newReq)
                .addOnSuccessListener {
                    Toast.makeText(requireActivity(), "Rental request send successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireActivity(), "Failed to send rental request", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireActivity(), "Failed to generate request ID", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        var notificationId = ""
    }
}