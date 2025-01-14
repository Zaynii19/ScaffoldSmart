package com.example.scaffoldsmart.client.client_fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.client.ClientSettingActivity
import com.example.scaffoldsmart.client.client_adapters.ClientScafoldRcvAdapter
import com.example.scaffoldsmart.client.client_models.ClientScafoldInfoModel
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.FragmentClientHomeBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.onesignal.OneSignal

class ClientHomeFragment : Fragment() {
    private val binding by lazy {
        FragmentClientHomeBinding.inflate(layoutInflater)
    }
    private var name: String = ""
    private var email: String = ""
    private var role: String = ""
    private var infoList = ArrayList<ClientScafoldInfoModel>()
    private lateinit var adapter: ClientScafoldRcvAdapter
    private lateinit var viewModel: ClientViewModel
    private lateinit var onesignal: OnesignalService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onesignal = OnesignalService(requireActivity())
        initializeInfoList()
        viewModel = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel.retrieveClientData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        observeClientLiveData()
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

    private fun observeClientLiveData() {
        binding.loading.visibility = View.VISIBLE
        viewModel.observeClientLiveData().observe(viewLifecycleOwner) { client ->
            binding.loading.visibility = View.GONE
            if (client != null) {
                name = client.name
                binding.welcomeTxt.text = buildString {
                    append("Welcome, ")
                    append(name)
                }

                email = client.email
                role = client.userType
                onesignal.oneSignalLogin(email, role)
            }
        }
    }
}