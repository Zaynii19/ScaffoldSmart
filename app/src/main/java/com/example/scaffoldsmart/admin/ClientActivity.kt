package com.example.scaffoldsmart.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.ClientModel
import com.example.scaffoldsmart.admin.admin_adapters.ClientRcvAdapter
import com.example.scaffoldsmart.databinding.ActivityClientBinding
import com.example.scaffoldsmart.admin.admin_fragments.AddClientFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClientActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientBinding.inflate(layoutInflater)
    }
    private var clientList = ArrayList<ClientModel>()
    private lateinit var adapter: ClientRcvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rcv.layoutManager = LinearLayoutManager(this@ClientActivity, LinearLayoutManager.VERTICAL, false)
        adapter = ClientRcvAdapter(this, clientList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)

        binding.addInventoryItem.setOnClickListener {
            showBottomSheet()
        }

    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = AddClientFragment.newInstance(object : AddClientFragment.OnClientUpdatedListener {
            override fun onClientUpdated(clientName: String, gender:String, status:String) {
                // Add the new item to the list
                clientList.add(ClientModel(clientName, status, gender))

                // Notify the adapter that a new item has been added
                adapter.notifyItemInserted(clientList.size - 1)
            }
        })
        bottomSheetDialog.show(this.supportFragmentManager, "Client")
    }
}