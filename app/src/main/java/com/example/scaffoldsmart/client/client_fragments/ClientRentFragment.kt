package com.example.scaffoldsmart.client.client_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_adapters.ClientRentalRcvAdapter
import com.example.scaffoldsmart.databinding.FragmentClientRentBinding

class ClientRentFragment : Fragment() {
    private val binding by lazy {
        FragmentClientRentBinding.inflate(layoutInflater)
    }
    private var rentalList = ArrayList<RentalModel>()
    private lateinit var adapter: ClientRentalRcvAdapter
    private lateinit var viewModel: RentalViewModel
    private var clientUid: String? = null
    private lateinit var chatPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatPreferences = requireActivity().getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        clientUid = chatPreferences.getString("SenderUid", null)

        viewModel = ViewModelProvider(this)[RentalViewModel::class.java]
        viewModel.retrieveRentalReq()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        setSearchView()
        observeRentalLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, ClientSettingActivity::class.java))
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClientRentalRcvAdapter(requireActivity(), rentalList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeRentalLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            binding.loading.visibility = View.GONE
            val filteredRentals = rentals?.filter { it.status?.isNotEmpty() == true && it.clientID == clientUid } // Get only approved rentals
            rentalList.clear()
            filteredRentals?.let {
                rentalList.addAll(it)
                Log.d("ClientRentFragDebug", "observeRentalReqLiveData: ${rentalList.size} ")
            }
            adapter.updateList(rentalList)
        }
    }

    private fun setSearchView() {
        // Change text color to white of search view
        binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        // Get app color from colors.xml
        val appColor = ContextCompat.getColor(requireContext(), R.color.item_color)

        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)?.setColorFilter(appColor)
        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.setColorFilter(appColor)

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter the itemList based on the search text (case-insensitive)
                val filteredList = rentalList.filter { item ->
                    item.clientName?.lowercase()?.contains(newText?.lowercase() ?: "") == true
                }

                // Update the adapter with the filtered list
                adapter.updateList(filteredList as ArrayList<RentalModel>)
                return true
            }
        })
    }
}