package com.example.scaffoldsmart.client.client_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_models.RentalClientModel
import com.example.scaffoldsmart.client.client_adapters.ClientInventoryRcvAdapter
import com.example.scaffoldsmart.client.client_models.ClientInventoryItemIModel
import com.example.scaffoldsmart.databinding.FragmentClientInventoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClientInventoryFragment : Fragment() {
    private val binding by lazy {
        FragmentClientInventoryBinding.inflate(layoutInflater)
    }
    private var reqList = ArrayList<RentalClientModel>()
    private var itemList = ArrayList<ClientInventoryItemIModel>()
    private lateinit var adapter: ClientInventoryRcvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeItemList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()

        binding.rentalRequestBtn.setOnClickListener {
            showBottomSheet()
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setRcv() {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClientInventoryRcvAdapter(requireActivity(), itemList)
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }

    private fun initializeItemList() {
        itemList.add(ClientInventoryItemIModel("Pipes", "5000"))
        itemList.add(ClientInventoryItemIModel("Joints", "2000"))
        itemList.add(ClientInventoryItemIModel("Motors", "1000"))
        itemList.add(ClientInventoryItemIModel("Generators", "500"))
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = RentalReqFragment.newInstance(object : RentalReqFragment.OnSendReqListener {
            override fun onReqSendUpdated(clientName: String) {
                // Add the new item to the list
                reqList.add(RentalClientModel(clientName))
            }
        })
        bottomSheetDialog.show(requireActivity().supportFragmentManager, "RentalReq")
    }
}