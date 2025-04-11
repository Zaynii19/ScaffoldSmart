package com.example.scaffoldsmart.admin.admin_fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.databinding.FragmentAddInventoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddInventoryFragment : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentAddInventoryBinding.inflate(layoutInflater)
    }

    private lateinit var availabilityAdapter: ArrayAdapter<String>
    private var selectedAvailability = ""
    private lateinit var availabilityOptions: List<String>
    private lateinit var inventoryPreferences: SharedPreferences
    private var isUpdate = false
    private var itemId: String = ""

    private var onInventoryUpdatedListener: OnInventoryUpdatedListener? = null

    interface OnInventoryUpdatedListener {
        fun onInventoryAdded(itemName: String, price: Int, quantity: Int, availability: String)
        fun onInventoryUpdated(itemId: String, itemName: String, price: Int, quantity: Int, availability: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inventoryPreferences = requireActivity().getSharedPreferences("INVENTORY", MODE_PRIVATE)
        isUpdate = inventoryPreferences.getBoolean("Update", false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setSpinner()
        if (isUpdate) {
            setValuesForUpdate()
        }
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
                if (isUpdate) {
                    onInventoryUpdatedListener?.onInventoryUpdated(itemId, itemName, price.toInt(), quantity.toInt(), statusValue)
                } else{
                    onInventoryUpdatedListener?.onInventoryAdded(itemName, price.toInt(), quantity.toInt(), statusValue)
                }
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add require information", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setSpinner() {
        // Define the options for the Spinner
        availabilityOptions = listOf("In-stock", "Rented")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        availabilityAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, availabilityOptions)

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

    private fun setValuesForUpdate() {
        val item: InventoryModel = arguments?.getSerializable(ARG_ITEM) as InventoryModel
        binding.title.text = getString(R.string.update_inventory)
        itemId = item.itemId
        binding.itemName.setText(item.itemName)
        binding.itemPrice.setText("${item.price}")
        binding.itemQuantity.setText("${item.quantity}")
        val position = availabilityOptions.indexOf(item.availability)
        binding.availabilitySpinner.setSelection(position)
    }

    companion object {
        private const val ARG_ITEM = "arg_item"
        fun newInstance(listener: OnInventoryUpdatedListener, item: InventoryModel): AddInventoryFragment {
            val fragment = AddInventoryFragment()

            fragment.onInventoryUpdatedListener = listener

            val args = Bundle()
            args.putSerializable(ARG_ITEM, item)  // Pass the item as Serializable
            fragment.arguments = args

            return fragment
        }
    }
}