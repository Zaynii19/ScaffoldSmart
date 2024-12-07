package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.scaffoldsmart.LoginActivity
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentProfileBinding
import com.example.scaffoldsmart.databinding.SocialPlatformDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private val binding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.personalDetailBtn.setOnClickListener {
            adminDetailsDialog()
        }

        binding.otherPlatformBtn.setOnClickListener {
            otherPlatformDialog()
        }

        binding.logoutBtn.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            Toast.makeText(requireActivity(), "Logout Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }

    }

    private fun adminDetailsDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.admin_details_dialog, null)

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(customDialog)
            .setTitle("Admin Details")
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.curved_msg_view_client))
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
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.curved_msg_view_client))
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
}