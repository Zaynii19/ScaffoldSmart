package com.example.scaffoldsmart.admin

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import com.example.scaffoldsmart.admin.admin_fragments.AddInventoryFragment.OnInventoryUpdatedListener
import com.example.scaffoldsmart.admin.admin_models.InventoryItemIModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import java.util.Locale

class InventoryActivity : AppCompatActivity(), InventoryRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        ActivityInventoryBinding.inflate(layoutInflater)
    }

    private var itemList = ArrayList<InventoryItemIModel>()
    private lateinit var adapter: InventoryRcvAdapter
    private var isUpdate = false
    private lateinit var inventoryPreferences: SharedPreferences

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

        setStatusBarColor()
        setRcv()
        setSearchView()
        retrieveInventory()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addInventoryItem.setOnClickListener {
            isUpdate = false
            val editor = inventoryPreferences.edit()
            editor.putBoolean("Update", isUpdate)
            editor.apply()
            // Dummy item
            val item = InventoryItemIModel()
            showBottomSheet(item)
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
                // Convert newText to lower case for case insensitive search
                val searchText = newText?.lowercase(Locale.getDefault()) ?: ""

                // Filter the itemList based on the search text
                val filteredList = itemList.filter { item ->
                    item.itemName.contains(searchText)
                }

                // Update the adapter with the filtered list
                adapter.updateList(filteredList as ArrayList<InventoryItemIModel>)
                return true
            }
        })
    }

    private fun showBottomSheet(item: InventoryItemIModel) {
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

    private fun storeInventoryItem(itemName: String, price: String, quantity: String, availability: String) {
        val databaseRef = Firebase.database.reference.child("Inventory").child(Firebase.auth.currentUser!!.uid)
        val newItemRef = databaseRef.push()
        val itemId = newItemRef.key // Get the generated key

        if (itemId != null) {
            val newItem = InventoryItemIModel(itemId, itemName, price, quantity, availability)

            newItemRef.setValue(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this@InventoryActivity, "Item added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@InventoryActivity, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this@InventoryActivity, "Failed to generate item ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveInventory() {
        Firebase.database.reference.child("Inventory").child(Firebase.auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newItems = ArrayList<InventoryItemIModel>()
                    for (child in snapshot.children) {
                        val inventory = child.getValue<InventoryItemIModel>()
                        if (inventory != null) {
                            inventory.itemId = child.key ?: "" // Assign Firebase key as itemId
                            newItems.add(inventory)
                        }
                    }

                    // Update the itemList directly
                    itemList.clear()
                    itemList.addAll(newItems)
                    // Notify the adapter about the changes
                    adapter.updateList(itemList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InventoryActivity", "Failed to retrieve inventory", error.toException())
                }
            })
    }

    private fun updateInventoryItem(itemId: String, itemName: String, price: String, quantity: String, availability: String) {
        // Get a reference to the specific item in Firebase
        val databaseRef = Firebase.database.reference.child("Inventory")
            .child(Firebase.auth.currentUser!!.uid)
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
            }
            .addOnFailureListener {
                Toast.makeText(this@InventoryActivity, "Failed to update item", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onEditButtonClick(item: InventoryItemIModel) {
        isUpdate = true
        val editor = inventoryPreferences.edit()
        editor.putBoolean("Update", isUpdate)
        editor.apply()
        showBottomSheet(item)
    }
}