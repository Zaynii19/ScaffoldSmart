package com.example.scaffoldsmart.admin.admin_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentAddInventoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddInventoryFragment : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentAddInventoryBinding.inflate(layoutInflater)
    }

    private var onInventoryUpdatedListener: OnInventoryUpdatedListener? = null

    interface OnInventoryUpdatedListener {
        fun onInventoryUpdated(itemName: String, price: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setSpinner()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBtn.setOnClickListener {
            val itemName = binding.itemName.text.toString()
            val price = binding.itemPrice.text.toString()

            if (itemName.isNotEmpty() && price.isNotEmpty()) {
                onInventoryUpdatedListener?.onInventoryUpdated(itemName, price)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add Name and Price", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setSpinner() {
        // Define the options for the Spinner
        val lengthOptions = listOf("5 to 10 feet", "10 to 15 feet", "15 to 20 feet")
        val availabilityOptions = listOf("In-stock", "Rented")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val lengthAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, lengthOptions)
        val availabilityAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, availabilityOptions)

        // Specify the layout to use when the list of choices appears
        lengthAdapter.setDropDownViewResource(R.layout.spinner_item)
        availabilityAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.lengthSpinner.adapter = lengthAdapter
        binding.availabilitySpinner.adapter = availabilityAdapter
    }

    companion object {
        fun newInstance(listener: OnInventoryUpdatedListener): AddInventoryFragment {
            val fragment = AddInventoryFragment()
            fragment.onInventoryUpdatedListener = listener
            return fragment
        }
    }
}