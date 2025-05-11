package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
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
import com.example.scaffoldsmart.admin.admin_bottomsheets.ShowInvoice
import com.example.scaffoldsmart.admin.admin_bottomsheets.SendReminder
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.databinding.AdminDetailsDialogBinding
import com.example.scaffoldsmart.databinding.FragmentProfileBinding
import com.example.scaffoldsmart.databinding.SocialPlatformDialogBinding
import com.example.scaffoldsmart.util.CheckNetConnectvity
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import androidx.core.net.toUri

class ProfileFragment : Fragment() {
    private val binding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }
    private lateinit var onesignal: OnesignalService
    private lateinit var chatPreferences: SharedPreferences
    private lateinit var invoicePreferences: SharedPreferences
    private var senderUid: String? = null
    private lateinit var viewModel: AdminViewModel
    private var adminObj: AdminModel? = null

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
                    senderUid?.let { Firebase.database.reference.child("ChatUser").child(it) }?.updateChildren(presenceMap)
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
                adminObj = admin

                binding.userName.text = admin.name
                binding.companyName.text = admin.company
            }
        }
    }

    private fun adminDetailsDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.admin_details_dialog, null)
        val binder = AdminDetailsDialogBinding.bind(customDialog)

        adminObj?.let { binder.adminName.text = it.name }
        adminObj?.let { binder.email.text = it.email }
        adminObj?.let { binder.address.text = it.address }
        adminObj?.let { binder.companyName.text = it.company }
        adminObj?.let { binder.phoneNum.text = it.phone }

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

    private fun showReminderBottomSheet() {
        val bottomSheetDialog = SendReminder()
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "Reminder")
    }

    private fun showInvoiceBottomSheet() {
        val bottomSheetDialog = ShowInvoice()
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "ShowInvoice")
    }
}
