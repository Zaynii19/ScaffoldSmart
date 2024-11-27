package com.example.scaffoldsmart.admin.admin_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_adapters.RentalRcvAdapter
import com.example.scaffoldsmart.databinding.FragmentRentBinding
import com.example.scaffoldsmart.admin.admin_models.RentalModel

class RentFragment : Fragment() {
    private val binding by lazy {
        FragmentRentBinding.inflate(layoutInflater)
    }
    private var rentalList = ArrayList<RentalModel>()
    private lateinit var adapter: RentalRcvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeRentalList()

        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = RentalRcvAdapter(requireActivity(), rentalList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initializeRentalList() {
        rentalList.add(RentalModel("Hasan", "returned", "PiPe","1200","Gulzar-e-Quaid, Satellite Town, Rawalpindi","12000 .Rs","7 Weeks","male"))
        rentalList.add(RentalModel("Fatima",  "overdue",  "Joints","1000","Holy Family Hospital, Murree Road","15000 .Rs","2 Weeks","female"))
        rentalList.add(RentalModel("Ali",  "ongoing", "Generators","5","Commercial Market, Satellite Town, Rawalpindi","20000 .Rs","8 Weeks","male"))
        rentalList.add(RentalModel("Laiba",  "returned", "Slugs Pump","2","Rawalpindi Railway Station, Mall Road","2000 .Rs","1 Week","female"))
        rentalList.add(RentalModel("Danish",  "overdue", "Wench","3","Bahria Town Phase Eight, Rawalpindi","22000 .Rs","6 Weeks","male"))
        rentalList.add(RentalModel("Rabia", "ongoing", "Wheel Barrows","2","Holy Family Hospital, Murree Road","2200 .Rs","9 Weeks","female"))
        rentalList.add(RentalModel("Haider",  "returned", "Motors","5","Rawalpindi Railway Station, Mall Road","7000 .Rs","3 Weeks","male"))
    }
}