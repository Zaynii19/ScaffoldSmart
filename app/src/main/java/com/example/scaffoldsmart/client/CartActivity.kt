package com.example.scaffoldsmart.client

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.client.client_adapters.CartRcvAdapter
import com.example.scaffoldsmart.client.client_bottomsheets.SendRentalReq
import com.example.scaffoldsmart.client.client_bottomsheets.UpdateClient
import com.example.scaffoldsmart.client.client_models.CartModel
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.client.client_viewmodel.CartViewModel
import com.example.scaffoldsmart.client.client_viewmodel.CartViewModelFactory
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.ActivityCartBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.example.scaffoldsmart.util.Security
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database

class CartActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCartBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: CartRcvAdapter
    private var itemList = ArrayList<CartModel>()
    private lateinit var viewModel: CartViewModel
    private lateinit var chatPreferences: SharedPreferences
    private var senderUid: String? = null
    private lateinit var viewModelC: ClientViewModel
    private var clientObj: ClientModel? = null
    private var currentDecryptedPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        senderUid = chatPreferences.getString("SenderUid", null)

        setStatusBarColor()
        setRcv()

        // Create the ViewModel using the factory
        senderUid?.let { viewModel = ViewModelProvider(this, CartViewModelFactory(it))[CartViewModel::class.java] }
        viewModel.retrieveCartItems()
        observeCartLiveData()

        viewModelC = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModelC.retrieveClientData()
        observeClientLiveData()

        clientObj.let { client ->
            if (client == null || itemList.isEmpty()) {
                // Disable the button if clientObj is null
                binding.rentalReqBtn.isEnabled = false
                binding.rentalReqBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.dark_gray)
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this@CartActivity, ClientSettingActivity::class.java))
        }

        binding.rentalReqBtn.setOnClickListener {
            if (itemList.isEmpty()) {
                Toast.makeText(this@CartActivity, "Cart is Empty", Toast.LENGTH_SHORT).show()
            } else {
                clientObj?.let { client ->
                    if (client.cnic.isNullOrEmpty() && client.phone.isNullOrEmpty() && client.address.isNullOrEmpty()) {
                        showVerificationDialog()
                    } else {
                        showReqBottomSheet()
                    }
                }
            }
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(this@CartActivity, LinearLayoutManager.VERTICAL, false)
        adapter = CartRcvAdapter(this, itemList, senderUid)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeCartLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeCartLiveData().observe(this) { items ->
            binding.loading.visibility = View.GONE
            itemList.clear()
            items?.let {
                itemList.addAll(it)
                Log.d("CartDebug", "observeCartLiveData: ${it.size}")
            }
            adapter.updateList(itemList) // Notify the adapter about the changes
        }
    }

    private fun observeClientLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModelC.observeClientLiveData().observe(this) { client ->
            binding.loading.visibility = View.GONE
            if (client != null) {
                client.pass?.let { currentDecryptedPassword = Security.decrypt(it) }
                clientObj = client //Passing whole client to the obj

                // Enable the button if clientObj is not null
                binding.rentalReqBtn.isEnabled = true
                binding.rentalReqBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttons_color)
            }
        }
    }

    private fun showReqBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = SendRentalReq.newInstance(object : SendRentalReq.OnSendReqListener {
            override fun onReqSend(
                rentalAddress: String?,
                startDuration: String?,
                endDuration: String?,
                rent: Int?,
                itemList: ArrayList<RentalItem>?
            ) {
                val onesignal = OnesignalService(this@CartActivity)
                onesignal.sendReqNotiByOneSignalToSegment(
                    clientObj?.id, clientObj?.name, clientObj?.address,
                    clientObj?.email, clientObj?.phone, clientObj?.cnic,
                    rentalAddress ,startDuration, endDuration, rent, itemList
                )
                clearCart(clientObj?.id)
                finish()
            }
        }, clientObj, itemList)
        bottomSheetDialog.show(this.supportFragmentManager, "RentalReq")
    }

    private fun clearCart(clientId: String?) {
        clientId?.let { cId ->
            val databaseRef = Firebase.database.reference.child("Cart").child(cId)
            databaseRef.removeValue()
                .addOnSuccessListener {}
                .addOnFailureListener {}
        }
    }

    private fun showVerificationDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Account Verification")
            .setBackground(ContextCompat.getDrawable(this, R.drawable.msg_view_received))
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
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    private fun showVerifyBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = UpdateClient.newInstance(object : UpdateClient.OnClientUpdatedListener {
            override fun onClientUpdated(name: String?, email: String?, pass: String?, cnic: String?, phone: String?, address: String?) {}

            override fun onClientVerified(cnic: String?, phone: String?, address: String?) {
                ClientSettingActivity.verifyClient(cnic, address, phone, currentDecryptedPassword, this@CartActivity)
            }
        }, true)
        bottomSheetDialog.show(this.supportFragmentManager, "Client")
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }

    override fun onPause() {
        super.onPause()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Offline"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }
}