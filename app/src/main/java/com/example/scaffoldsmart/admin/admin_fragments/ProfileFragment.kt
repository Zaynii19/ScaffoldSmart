package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.LoginActivity
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.databinding.AdminDetailsDialogBinding
import com.example.scaffoldsmart.databinding.FragmentProfileBinding
import com.example.scaffoldsmart.databinding.InvoiceItemBinding
import com.example.scaffoldsmart.databinding.SocialPlatformDialogBinding
import com.example.scaffoldsmart.util.CheckNetConnectvity
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database

class ProfileFragment : Fragment() {
    private val binding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }
    private var name: String = ""
    private var email: String = ""
    private var company: String = ""
    private var address: String = ""
    private var phone: String = ""
    private lateinit var onesignal: OnesignalService
    private lateinit var chatPreferences: SharedPreferences
    private lateinit var invoicePreferences: SharedPreferences
    private var senderUid: String? = null
    private lateinit var viewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatPreferences = requireActivity().getSharedPreferences("CHATADMIN", MODE_PRIVATE)
        invoicePreferences = requireActivity().getSharedPreferences("INVOICE", MODE_PRIVATE)

        onesignal = OnesignalService(requireActivity())

        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModel.retrieveAdminData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        observeAdminLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }

        binding.personalDetailBtn.setOnClickListener {
            adminDetailsDialog()
        }

        binding.otherPlatformBtn.setOnClickListener {
            otherPlatformDialog()
        }

        binding.sendReminderBtn.setOnClickListener {
            showReminderBottomSheet()
        }

        binding.invoiceGenerateBtn.setOnClickListener {
            invoicePreferences.edit { putString("USER", "admin") }
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
                    Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
                }
                Toast.makeText(requireActivity(), "Logout Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
            } else {
                Toast.makeText(requireActivity(), "Please check your internet connection and try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAdminLiveData() {
        viewModel.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            if (admin != null) {
                name = admin.name
                email = admin.email
                company = admin.company
                phone = admin.phone
                address = admin.address

                binding.userName.text = name
                binding.companyName.text = company
            }
        }
    }

    private fun adminDetailsDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.admin_details_dialog, null)
        val binder = AdminDetailsDialogBinding.bind(customDialog)

        binder.adminName.text = name
        binder.email.text = email
        binder.address.text = address
        binder.companyName.text = company
        binder.phoneNum.text = phone

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(customDialog)
            .setTitle("Admin Details")
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

    private fun otherPlatformDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.social_platform_dialog, null)
        val binder = SocialPlatformDialogBinding.bind(customDialog)

        /*binder.facebook.setOnClickListener {

        }*/

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(customDialog)
            .setTitle("Other Platforms")
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

    private fun showReminderBottomSheet() {
        val bottomSheetDialog = ReminderSendFragment()
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "Reminder")
    }

    private fun showInvoiceBottomSheet() {
        val bottomSheetDialog = InvoiceFragment()
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "Invoice")
    }
}