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
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.databinding.FragmentRentBinding
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.util.DateFormater
import com.example.scaffoldsmart.util.SmartContract
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RentFragment : Fragment(), RentalRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        FragmentRentBinding.inflate(layoutInflater)
    }

    private var rentalList = ArrayList<RentalModel>()
    private lateinit var adapter: RentalRcvAdapter
    private lateinit var viewModel: RentalViewModel
    private lateinit var viewModelA: AdminViewModel
    private var rentStatus: String = ""
    private var smartContract: SmartContract? = null
    private var name: String = ""
    private var email: String = ""
    private var company: String = ""
    private var address: String = ""
    private var phone: String = ""

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
            val filteredRentals = rentals?.filter { it.status.isNotEmpty() } // Get only approved rentals
            rentalList.clear()
            filteredRentals?.let {
                rentalList.addAll(it)
                Log.d("RentFragDebug", "observeRentalReqLiveData: ${rentalList.size} ")
            }
            adapter.updateList(rentalList)
            calculateRentalStatus(rentalList)
        }
    }

    private fun observeAdminLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModelA.observeAdminLiveData().observe(viewLifecycleOwner) { admin ->
            binding.loading.visibility = View.GONE
            if (admin != null) {
                name = admin.name
                email = admin.email
                company = admin.company
                phone = admin.phone
                address = admin.address
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
                    item.clientName.lowercase().contains(newText!!.lowercase())
                }

                // Update the adapter with the filtered list
                adapter.updateList(filteredList as ArrayList<RentalModel>)
                return true
            }
        })
    }

    private fun calculateRentalStatus(rentalList: ArrayList<RentalModel>) {
        for (rental in rentalList) {

            // Determine the status
            val isOverdue = DateFormater.compareDateWithCurrentDate(rental.endDuration)
            rentStatus = if (isOverdue) {
                "overdue"
            } else {
                "ongoing"
            }

            // Call method to update the database with the new status
            updateRentalStatus(rental.rentalId, rentStatus)
        }
    }

    private fun updateRentalStatus(rentalId: String, newStatus: String) {
        // Reference to the specific rental in Firebase
        val databaseRef = Firebase.database.reference.child("Rentals").child(rentalId)

        // Create a map of the fields you want to update
        val updates = hashMapOf<String, Any>(
            "rentStatus" to newStatus
        )

        // Update the item with the new values
        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                Log.i("RentFragDebug", "Successfully updated status for rental ID: $rentalId to $newStatus")
            }
            .addOnFailureListener {
                Log.e("RentFragDebug", "Failed to update status for rental ID: $rentalId")
            }
    }

    override fun onDownloadButtonClick(rental: RentalModel) {
        smartContract!!.createScaffoldingContractPdf(
            requireActivity(), false, name, company, email, phone, address, rental.clientName, rental.clientPhone,
            rental.clientEmail, rental.clientCnic, rental.clientAddress, rental.rentalAddress, rental.startDuration,
            rental.endDuration, rental.pipes, rental.pipesLength, rental.joints, rental.wench, rental.motors,
            rental.pumps, rental.generators, rental.wheel
        )
    }
}