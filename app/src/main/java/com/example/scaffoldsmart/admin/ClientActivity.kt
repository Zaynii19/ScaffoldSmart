package com.example.scaffoldsmart.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
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
import com.example.scaffoldsmart.admin.admin_models.RentalClientModel
import com.example.scaffoldsmart.admin.admin_adapters.ClientRcvAdapter
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.databinding.ActivityClientBinding

class ClientActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientBinding.inflate(layoutInflater)
    }
    private var clientList = ArrayList<RentalClientModel>()
    private lateinit var adapter: ClientRcvAdapter
    private lateinit var viewModel: RentalViewModel

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
        viewModel = ViewModelProvider(this)[RentalViewModel::class.java]
        viewModel.retrieveRentalReq()
        observeRentalLiveData()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this@ClientActivity, SettingActivity::class.java))
        }

    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(this@ClientActivity, LinearLayoutManager.VERTICAL, false)
        adapter = ClientRcvAdapter(this, clientList)
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

    private fun observeRentalLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeRentalReqLiveData().observe(this@ClientActivity) { rentals ->
            binding.loading.visibility = View.GONE
            val filteredRentals = rentals?.filter { it.status.isNotEmpty() } // Get only approved rentals
            clientList.clear()
            filteredRentals?.forEach {
                clientList.add(RentalClientModel(it.rentalId ,it.clientName, it.clientEmail, it.clientCnic, it.rentalAddress, it.clientPhone))
                adapter.updateList(clientList)
                Log.d("AdminClientDebug", "observeRentalReqLiveData: ${clientList.size} ")
            }
        }
    }

}