package com.example.scaffoldsmart.admin.admin_fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.RentalRcvAdapter
import com.example.scaffoldsmart.databinding.FragmentRentBinding
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.google.firebase.messaging.FirebaseMessaging

class RentFragment : Fragment() {
    private val binding by lazy {
        FragmentRentBinding.inflate(layoutInflater)
    }
    private var rentalList = ArrayList<RentalModel>()
    private lateinit var adapter: RentalRcvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeRentalList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        setSearchView()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = RentalRcvAdapter(requireActivity(), rentalList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun initializeRentalList() {
        rentalList.add(RentalModel("Hasan", "returned", "PiPe","1200","Gulzar-e-Quaid, Satellite Town, Rawalpindi","12000 .Rs","7 Weeks"))
        rentalList.add(RentalModel("Fatima",  "overdue",  "Joints","1000","Holy Family Hospital, Murree Road","15000 .Rs","2 Weeks"))
        rentalList.add(RentalModel("Ali",  "ongoing", "Generators","5","Commercial Market, Satellite Town, Rawalpindi","20000 .Rs","8 Weeks"))
        rentalList.add(RentalModel("Laiba",  "returned", "Slugs Pump","2","Rawalpindi Railway Station, Mall Road","2000 .Rs","1 Week"))
        rentalList.add(RentalModel("Danish",  "overdue", "Wench","3","Bahria Town Phase Eight, Rawalpindi","22000 .Rs","6 Weeks"))
        rentalList.add(RentalModel("Rabia", "ongoing", "Wheel Barrows","2","Holy Family Hospital, Murree Road","2200 .Rs","9 Weeks"))
        rentalList.add(RentalModel("Haider",  "returned", "Motors","5","Rawalpindi Railway Station, Mall Road","7000 .Rs","3 Weeks"))
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
                adapter.filter(newText ?: "") // Call filter method on text change
                return true
            }
        })
    }
}