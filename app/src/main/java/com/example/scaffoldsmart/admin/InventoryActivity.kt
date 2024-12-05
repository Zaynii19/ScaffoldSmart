package com.example.scaffoldsmart.admin

import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
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

        setStatusBarColor()
        setRcv()
        setSearchView()

        binding.addInventoryItem.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(this@InventoryActivity, LinearLayoutManager.VERTICAL, false)
        adapter = InventoryRcvAdapter(this, itemList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun setSearchView() {
        // Change text color to white of search view
        binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        // Get app color from colors.xml
        val appColor = ContextCompat.getColor(this, R.color.item_color)

        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)?.setColorFilter(appColor)
        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.setColorFilter(appColor)

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "") // Call filter method on text change
                return true
            }
        })
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