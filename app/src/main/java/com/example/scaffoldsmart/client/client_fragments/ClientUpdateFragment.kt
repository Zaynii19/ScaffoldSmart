package com.example.scaffoldsmart.client.client_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentClientUpdateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClientUpdateFragment : BottomSheetDialogFragment() {
    private val binding by lazy {
        FragmentClientUpdateBinding.inflate(layoutInflater)
    }
    private var onClientUpdatedListener: OnClientUpdatedListener? = null

    interface OnClientUpdatedListener {
        fun onClientUpdated()
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

        binding.saveBtn.setOnClickListener {

            if (binding.adminName.text!!.isNotEmpty()) {
                //onClientUpdatedListener?.onAdminUpdated(gender)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add Name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(listener: OnClientUpdatedListener): ClientUpdateFragment {
            val fragment = ClientUpdateFragment()
            fragment.onClientUpdatedListener = listener
            return fragment
        }
    }

}