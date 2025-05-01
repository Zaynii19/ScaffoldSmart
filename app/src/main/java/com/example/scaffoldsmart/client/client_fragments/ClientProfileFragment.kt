package com.example.scaffoldsmart.client.client_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
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
import com.example.scaffoldsmart.admin.ChatActivity
import com.example.scaffoldsmart.admin.admin_models.ChatUserModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.client.ClientChatActivity
import com.example.scaffoldsmart.client.ClientCostComparisonActivity
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.ClientDetailsDialogBinding
import com.example.scaffoldsmart.databinding.FragmentClientProfileBinding
import com.example.scaffoldsmart.databinding.SocialPlatformDialogBinding
import com.example.scaffoldsmart.util.CheckNetConnectvity
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import androidx.core.content.edit

class ClientProfileFragment : Fragment() {
    private val binding by lazy {
        FragmentClientProfileBinding.inflate(layoutInflater)
    }
    private var email: String = ""
    private var name: String = ""
    private var cnic: String = ""
    private var address: String = ""
    private var phone: String = ""
    private lateinit var viewModel: ClientViewModel
    private lateinit var chatPreferences: SharedPreferences
    private var senderUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatPreferences = requireActivity().getSharedPreferences("CHATCLIENT", MODE_PRIVATE)

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
                    Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
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
                name = client.name
                email = client.email
                cnic = client.cnic
                address = client.address
                phone = client.phone

                binding.userName.text = name
                binding.email.text = email
            }
        }
    }

    private fun clientDetailsDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.client_details_dialog, null)
        val binder = ClientDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = name
        binder.email.text = email
        binder.cnic.text = cnic
        binder.address.text = address
        binder.phoneNum.text = phone

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

        /*binder.facebook.setOnClickListener {

        }*/

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

    private fun fetchingAdminData() {
        Firebase.database.reference.child("Admin").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue(ClientModel::class.java)
                    if (user != null) {
                        chatPreferences.edit { putString("receiverUid", user.id) }
                        chatPreferences.edit { putString("receiverName", user.name) }

                        Log.d("ClientProfileDebug", "ReceiverUid: ${user.id}, ReceiverName: ${user.name}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ClientProfileDebug", "Failed to retrieve others user data", error.toException())
            }
        })
    }
}