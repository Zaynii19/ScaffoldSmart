package com.example.scaffoldsmart.admin.admin_fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.ClientActivity
import com.example.scaffoldsmart.admin.InventoryActivity
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.admin.admin_adapters.ScafoldRcvAdapter
import com.example.scaffoldsmart.databinding.FragmentHomeBinding
import com.example.scaffoldsmart.admin.admin_models.ScafoldInfoModel

class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private var infoList = ArrayList<ScafoldInfoModel>()
    private lateinit var adapter: ScafoldRcvAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeInfoList()

        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ScafoldRcvAdapter(requireActivity(), infoList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.inventoryBtn.setOnClickListener {
            startActivity(Intent(context, InventoryActivity::class.java))
        }

        binding.clientBtn.setOnClickListener {
            startActivity(Intent(context, ClientActivity::class.java))
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initializeInfoList() {
        infoList.add(ScafoldInfoModel("Hasan", "300 Pipes and 200 Joints", "returned"))
        infoList.add(ScafoldInfoModel("Fatima", "300 Pipes and 200 Joints", "overdue"))
        infoList.add(ScafoldInfoModel("Ali", "300 Pipes and 200 Joints", "ongoing"))
        infoList.add(ScafoldInfoModel("Laiba", "300 Pipes and 200 Joints", "returned"))
        infoList.add(ScafoldInfoModel("Danish", "300 Pipes and 200 Joints", "overdue"))
        infoList.add(ScafoldInfoModel("Rabia", "300 Pipes and 200 Joints", "ongoing"))
        infoList.add(ScafoldInfoModel("Haider", "300 Pipes and 200 Joints", "returned"))
    }

}