package com.example.scaffoldsmart.admin

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.InventoryRcvAdapter
import com.example.scaffoldsmart.databinding.ActivityInventoryBinding
import com.example.scaffoldsmart.admin.admin_fragments.AddInventoryFragment
import com.example.scaffoldsmart.admin.admin_fragments.AddInventoryFragment.OnInventoryUpdatedListener
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.database.database

class InventoryActivity : AppCompatActivity(), InventoryRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        ActivityInventoryBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: InventoryRcvAdapter
    private var isUpdate = false
    private lateinit var inventoryPreferences: SharedPreferences
    private var itemList = ArrayList<InventoryModel>()
    private lateinit var viewModel: InventoryViewModel
    private lateinit var chatPreferences: SharedPreferences
    private var senderUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inventoryPreferences = getSharedPreferences("INVENTORY", MODE_PRIVATE)
        chatPreferences = getSharedPreferences("CHATADMIN", MODE_PRIVATE)

        setStatusBarColor()
        setRcv()
        setSearchView()

        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()
        observeInventoryLiveData()

        binding.swipeRefresh.setColorSchemeResources(R.color.item_color)
        binding.swipeRefresh.setOnRefreshListener {
            // Refresh data here
            viewModel.retrieveInventory() // Call your method to fetch data again
            observeInventoryLiveData()
            binding.swipeRefresh.isRefreshing = false // Hide the refresh animation after data is fetched
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addInventoryItem.setOnClickListener {
            isUpdate = false
            val editor = inventoryPreferences.edit()
            editor.putBoolean("Update", isUpdate)
            editor.apply()
            // Dummy item
            val item = InventoryModel()
            showBottomSheet(item)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this@InventoryActivity, SettingActivity::class.java))
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(this@InventoryActivity, LinearLayoutManager.VERTICAL, false)
        adapter = InventoryRcvAdapter(this, itemList, this)
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

                // Filter the itemList based on the search text (case-insensitive)
                val filteredList = itemList.filter { item ->
                    item.itemName.lowercase().contains(newText!!.lowercase())
                }

                // Update the adapter with the filtered list
                adapter.updateList(filteredList as ArrayList<InventoryModel>)
                return true
            }
        })
    }

    private fun showBottomSheet(item: InventoryModel) {
        val bottomSheetDialog: BottomSheetDialogFragment = AddInventoryFragment.newInstance(
            object : OnInventoryUpdatedListener {
                override fun onInventoryUpdated(itemId: String, itemName: String, price: String, quantity: String, availability: String) {
                    updateInventoryItem(item.itemId, itemName, price, quantity, availability)
                }

                override fun onInventoryAdded(itemName: String, price: String, quantity: String, availability: String) {
                    storeInventoryItem(itemName, price, quantity, availability)
                }
            }
        , item)
        bottomSheetDialog.show(this.supportFragmentManager, "Inventory")
    }

    private fun observeInventoryLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeInventoryLiveData().observe(this) { items ->
            binding.loading.visibility = View.GONE
            itemList.clear()
            items?.let {
                itemList.addAll(it)
                Log.d("InventoryDebug", "observeInventoryLiveData: ${it.size}")
            }
            adapter.updateList(itemList) // Notify the adapter about the changes
        }
    }

    // Store inventory in common node accessible by both admin and client
    private fun storeInventoryItem(itemName: String, price: String, quantity: String, availability: String) {
        // Reference to the inventory in Firebase
        val databaseRef = Firebase.database.reference.child("Inventory")
        val newItemRef = databaseRef.push()
        val itemId = newItemRef.key // Get the generated key

        if (itemId != null) {
            // Create new inventory item model
            val newItem = InventoryModel(itemId, itemName, price, quantity, availability)

            // Store the new item in Firebase
            newItemRef.setValue(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this@InventoryActivity, "Item added successfully", Toast.LENGTH_SHORT).show()
                    viewModel.retrieveInventory() // Refresh to reflect changes
                }
                .addOnFailureListener {
                    Toast.makeText(this@InventoryActivity, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this@InventoryActivity, "Failed to generate item ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateInventoryItem(itemId: String, itemName: String, price: String, quantity: String, availability: String) {
        // Reference to the specific item in Firebase
        val databaseRef = Firebase.database.reference.child("Inventory")
            .child(itemId) // Reference to the specific item using itemId

        // Create a map of the fields you want to update
        val updates = hashMapOf<String, Any>(
            "itemName" to itemName,
            "price" to price,
            "quantity" to quantity,
            "availability" to availability
        )

        // Update the item with the new values
        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this@InventoryActivity, "Item updated successfully", Toast.LENGTH_SHORT).show()
                viewModel.retrieveInventory() // Refresh to reflect changes
            }
            .addOnFailureListener {
                Toast.makeText(this@InventoryActivity, "Failed to update item", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onEditButtonClick(item: InventoryModel) {
        isUpdate = true
        val editor = inventoryPreferences.edit()
        editor.putBoolean("Update", isUpdate)
        editor.apply()
        showBottomSheet(item)
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
    }

    override fun onPause() {
        super.onPause()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Offline"
        presenceMap["lastSeen"] = currentTime
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
    }
}