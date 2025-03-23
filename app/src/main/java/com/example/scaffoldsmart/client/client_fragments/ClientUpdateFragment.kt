package com.example.scaffoldsmart.client.client_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.FragmentClientUpdateBinding
import com.example.scaffoldsmart.util.Encryption
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClientUpdateFragment : BottomSheetDialogFragment() {
    private val binding by lazy {
        FragmentClientUpdateBinding.inflate(layoutInflater)
    }
    private var onClientUpdatedListener: OnClientUpdatedListener? = null
    private lateinit var viewModel: ClientViewModel
    private var isVerify: Boolean = false

    interface OnClientUpdatedListener {
        fun onClientUpdated(name: String, email: String, pass: String, cnic: String, phone: String, address: String)
        fun onClientVerified(cnic: String, phone: String, address: String)
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
            binding.clientName.visibility = View.GONE
            binding.email.visibility = View.GONE
            binding.password.visibility = View.GONE
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
            val email = binding.email.text.toString()
            val pass  = binding.password.text.toString()
            val cnic = binding.cnic.text.toString()
            val phone = binding.phoneNum.text.toString()
            val address = binding.address.text.toString()

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
                val decryptedPassword = Encryption.decrypt(client.pass)
                binding.password.setText(decryptedPassword)
                binding.cnic.setText(client.cnic)
                binding.phoneNum.setText(client.phone)
                binding.address.setText(client.address)
            }
        }
    }

    companion object {
        private const val ARG_IS_VERIFY = "is_verify"
        fun newInstance(listener: OnClientUpdatedListener, isVerify: Boolean): ClientUpdateFragment {
            val fragment = ClientUpdateFragment()
            fragment.onClientUpdatedListener = listener

            // Bundle to pass arguments
            val args = Bundle()
            args.putBoolean(ARG_IS_VERIFY, isVerify) // Pass the isVerify flag
            fragment.arguments = args

            return fragment
        }
    }
}