package com.example.scaffoldsmart.client.client_fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_adapters.ScafoldRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.ScafoldInfoModel
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_adapters.ClientScafoldRcvAdapter
import com.example.scaffoldsmart.client.client_models.ClientScafoldInfoModel
import com.example.scaffoldsmart.databinding.FragmentClientHomeBinding

class ClientHomeFragment : Fragment() {
    private val binding by lazy {
        FragmentClientHomeBinding.inflate(layoutInflater)
    }
    private var infoList = ArrayList<ClientScafoldInfoModel>()
    private lateinit var adapter: ClientScafoldRcvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeInfoList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
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
        adapter = ClientScafoldRcvAdapter(requireActivity(), infoList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun initializeInfoList() {
        infoList.add(ClientScafoldInfoModel("Pipes", "300 Pipes and 200 Joints", "3 Weeks"))
        infoList.add(ClientScafoldInfoModel("Joints", "300 Pipes and 200 Joints", "5 Weeks"))
        infoList.add(ClientScafoldInfoModel("Generators", "300 Pipes and 200 Joints", "12 Weeks"))
        infoList.add(ClientScafoldInfoModel("Motors", "300 Pipes and 200 Joints", "8 Weeks"))
    }
}