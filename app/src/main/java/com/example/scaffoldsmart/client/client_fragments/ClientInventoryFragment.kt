package com.example.scaffoldsmart.client.client_fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.InventoryActivity
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.client.CartActivity
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_adapters.ClientInventoryRcvAdapter
import com.example.scaffoldsmart.client.client_bottomsheets.AddToCart
import com.example.scaffoldsmart.client.client_bottomsheets.UpdateClient
import com.example.scaffoldsmart.client.client_bottomsheets.SendRentalReq
import com.example.scaffoldsmart.client.client_models.CartModel
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.FragmentClientInventoryBinding
import com.example.scaffoldsmart.util.Security
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database


class ClientInventoryFragment : Fragment(), ClientInventoryRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        FragmentClientInventoryBinding.inflate(layoutInflater)
    }
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var adapter: ClientInventoryRcvAdapter
    private lateinit var viewModel: InventoryViewModel
    private lateinit var chatPreferences: SharedPreferences
    private var clientId: String? = null

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

        chatPreferences = requireActivity().getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        clientId = chatPreferences.getString("SenderUid", null)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, ClientSettingActivity::class.java))
        }

        binding.viewCartBtn.setOnClickListener {
            startActivity(Intent(context, CartActivity::class.java))
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
        adapter = ClientInventoryRcvAdapter(requireActivity(), itemList, this)
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

    override fun onCartButtonClick(item: InventoryModel) {
        showAddToCartBottomSheet(item)
    }

    private fun showAddToCartBottomSheet(item: InventoryModel) {
        val bottomSheetDialog: BottomSheetDialogFragment = AddToCart.newInstance(object : AddToCart.OnItemAddedListener {
            override fun onItemAdded(itemName: String?, quantity: Int?, price: Int?) {
                storeCartItem(itemName, quantity, price, null)
            }

            override fun onPipesAdded(itemName: String?, quantity: Int?, price: Int?, pipesLength: Int?) {
                storeCartItem(itemName, quantity, price, pipesLength)
            }
        }, item)
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "AddToCart")
    }

    private fun storeCartItem(itemName: String?, quantity: Int?, price: Int?, pipesLength: Int?) {
        // Reference to the inventory in Firebase
        clientId?.let { cId ->
            val databaseRef = Firebase.database.reference.child("Cart").child(cId)
            val newItemRef = databaseRef.push()
            val itemId = newItemRef.key // Get the generated key

            val isPipe = itemName?.lowercase()?.contains("pipe") == true
            val newCartItem = if (isPipe) {
                CartModel(itemId, itemName, quantity, price, pipesLength)
            } else {
                CartModel(itemId, itemName, quantity, price, null)
            }

            if (itemId != null) {
                // Store the new item in Firebase
                newItemRef.setValue(newCartItem)
                    .addOnSuccessListener {
                        Toast.makeText(requireActivity(), "Item added to Cart", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireActivity(), "Failed to add item to cart", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireActivity(), "Failed to generate item ID", Toast.LENGTH_SHORT).show()
            }
        }
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
}