package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_adapters.RentalRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.databinding.FragmentRentBinding
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.util.SmartContract

class RentFragment : Fragment(), RentalRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        FragmentRentBinding.inflate(layoutInflater)
    }

    private var rentalList = ArrayList<RentalModel>()
    private lateinit var adapter: RentalRcvAdapter
    private lateinit var viewModel: RentalViewModel
    private lateinit var viewModelA: AdminViewModel
    private var smartContract: SmartContract? = null
    private var adminObj: AdminModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smartContract = SmartContract()

        viewModel = ViewModelProvider(this)[RentalViewModel::class.java]
        viewModel.retrieveRentalReq()

        viewModelA = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModelA.retrieveAdminData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        setSearchView()
        observeRentalLiveData()
        observeAdminLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = RentalRcvAdapter(requireActivity(), rentalList, this@RentFragment)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun observeRentalLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeRentalReqLiveData().observe(viewLifecycleOwner) { rentals ->
            binding.loading.visibility = View.GONE
            val filteredRentals = rentals?.filter { it.status?.isNotEmpty() == true } // Get only approved rentals
            rentalList.clear()
            filteredRentals?.let {
                rentalList.addAll(it)
                Log.d("RentFragDebug", "observeRentalReqLiveData: ${rentalList.size} ")
            }
            adapter.updateList(rentalList)
        }
    }

    private fun observeAdminLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModelA.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            binding.loading.visibility = View.GONE
            if (admin != null) {
                adminObj = admin
            }
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

    override fun onDownloadButtonClick(rental: RentalModel) {
        smartContract?.createScaffoldingContractPdf(
            requireActivity(), false, adminObj?.name, adminObj?.company, adminObj?.email, adminObj?.phone, adminObj?.address,
            rental.clientName, rental.clientPhone, rental.clientEmail, rental.clientCnic, rental.clientAddress, rental.rentalAddress,
            rental.startDuration, rental.endDuration, rental.pipes.toString(), rental.pipesLength.toString(), rental.joints.toString(),
            rental.wench.toString(), rental.motors.toString(), rental.pumps.toString(), rental.generators.toString(), rental.wheel.toString()
        )
    }
}