package com.example.scaffoldsmart.client.client_bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.UpdateClientBinding
import com.example.scaffoldsmart.util.Security
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UpdateClient : BottomSheetDialogFragment() {
    private val binding by lazy {
        UpdateClientBinding.inflate(layoutInflater)
    }
    private var onClientUpdatedListener: OnClientUpdatedListener? = null
    private lateinit var viewModel: ClientViewModel
    private var isVerify: Boolean = false

    interface OnClientUpdatedListener {
        fun onClientUpdated(name: String?, email: String?, pass: String?, cnic: String?, phone: String?, address: String?)
        fun onClientVerified(cnic: String?, phone: String?, address: String?)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve arguments
        arguments?.let { args ->
            isVerify = args.getBoolean(ARG_IS_VERIFY, false) // Default to false if not provided
        }

        if (isVerify) {
            binding.title.text = getString(R.string.verify_account)
            binding.saveBtn.text = getString(R.string.verify)
            binding.textInputLayout.visibility = View.GONE
            binding.textInputLayout2.visibility = View.GONE
            binding.textInputLayout3.visibility = View.GONE
        } else {
            binding.title.text = getString(R.string.update_account_info)
            binding.saveBtn.text = getString(R.string.save)
        }

        viewModel = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel.retrieveClientData()
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

        binding.saveBtn.setOnClickListener {
            val name = binding.clientName.text.toString()
            var email = ""
            var pass  = ""
            var cnic = ""
            var phone = ""
            val address = binding.address.text.toString()

            if (binding.email.text.toString().contains("@")  || binding.email.text.toString().contains(".com")) {
                email = binding.email.text.toString()
            } else {
                binding.email.error = "Enter a valid email"
                binding.email.setText("")
            }

            val pattern = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}\$".toRegex()
            if (binding.password.text.toString().matches(pattern)) {
                pass  = binding.password.text.toString()
            } else {
                binding.password.error = "Password must contain: lowercase letters, numbers, symbols, and be at least 8 characters"
                binding.password.setText("")
            }

            if (binding.cnic.text.toString().length == 13) {
                cnic = binding.cnic.text.toString()
            } else {
                binding.cnic.error = "Enter a valid CNIC"
                binding.cnic.setText("")
            }

            if (binding.phoneNum.text.toString().length == 11) {
                phone  = binding.phoneNum.text.toString()
            } else {
                binding.phoneNum.error = "Enter a valid Phone Number"
                binding.phoneNum.setText("")
            }

            if (isVerify) {
                if (cnic.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                    onClientUpdatedListener?.onClientVerified(cnic, phone, address)
                    dismiss() // Dismiss only after saving data
                } else {
                    Toast.makeText(context, "Please fill require information", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && cnic.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                    onClientUpdatedListener?.onClientUpdated(name, email, pass, cnic, phone, address)
                    dismiss() // Dismiss only after saving data
                } else {
                    Toast.makeText(context, "Please fill require information", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeClientLiveData() {
        viewModel.observeClientLiveData().observe(viewLifecycleOwner) { client ->
            if (client != null) {
                binding.clientName.setText(client.name)
                binding.email.setText(client.email)
                val decryptedPassword = client.pass?.let { Security.decrypt(it) }
                binding.password.setText(decryptedPassword)
                binding.cnic.setText(client.cnic)
                binding.phoneNum.setText(client.phone)
                binding.address.setText(client.address)
            }
        }
    }

    companion object {
        private const val ARG_IS_VERIFY = "is_verify"
        fun newInstance(listener: OnClientUpdatedListener, isVerify: Boolean): UpdateClient {
            val fragment = UpdateClient()
            fragment.onClientUpdatedListener = listener

            // Bundle to pass arguments
            val args = Bundle()
            args.putBoolean(ARG_IS_VERIFY, isVerify) // Pass the isVerify flag
            fragment.arguments = args

            return fragment
        }
    }
}