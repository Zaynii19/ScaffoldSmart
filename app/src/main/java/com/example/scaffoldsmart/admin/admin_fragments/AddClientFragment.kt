package com.example.scaffoldsmart.admin.admin_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentAddClientBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddClientFragment : BottomSheetDialogFragment() {
    private val binding by lazy {
        FragmentAddClientBinding.inflate(layoutInflater)
    }
    private var onClientUpdatedListener: OnClientUpdatedListener? = null

    interface OnClientUpdatedListener {
        fun onClientUpdated(clientName: String, gender: String, status: String)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Define the options for the Spinner
        val genderOptions = listOf("Male", "Female")
        val statusOptions = listOf("Ongoing", "Returned", "Overdue")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val genderAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, genderOptions)
        val statusAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, statusOptions)

        // Specify the layout to use when the list of choices appears
        genderAdapter.setDropDownViewResource(R.layout.spinner_item)
        statusAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.genderSpinner.adapter = genderAdapter
        binding.statusSpinner.adapter = statusAdapter

        binding.saveBtn.setOnClickListener {
            val clientName = binding.name.text.toString()
            val gender = binding.genderSpinner.selectedItem.toString()
            val status = binding.statusSpinner.selectedItem.toString()

            if (clientName.isNotEmpty() && status.isNotEmpty() && gender.isNotEmpty()) {
                onClientUpdatedListener?.onClientUpdated(clientName, gender, status)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add Name and Price", Toast.LENGTH_SHORT).show()
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        fun newInstance(listener: OnClientUpdatedListener): AddClientFragment {
            val fragment = AddClientFragment()
            fragment.onClientUpdatedListener = listener
            return fragment
        }
    }
}