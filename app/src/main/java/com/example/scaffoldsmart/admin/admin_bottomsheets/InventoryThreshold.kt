package com.example.scaffoldsmart.admin.admin_bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.ThresholdRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.databinding.InventoryThresholdBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.database.database

class InventoryThreshold : BottomSheetDialogFragment(), ThresholdRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        InventoryThresholdBinding.inflate(layoutInflater)
    }
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var viewModel: InventoryViewModel
    private lateinit var adapter: ThresholdRcvAdapter
    private var thresholdMap = mutableMapOf<String?, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeInventoryLiveData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemList.let { items ->
            if (items.isEmpty()) {
                binding.saveBtn.isEnabled = false
                binding.saveBtn.backgroundTintList = ContextCompat.getColorStateList(requireActivity(), R.color.dark_gray)
            }
        }

        binding.saveBtn.setOnClickListener {
            val hasNonZeroThreshold = thresholdMap.values.any { it != 0 }
            if (!hasNonZeroThreshold || thresholdMap.isEmpty()) {
                Toast.makeText(requireActivity(), "Please enter at least one item threshold value", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                updateInventoryThreshold(thresholdMap)
            }
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ThresholdRcvAdapter(requireActivity(), ArrayList(), this)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeInventoryLiveData() {
        viewModel.observeInventoryLiveData().observe(viewLifecycleOwner) { items ->
            itemList.clear()
            items?.let {
                itemList.addAll(it)
                adapter.updateData(itemList)
                binding.saveBtn.isEnabled = true
                binding.saveBtn.backgroundTintList = ContextCompat.getColorStateList(requireActivity(), R.color.buttons_color)

                if (itemList.isEmpty()) {
                    Toast.makeText(requireActivity(), "No Item Found", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    override fun onEditTextTyped(newThresholdQuantity: Int, currentItem: InventoryModel) {
        thresholdMap[currentItem.itemId] = newThresholdQuantity
    }

    private fun updateInventoryThreshold(thresholdMap: MutableMap<String?, Int>) {
        val updates = mutableMapOf<String, Any>()
        thresholdMap.forEach { (key, value) ->
            key?.let {
                updates["Inventory/$it/threshold"] = value
            }
        }

        Firebase.database.reference.updateChildren(updates)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireActivity(), "Threshold values updated successfully", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireActivity(), "Failed to update threshold values: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}