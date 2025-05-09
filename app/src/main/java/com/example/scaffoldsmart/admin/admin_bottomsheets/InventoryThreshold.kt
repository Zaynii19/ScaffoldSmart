package com.example.scaffoldsmart.admin.admin_bottomsheets

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.databinding.InventoryThresholdBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database

class InventoryThreshold : BottomSheetDialogFragment() {
    private val binding by lazy {
        InventoryThresholdBinding.inflate(layoutInflater)
    }
    private var pipeThreshold: Int? = null
    private var jointsThreshold : Int? = null
    private var wenchThreshold : Int? = null
    private var pumpsThreshold : Int? = null
    private var motorsThreshold : Int? = null
    private var generatorsThreshold : Int? = null
    private var wheelThreshold : Int? = null
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var viewModel: InventoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        observeInventoryLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemList.let { items ->
            if (items.isEmpty()) {
                // Disable the button
                binding.saveBtn.isEnabled = false
                binding.saveBtn.backgroundTintList = ContextCompat.getColorStateList(requireActivity(), R.color.dark_gray)
            }
        }

        getThresholdValues()

        binding.saveBtn.setOnClickListener {
            // Get all values safely (use 0 if empty)
            pipeThreshold = binding.pipesQuantity.text.toString().toIntOrNull() ?: 0
            jointsThreshold = binding.jointsQuantity.text.toString().toIntOrNull() ?: 0
            wenchThreshold = binding.wenchQuantity.text.toString().toIntOrNull() ?: 0
            pumpsThreshold = binding.slugPumpsQuantity.text.toString().toIntOrNull() ?: 0
            motorsThreshold = binding.motorsQuantity.text.toString().toIntOrNull() ?: 0
            generatorsThreshold = binding.generatorsQuantity.text.toString().toIntOrNull() ?: 0
            wheelThreshold = binding.wheQuantityQuantity.text.toString().toIntOrNull() ?: 0

            // Check if at least one field has a non-zero value
            if (isItemThresholdSelected()) {
                updateInventoryThreshold(
                    pipeThreshold, jointsThreshold, wenchThreshold, pumpsThreshold,
                    motorsThreshold, generatorsThreshold, wheelThreshold
                )
                Toast.makeText(requireActivity(), "Threshold values updated successfully", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireActivity(), "Type at least one item threshold value", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isItemThresholdSelected(): Boolean {
        return (pipeThreshold ?: 0) > 0 ||
                (jointsThreshold ?: 0) > 0 ||
                (wenchThreshold ?: 0) > 0 ||
                (pumpsThreshold ?: 0) > 0 ||
                (motorsThreshold ?: 0) > 0 ||
                (generatorsThreshold ?: 0) > 0 ||
                (wheelThreshold ?: 0) > 0
    }

    private fun observeInventoryLiveData() {
        viewModel.observeInventoryLiveData().observe(requireActivity()) { items ->
            itemList.clear()
            items?.let {
                itemList.addAll(it)

                setThresholdValuesToEditTexts(itemList)

                // Enable the button
                binding.saveBtn.isEnabled = true
                binding.saveBtn.backgroundTintList = ContextCompat.getColorStateList(requireActivity(), R.color.buttons_color)
            }
        }
    }

    private fun setThresholdValuesToEditTexts(itemList: ArrayList<InventoryModel>) {
        if (itemList.isEmpty()) {
            Toast.makeText(requireActivity(), "Item List is empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Map of item keywords to their corresponding UI components and thresholds
        val thresholdConfigs = mapOf(
            "pipe" to Triple(binding.pipes, binding.pipesQuantity, pipeThreshold),
            "joint" to Triple(binding.joints, binding.jointsQuantity, jointsThreshold),
            "wench" to Triple(binding.wench, binding.wenchQuantity, wenchThreshold),
            "pump" to Triple(binding.slugPumps, binding.slugPumpsQuantity, pumpsThreshold),
            "motor" to Triple(binding.motors, binding.motorsQuantity, motorsThreshold),
            "generator" to Triple(binding.generators, binding.generatorsQuantity, generatorsThreshold),
            "wheel" to Triple(binding.wheel, binding.wheQuantityQuantity, wheelThreshold)
        )

        itemList.forEach { item ->
            val lowerName = item.itemName?.lowercase() ?: return@forEach
            val threshold = item.threshold ?: return@forEach

            thresholdConfigs.forEach { (keyword, config) ->
                if (lowerName.contains(keyword)) {
                    val (itemView, quantityView, typeThreshold) = config

                    if (threshold.toString().isNotEmpty() && typeThreshold.toString().isNotEmpty()) {
                        quantityView.setText("$threshold")
                    } else {
                        itemView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getThresholdValues() {
        // Set all EditText fields to be non-focusable and non-clickable
        listOf(
            binding.wenchQuantity,
            binding.slugPumpsQuantity,
            binding.motorsQuantity,
            binding.generatorsQuantity,
            binding.wheQuantityQuantity
        ).forEach { editText ->
            editText.apply {
                isFocusable = false
                isClickable = false
                setOnClickListener { showNumberPickerForField(editText) }
            }
        }
    }

    private fun showNumberPickerForField(editText: EditText) {
        val title = when (editText.id) {
            R.id.wenchQuantity -> "Select Wench Threshold"
            R.id.slugPumpsQuantity -> "Select Pumps Threshold"
            R.id.motorsQuantity -> "Select Motors Threshold"
            R.id.generatorsQuantity -> "Select Generators Threshold"
            R.id.wheQuantityQuantity -> "Select Wheels Threshold"
            else -> "Select Threshold"
        }

        val currentValue = editText.text.toString().takeIf { it.isNotEmpty() } ?: "1"

        showNumberPickerDialog(
            title = title,
            currentValue = currentValue
        ) {
            selectedValue -> editText.setText(selectedValue)
            // Update the corresponding threshold variable
            when (editText.id) {
                R.id.wenchQuantity -> wenchThreshold = selectedValue.toInt()
                R.id.slugPumpsQuantity -> pumpsThreshold = selectedValue.toInt()
                R.id.motorsQuantity -> motorsThreshold = selectedValue.toInt()
                R.id.generatorsQuantity -> generatorsThreshold = selectedValue.toInt()
                R.id.wheQuantityQuantity -> wheelThreshold = selectedValue.toInt()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun showNumberPickerDialog(
        title: String,
        currentValue: String = "1",
        onValueSelected: (String) -> Unit
    ) {
        val numberPicker = NumberPicker(requireContext()).apply {
            minValue = 1
            maxValue = 20
            value = currentValue.toIntOrNull() ?: minValue
            textColor = Color.BLACK
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                onValueSelected(numberPicker.value.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .create()
            .apply {
                show()
                // Customize dialog appearance
                findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.setTextColor(Color.BLACK)
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
                getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
            }
    }

    private fun updateInventoryThreshold(
        pipeThreshold: Int?,
        jointsThreshold: Int?,
        wenchThreshold: Int?,
        pumpsThreshold: Int?,
        motorsThreshold: Int?,
        generatorsThreshold: Int?,
        wheelThreshold: Int?
    ) {
        // Define thresholds in a map (key = keyword, value = threshold)
        val thresholds = mapOf(
            "pipe" to pipeThreshold,
            "joint" to jointsThreshold,
            "wench" to wenchThreshold,
            "pump" to pumpsThreshold,
            "motor" to motorsThreshold,
            "generator" to generatorsThreshold,
            "wheel" to wheelThreshold
        )

        itemList.forEach { item ->
            val lowerName = item.itemName?.lowercase()
            val databaseRef = item.itemId?.let { Firebase.database.reference.child("Inventory").child(it) }

            thresholds.forEach { (keyword, threshold) ->
                threshold?.let { th ->
                    if (lowerName?.contains(keyword) == true && th > 0) {
                        item.quantity?.takeIf { it >= th }?.let {
                            databaseRef?.updateChildren(mapOf("threshold" to th))
                        }
                    }
                }
            }
        }
    }
}