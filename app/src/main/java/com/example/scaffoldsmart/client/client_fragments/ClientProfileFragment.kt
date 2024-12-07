package com.example.scaffoldsmart.client.client_fragments

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
import com.example.scaffoldsmart.databinding.ClientDetailsDialogBinding
import com.example.scaffoldsmart.databinding.FragmentClientProfileBinding
import com.example.scaffoldsmart.databinding.SocialPlatformDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class ClientProfileFragment : Fragment() {
    private val binding by lazy {
        FragmentClientProfileBinding.inflate(layoutInflater)
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
            clientDetailsDialog()
        }

        binding.otherPlatformBtn.setOnClickListener {
            adminPlatformDialog()
        }

        binding.logoutBtn.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            Toast.makeText(requireActivity(), "Logout Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }

    }

    private fun clientDetailsDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.client_details_dialog, null)
        val binder = ClientDetailsDialogBinding.bind(customDialog)

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(customDialog)
            .setTitle("Client Details")
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

    private fun adminPlatformDialog(){
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.social_platform_dialog, null)
        val binder = SocialPlatformDialogBinding.bind(customDialog)

        /*binder.facebook.setOnClickListener {

        }*/

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(customDialog)
            .setTitle("Admin Other Platforms")
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