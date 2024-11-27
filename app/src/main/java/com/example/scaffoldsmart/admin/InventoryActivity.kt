package com.example.scaffoldsmart.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.InventoryRcvAdapter
import com.example.scaffoldsmart.databinding.ActivityInventoryBinding
import com.example.scaffoldsmart.admin.admin_fragments.AddInventoryFragment
import com.example.scaffoldsmart.admin.admin_models.InventoryItemIModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InventoryActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityInventoryBinding.inflate(layoutInflater)
    }

    private var itemList = ArrayList<InventoryItemIModel>()
    private lateinit var adapter: InventoryRcvAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rcv.layoutManager = LinearLayoutManager(this@InventoryActivity, LinearLayoutManager.VERTICAL, false)
        adapter = InventoryRcvAdapter(this, itemList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)

        binding.addInventoryItem.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = AddInventoryFragment.newInstance(object : AddInventoryFragment.OnInventoryUpdatedListener {
            override fun onInventoryUpdated(itemName: String, price:String) {
                // Add the new item to the list
                itemList.add(InventoryItemIModel(itemName, price))

                // Notify the adapter that a new item has been added
                adapter.notifyItemInserted(itemList.size - 1)
            }
        })
        bottomSheetDialog.show(this.supportFragmentManager, "Inventory")
    }
}