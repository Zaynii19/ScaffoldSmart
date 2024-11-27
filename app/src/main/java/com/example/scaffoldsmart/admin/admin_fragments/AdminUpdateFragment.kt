package com.example.scaffoldsmart.admin.admin_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentAdminUpdateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AdminUpdateFragment : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentAdminUpdateBinding.inflate(layoutInflater)
    }
    private var onAdminUpdatedListener: OnAdminUpdatedListener? = null

    interface OnAdminUpdatedListener {
        fun onAdminUpdated()
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

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val genderAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, genderOptions)

        // Specify the layout to use when the list of choices appears
        genderAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.genderSpinner.adapter = genderAdapter

        binding.saveBtn.setOnClickListener {

            val gender = binding.genderSpinner.selectedItem.toString()

            if (gender.isNotEmpty()) {
                //onAdminUpdatedListener?.onAdminUpdated(gender)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add Name and Price", Toast.LENGTH_SHORT).show()
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        fun newInstance(listener: OnAdminUpdatedListener): AdminUpdateFragment {
            val fragment = AdminUpdateFragment()
            fragment.onAdminUpdatedListener = listener
            return fragment
        }
    }

}