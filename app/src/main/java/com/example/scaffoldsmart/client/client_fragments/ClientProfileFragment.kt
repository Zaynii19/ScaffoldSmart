package com.example.scaffoldsmart.client.client_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.LoginActivity
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.client.ClientChatActivity
import com.example.scaffoldsmart.client.ClientCostComparisonActivity
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.ClientDetailsDialogBinding
import com.example.scaffoldsmart.databinding.FragmentClientProfileBinding
import com.example.scaffoldsmart.databinding.SocialPlatformDialogBinding
import com.example.scaffoldsmart.util.CheckNetConnectvity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.scaffoldsmart.admin.admin_bottomsheets.ShowInvoice
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.client.client_models.ClientModel

class ClientProfileFragment : Fragment() {
    private val binding by lazy {
        FragmentClientProfileBinding.inflate(layoutInflater)
    }
    private var clientObj: ClientModel? = null
    private lateinit var viewModel: ClientViewModel
    private lateinit var chatPreferences: SharedPreferences
    private lateinit var invoicePreferences: SharedPreferences
    private var senderUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatPreferences = requireActivity().getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        invoicePreferences = requireActivity().getSharedPreferences("INVOICE", MODE_PRIVATE)

        viewModel = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel.retrieveClientData()

        fetchingAdminData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        observeClientLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, ClientSettingActivity::class.java))
        }

        binding.personalDetailBtn.setOnClickListener {
            clientDetailsDialog()
        }

        binding.otherPlatformBtn.setOnClickListener {
            adminPlatformDialog()
        }

        binding.costComparisonBtn.setOnClickListener {
            startActivity(Intent(context, ClientCostComparisonActivity::class.java))
        }

        binding.invoiceGenerateBtn.setOnClickListener {
            invoicePreferences.edit { putString("USER", "client") }
            showInvoiceBottomSheet()
        }

        binding.logoutBtn.setOnClickListener {
            if (CheckNetConnectvity.hasInternetConnection(requireActivity())) {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                senderUid = chatPreferences.getString("SenderUid", null)
                if (senderUid != null) {
                    val currentTime = System.currentTimeMillis()
                    val presenceMap = HashMap<String, Any>()
                    presenceMap["status"] = "Offline"
                    presenceMap["lastSeen"] = currentTime
                    senderUid?.let { Firebase.database.reference.child("ChatUser").child(it) }?.updateChildren(presenceMap)
                }
                Toast.makeText(requireActivity(), "Logout Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
            } else {
                Toast.makeText(requireActivity(), "Please check your internet connection and try again", Toast.LENGTH_SHORT).show()
            }
        }

        binding.adminChatBtn.setOnClickListener {
            startActivity(Intent(context, ClientChatActivity::class.java))
        }

    }

    private fun observeClientLiveData() {
        viewModel.observeClientLiveData().observe(viewLifecycleOwner) { client ->
            if (client != null) {

                clientObj = client

                binding.userName.text = client.name
                binding.email.text = client.email
            }
        }
    }

    private fun clientDetailsDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.client_details_dialog, null)
        val binder = ClientDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = clientObj?.name
        binder.email.text = clientObj?.email
        binder.cnic.text = clientObj?.cnic
        binder.address.text = clientObj?.address
        binder.phoneNum.text = clientObj?.phone

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(customDialog)
            .setTitle("Client Details")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }.create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
                // Set button color
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            }
    }

    private fun adminPlatformDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.social_platform_dialog, null)
        val binder = SocialPlatformDialogBinding.bind(customDialog)

        // WhatsApp click handler
        binder.whatsapp.setOnClickListener {
            openWhatsapp()
        }

        // Facebook click handler
        binder.facebook.setOnClickListener {
            openFacebook()
        }

        // Instagram click handler
        binder.instagram.setOnClickListener {
            Toast.makeText(requireActivity(), "Not Connected Yet", Toast.LENGTH_SHORT).show()
        }

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(customDialog)
            .setTitle("Admin Other Platforms")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }.create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
                // Set button color
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            }
    }

    private fun openWhatsapp() {
        val phoneNumber = "923125351971"
        val url = "https://wa.me/$phoneNumber"
        try {
            val packageManager = context?.packageManager
            val whatsappIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                setPackage("com.whatsapp") // Force open with WhatsApp if installed
            }

            packageManager?.let { pm ->
                if (whatsappIntent.resolveActivity(pm) != null) {
                    startActivity(whatsappIntent)
                } else {
                    // WhatsApp not installed, open in browser
                    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to browser
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    }

    private fun openFacebook() {
        val profileUrl = "https://www.facebook.com/share/15ar5ty5aK/?mibextid=wwXIfr"
        try {
            val packageManager = context?.packageManager
            val facebookIntent = Intent(Intent.ACTION_VIEW,
                "fb://facewebmodal/f?href=${Uri.encode(profileUrl)}".toUri())

            packageManager?.let { pm ->
                if (facebookIntent.resolveActivity(pm) != null) {
                    startActivity(facebookIntent)
                } else {
                    // Facebook app not installed, open in browser
                    startActivity(Intent(Intent.ACTION_VIEW, profileUrl.toUri()))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to browser
            startActivity(Intent(Intent.ACTION_VIEW, profileUrl.toUri()))
        }
    }

    private fun fetchingAdminData() {
        Firebase.database.reference.child("Admin").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val admin = child.getValue(AdminModel::class.java)
                    if (admin != null) {
                        chatPreferences.edit { putString("receiverUid", admin.id) }
                        chatPreferences.edit { putString("receiverName", admin.name) }

                        Log.d("ClientProfileDebug", "ReceiverUid: ${admin.id}, ReceiverName: ${admin.name}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ClientProfileDebug", "Failed to retrieve others user data", error.toException())
            }
        })
    }

    private fun showInvoiceBottomSheet() {
        val bottomSheetDialog = ShowInvoice()
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "ShowInvoice")
    }
}