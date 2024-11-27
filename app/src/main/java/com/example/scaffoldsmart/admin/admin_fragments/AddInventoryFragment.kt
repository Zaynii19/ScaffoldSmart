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

        // Define the options for the Spinner
        val lengthOptions = listOf("3.0 meters (10 feet)", "4.0 meters (13 feet)", "5.0 meters (16 feet)", "6.0 meters (20 feet)")
        val weightOptions = listOf("7 kg per meter", "8 kg per meter", "9 kg per meter", "10 kg per meter")
        val conditionOptions = listOf("Intact", "Damaged")
        val availabilityOptions = listOf("In-stock", "Rented")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val lengthAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, lengthOptions)
        val weightAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, weightOptions)
        val conditionAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, conditionOptions)
        val availabilityAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, availabilityOptions)

        // Specify the layout to use when the list of choices appears
        lengthAdapter.setDropDownViewResource(R.layout.spinner_item)
        weightAdapter.setDropDownViewResource(R.layout.spinner_item)
        conditionAdapter.setDropDownViewResource(R.layout.spinner_item)
        availabilityAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.lengthSpinner.adapter = lengthAdapter
        binding.weightSpinner.adapter = weightAdapter
        binding.conditionSpinner.adapter = conditionAdapter
        binding.availabilitySpinner.adapter = availabilityAdapter

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

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        fun newInstance(listener: OnInventoryUpdatedListener): AddInventoryFragment {
            val fragment = AddInventoryFragment()
            fragment.onInventoryUpdatedListener = listener
            return fragment
        }
    }
}