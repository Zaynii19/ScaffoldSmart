package com.example.scaffoldsmart.admin.admin_bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.util.Security
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.databinding.UpdateAdminBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UpdateAdmin : BottomSheetDialogFragment() {

    private val binding by lazy {
        UpdateAdminBinding.inflate(layoutInflater)
    }
    private lateinit var viewModel: AdminViewModel
    private var onAdminUpdatedListener: OnAdminUpdatedListener? = null

    interface OnAdminUpdatedListener {
        fun onAdminUpdated(name: String?, email: String?, pass: String?, company: String?, phone: String?, address: String?)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun observeAdminLiveData() {
        viewModel.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            if (admin != null) {
                binding.adminName.setText(admin.name)
                binding.email.setText(admin.email)
                val decryptedPassword = admin.pass?.let { Security.decrypt(it) }
                binding.password.setText(decryptedPassword)
                binding.companyName.setText(admin.company)
                binding.phoneNum.setText(admin.phone)
                binding.companyAddress.setText(admin.address)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBtn.setOnClickListener {
            val name = binding.adminName.text.toString()
            val email = binding.email.text.toString()
            val pass  = binding.password.text.toString()
            val company = binding.companyName.text.toString()
            val phone = binding.phoneNum.text.toString()
            val address = binding.companyAddress.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && company.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                onAdminUpdatedListener?.onAdminUpdated(name, email, pass, company, phone, address)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please fill require information", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(listener: OnAdminUpdatedListener): UpdateAdmin {
            val fragment = UpdateAdmin()
            fragment.onAdminUpdatedListener = listener
            return fragment
        }
    }
}