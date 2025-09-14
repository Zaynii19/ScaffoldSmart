package com.example.scaffoldsmart.client.client_bottomsheets

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.databinding.AddToCartBinding
import com.example.scaffoldsmart.util.parcelable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddToCart : BottomSheetDialogFragment() {
    private val binding by lazy {
        AddToCartBinding.inflate(layoutInflater)
    }
    private var onItemAddedListener: OnItemAddedListener? = null
    private var item: InventoryModel? = null
    private lateinit var dialog: AlertDialog
    private var itemQuantity: Int? = null
    private var pipesLength: Int? = null

    interface OnItemAddedListener {
        fun onItemAdded(itemName: String?, quantity: Int?, price: Int?)
        fun onPipesAdded(itemName: String?, quantity: Int?, price: Int?, pipesLength: Int?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the item from arguments safely
        item = arguments?.parcelable<InventoryModel>(ARG_ITEM) ?: InventoryModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding.itemName.text = item?.itemName
        item?.let {
            val lowerName = it.itemName?.lowercase() ?: return@let
            when {
                lowerName.contains("pipe") ->
                    item?.itemName.let { itemName ->
                        binding.itemQty.visibility = View.VISIBLE
                        binding.pipesLength.visibility = View.VISIBLE
                        binding.itemQtySpinner.visibility = View.GONE
                        binding.itemQty.hint = "$itemName Quantity"
                    }
                lowerName.contains("joint") ->
                    item?.itemName.let { itemName ->
                        binding.itemQty.visibility = View.VISIBLE
                        binding.pipesLength.visibility = View.GONE
                        binding.itemQtySpinner.visibility = View.GONE
                        binding.itemQty.hint = "$itemName Quantity"
                    }
                else -> {
                    binding.itemQty.visibility = View.GONE
                    binding.pipesLength.visibility = View.GONE
                    binding.itemQtySpinner.visibility = View.VISIBLE
                    setSpinner(it.quantity)
                }
            }
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        item?.let {
            val lowerName = it.itemName?.lowercase() ?: ""
            when {
                lowerName.contains("pipe") -> getPipes()
                else -> getSpinnerValues()
            }
        }

        binding.addToCartBtn.setOnClickListener {
            item?.let { currentItem ->
                val lowerName = currentItem.itemName?.lowercase() ?: ""
                when {
                    lowerName.contains("pipe") -> handlePipeAddition(currentItem)
                    lowerName.contains("joint") -> handleJointAddition(currentItem)
                    else -> handleSpinnerItemAddition(currentItem)
                }
            }
        }
    }

    private fun setSpinner(quantity: Int?) {
        val quantities = mutableListOf<Int>()
        val maxQuantity = quantity ?: 0

        // Create a list from 0 to maxQuantity
        for (i in 0..maxQuantity) {
            quantities.add(i)
        }

        // Create an adapter for the spinner
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            quantities
        ).apply {
            setDropDownViewResource(R.layout.spinner_item)
        }

        // Set the adapter to the spinner
        binding.itemQtySpinner.adapter = adapter
    }

    private fun getPipes() {
        // Initial setup for pipesLength click listener
        binding.pipesLength.setOnClickListener {
            if (binding.itemQuantity.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please enter pipe quantity first.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.itemQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                itemQuantity = try {
                    binding.itemQuantity.text.toString().toInt()
                } catch (_: NumberFormatException) {
                    0
                }

                pipesLength = try {
                    binding.pipesLength.text.toString().toInt()
                } catch (_: NumberFormatException) {
                    0
                }

                if (binding.itemQuantity.text.isNullOrEmpty()) {
                    // Clear pipe length if pipe quantity is empty and pipe length is not empty
                    binding.pipesLength.setText("")
                    binding.pipesLength.setOnClickListener(null) // Remove click listener
                } else {
                    binding.pipesLength.setOnClickListener {
                        getPipesLength()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getPipesLength() {
        // Create a new NumberPicker
        val numberPicker = NumberPicker(requireContext()).apply {
            minValue = 5
            maxValue = 20
        }
        numberPicker.textColor = Color.BLACK

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Pipe Length")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                pipesLength = numberPicker.value // Capture the selected value
                binding.pipesLength.setText(
                    buildString {
                        append(pipesLength)
                        append(" feet")
                    }
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Just dismiss the dialog
            }
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .create()

        dialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            //Customize button colors after the dialog is shown
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
    }

    private fun getJoints() {
        itemQuantity = try {
            binding.itemQuantity.text.toString().toInt()
        } catch (_: NumberFormatException) {
            0
        }
    }

    private fun getSpinnerValues () {
        binding.itemQtySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                itemQuantity = if (selected.isNotEmpty()) selected.toInt() else 0
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                itemQuantity = 0
            }
        }
    }

    private fun handlePipeAddition(item: InventoryModel) {
        when {
            (itemQuantity == null || itemQuantity == 0) && (pipesLength == null || pipesLength == 0) -> {
                Toast.makeText(requireActivity(), "Please enter item information", Toast.LENGTH_SHORT).show()
                return
            }
            pipesLength == null || pipesLength == 0 -> {
                Toast.makeText(requireActivity(), "Please enter pipe length", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                onItemAddedListener?.onPipesAdded(item.itemName, itemQuantity, item.price, pipesLength)
                dismiss()
            }
        }
    }

    private fun handleJointAddition(item: InventoryModel) {
        getJoints()
        if (itemQuantity == null || itemQuantity == 0) {
            Toast.makeText(requireActivity(), "Please enter item quantity", Toast.LENGTH_SHORT).show()
            return
        }
        onItemAddedListener?.onItemAdded(item.itemName, itemQuantity, item.price)
        dismiss()
    }

    private fun handleSpinnerItemAddition(item: InventoryModel) {
        if (itemQuantity == null || itemQuantity == 0) {
            Toast.makeText(requireActivity(), "Please enter item quantity", Toast.LENGTH_SHORT).show()
            return
        }
        onItemAddedListener?.onItemAdded(item.itemName, itemQuantity, item.price)
        dismiss()
    }

    companion object {
        private const val ARG_ITEM = "arg_item"
        fun newInstance(listener: OnItemAddedListener, item: InventoryModel?): AddToCart {
            val fragment = AddToCart()
            fragment.onItemAddedListener = listener

            // Bundle to pass arguments
            val args = Bundle()
            args.putParcelable(ARG_ITEM, item)  // Pass the item as Parcelable
            fragment.arguments = args

            return fragment
        }
    }
}