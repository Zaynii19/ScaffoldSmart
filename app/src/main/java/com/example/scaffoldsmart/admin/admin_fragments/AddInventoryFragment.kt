package com.example.scaffoldsmart.admin.admin_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentAddInventoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddInventoryFragment : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentAddInventoryBinding.inflate(layoutInflater)
    }

    private var selectedAvailability = ""

    private var onInventoryUpdatedListener: OnInventoryUpdatedListener? = null

    interface OnInventoryUpdatedListener {
        fun onInventoryUpdated(itemName: String, price: String, quantity: String, availability: String)
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

        getSpinnerValues()

        binding.saveBtn.setOnClickListener {
            val itemName = binding.itemName.text.toString()
            val price = binding.itemPrice.text.toString()
            val quantity = binding.itemQuantity.text.toString()
            val statusValue = selectedAvailability

            if (itemName.isNotEmpty() && price.isNotEmpty() && quantity.isNotEmpty() && statusValue.isNotEmpty()) {
                onInventoryUpdatedListener?.onInventoryUpdated(itemName, price, quantity, statusValue)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add require information", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setSpinner() {
        // Define the options for the Spinner
        val availabilityOptions = listOf("In-stock", "Rented")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val availabilityAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, availabilityOptions)

        // Specify the layout to use when the list of choices appears
        availabilityAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.availabilitySpinner.adapter = availabilityAdapter
    }

    private fun getSpinnerValues() {
        binding.availabilitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                selectedAvailability = parent.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }

    companion object {
        fun newInstance(listener: OnInventoryUpdatedListener): AddInventoryFragment {
            val fragment = AddInventoryFragment()
            fragment.onInventoryUpdatedListener = listener
            return fragment
        }
    }
}